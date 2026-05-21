package org.llw.studio.build;

import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.particles.ParticleSystemRefs;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.serialization.PrefabSerializer;
import org.llw.studio.serialization.SceneSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Deep-scans all project scenes and expands transitive asset dependencies.
 */
public final class BuildAssetScanner {
    private BuildAssetScanner() {
    }

    /**
     * @param projectRoot project root
     * @param assets      indexed asset database
     * @return assets reachable from any scene in the project, grouped by pack category
     * @throws IOException when a scene or prefab cannot be read
     */
    public static BuildAssetSet scan(Path projectRoot, AssetDatabase assets) throws IOException {
        Set<String> seedGuids = new LinkedHashSet<>();
        Map<String, StudioAsset> supplementalByGuid = new HashMap<>();
        List<String> log = new ArrayList<>();

        List<Path> scenePaths = discoverSceneFiles(projectRoot);
        log.add("Discovered " + scenePaths.size() + " scene file(s).");
        for (Path scenePath : scenePaths) {
            var scene = SceneSerializer.load(scenePath);
            Set<String> sceneGuids = SceneGuidCollector.collectFromScene(scene);
            seedGuids.addAll(sceneGuids);
            String sceneGuid = BuildAssetPaths.guidForPath(projectRoot, scenePath);
            seedGuids.add(sceneGuid);
            StudioAsset sceneAsset = assets.getByPath(scenePath);
            if (sceneAsset == null) {
                supplementalByGuid.put(sceneGuid, BuildAssetPaths.sceneAsset(scenePath, sceneGuid));
            }
            log.add("Scene " + scenePath.getFileName() + ": " + sceneGuids.size() + " direct reference(s).");
        }

        Set<String> referenced = expandClosure(projectRoot, assets, seedGuids, supplementalByGuid, log);
        EnumMap<BuildPackCategory, List<StudioAsset>> grouped = new EnumMap<>(BuildPackCategory.class);
        for (BuildPackCategory category : BuildPackCategory.values()) {
            grouped.put(category, new ArrayList<>());
        }

        for (String guid : referenced) {
            StudioAsset asset = assets.get(guid);
            if (asset == null) {
                asset = supplementalByGuid.get(guid);
            }
            if (asset == null) {
                log.add("Warning: missing asset for GUID " + guid);
                continue;
            }
            if (asset.isFolder() || asset.type() == AssetType.UNKNOWN) {
                continue;
            }
            grouped.get(BuildPackCategory.METADATA).add(asset);
            if (asset.type() == AssetType.SPRITE) {
                continue;
            }
            BuildPackCategory category = categoryFor(asset.type());
            if (category != null) {
                grouped.get(category).add(asset);
            }
            if (asset.type() == AssetType.TEXTURE) {
                for (StudioAsset sprite : assets.spriteChildren(guid)) {
                    if (referenced.contains(sprite.guid())) {
                        grouped.get(BuildPackCategory.METADATA).add(sprite);
                    }
                }
            }
        }

        int totalAssets = assets.allAssets().size();
        log.add("Packaging " + referenced.size() + " referenced asset(s); skipping "
                + Math.max(0, totalAssets - referenced.size()) + " unreferenced asset(s).");
        return new BuildAssetSet(referenced, grouped, supplementalByGuid, log);
    }

    private static Set<String> expandClosure(
            Path projectRoot,
            AssetDatabase assets,
            Set<String> seedGuids,
            Map<String, StudioAsset> supplementalByGuid,
            List<String> log
    ) throws IOException {
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> pending = new ArrayDeque<>(seedGuids);
        while (!pending.isEmpty()) {
            String guid = pending.poll();
            if (guid == null || guid.isBlank() || !visited.add(guid)) {
                continue;
            }
            StudioAsset asset = assets.get(guid);
            if (asset == null) {
                asset = supplementalByGuid.get(guid);
            }
            if (asset == null) {
                continue;
            }
            switch (asset.type()) {
                case SPRITE -> {
                    String textureGuid = asset.parentTextureGuid();
                    if (textureGuid != null && !textureGuid.isBlank()) {
                        pending.add(textureGuid);
                    }
                }
                case ANIMATION -> {
                    for (StudioAsset clip : assets.clipChildren(guid)) {
                        pending.add(clip.guid());
                    }
                    AnimationSetDefinition set = assets.animationSet(guid);
                    if (set != null) {
                        for (var state : set.states) {
                            if (state.clipGuid() != null && !state.clipGuid().isBlank()) {
                                pending.add(state.clipGuid());
                            }
                        }
                    }
                }
                case ANIMATION_CLIP -> {
                    String parent = asset.parentAnimationGuid();
                    if (parent != null && !parent.isBlank()) {
                        pending.add(parent);
                    }
                }
                case PREFAB -> {
                    var prefab = PrefabSerializer.load(asset.path());
                    Set<String> prefabGuids = SceneGuidCollector.collectFromPrefabObjects(prefab.objectNodes());
                    for (String prefabGuid : prefabGuids) {
                        pending.add(prefabGuid);
                    }
                }
                case PARTICLE_SYSTEM -> {
                    ParticleSystemDocument document = assets.loadParticleSystem(asset.path());
                    if (document != null) {
                        for (String ref : ParticleSystemRefs.collectGuids(document)) {
                            pending.add(ref);
                        }
                    }
                }
                default -> {
                }
            }
        }
        log.add("Expanded dependency closure to " + visited.size() + " GUID(s).");
        return visited;
    }

    private static List<Path> discoverSceneFiles(Path projectRoot) throws IOException {
        List<Path> scenes = new ArrayList<>();
        Path scenesDir = projectRoot.resolve("Scenes");
        if (Files.isDirectory(scenesDir)) {
            try (Stream<Path> walk = Files.walk(scenesDir)) {
                walk.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".scene.json"))
                        .forEach(scenes::add);
            }
        }
        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
        if (Files.isDirectory(assetsRoot)) {
            try (Stream<Path> walk = Files.walk(assetsRoot)) {
                walk.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".scene.json"))
                        .forEach(scenes::add);
            }
        }
        scenes.sort(Path::compareTo);
        return scenes;
    }

    private static BuildPackCategory categoryFor(AssetType type) {
        return switch (type) {
            case TEXTURE -> BuildPackCategory.TEXTURES;
            case FONT -> BuildPackCategory.FONTS;
            case AUDIO -> BuildPackCategory.AUDIO;
            case SCENE -> BuildPackCategory.SCENES;
            case SCRIPT -> BuildPackCategory.SCRIPTS;
            case PREFAB -> BuildPackCategory.PREFABS;
            case ANIMATION, ANIMATION_CLIP -> BuildPackCategory.ANIMATIONS;
            case SHADER_GRAPH -> BuildPackCategory.SHADERS;
            case PARTICLE_SYSTEM -> BuildPackCategory.PARTICLES;
            default -> null;
        };
    }
}
