package org.llw.studio.assets;

import org.llw.audio.SoundBuffer;
import org.llw.render.graphics.Texture2d;
import org.llw.resources.AssetRef;
import org.llw.resources.ResourceManager;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationClipSerializer;
import org.llw.studio.materials.assets.MaterialSerializer;
import org.llw.studio.materials.model.MaterialDocument;
import org.llw.studio.particles.ParticleSystemSerializer;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.shadergraph.assets.ShaderGraphSerializer;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.ui.UiFontCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * In-memory index of all assets under a project's {@code Assets} folder, with selection state
 * and lazy texture loading through the engine {@link ResourceManager}.
 */
public final class AssetDatabase {
    /** Drag-and-drop payload key for asset GUID strings. */
    public static final String PAYLOAD_ASSET_GUID = "ASSET_GUID";

    private Path projectRoot;
    private Path assetsRoot;
    private final ResourceManager resources;
    private final Map<String, StudioAsset> byGuid = new HashMap<>();
    private final Map<Path, String> guidByPath = new HashMap<>();
    private final Map<String, Texture2d> textureCache = new HashMap<>();
    private final Map<String, AssetRef<SoundBuffer>> soundCache = new HashMap<>();
    private final UiFontCache uiFontCache;
    private final Map<String, SpriteDefinition> spritesByGuid = new HashMap<>();
    private final Map<String, List<String>> spriteGuidsByTexture = new HashMap<>();
    private final Map<String, String> defaultSpriteByTexture = new HashMap<>();
    private final Map<String, AnimationClip> clipCache = new HashMap<>();
    private final Map<String, AnimationSetDefinition> animationSetCache = new HashMap<>();
    private final Map<String, List<String>> clipGuidsByAnimation = new HashMap<>();
    private final Map<String, TilesetDefinition> tilesetByTextureGuid = new HashMap<>();
    private final Map<String, TileDefinition> tileBySpriteGuid = new HashMap<>();
    private final Map<String, TextureImportSettings> textureImportSettingsByGuid = new HashMap<>();
    private final Map<String, Integer> shaderGraphRevision = new HashMap<>();
    private final Map<String, Integer> particleRevision = new HashMap<>();
    private final Map<String, Integer> materialRevision = new HashMap<>();
    private String selectedGuid;
    private String infoGuid;

    /**
     * @param projectRoot project directory containing an {@code Assets} subfolder
     * @param resources engine resource manager used to load textures by GUID
     */
    public AssetDatabase(Path projectRoot, ResourceManager resources) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.assetsRoot = this.projectRoot.resolve("Assets");
        this.resources = resources;
        this.uiFontCache = new UiFontCache(resources);
    }

    /** @return engine resource manager used to load textures and sounds */
    public ResourceManager resourceManager() {
        return resources;
    }

    /** @return shared UI font cache; holds {@link AssetRef} until {@link #clearUiFontCache()} */
    public UiFontCache uiFontCache() {
        return uiFontCache;
    }

    /**
     * Re-scans the assets tree from disk, clearing caches and selection.
     *
     * @throws IllegalStateException if the assets folder cannot be read
     */
    public void refresh() {
        byGuid.clear();
        guidByPath.clear();
        textureCache.clear();
        clearSoundCache();
        clearUiFontCache();
        spritesByGuid.clear();
        spriteGuidsByTexture.clear();
        defaultSpriteByTexture.clear();
        clipCache.clear();
        animationSetCache.clear();
        clipGuidsByAnimation.clear();
        tilesetByTextureGuid.clear();
        tileBySpriteGuid.clear();
        shaderGraphRevision.clear();
        particleRevision.clear();
        selectedGuid = null;
        infoGuid = null;
        try {
            Files.createDirectories(assetsRoot);
            indexFolder(assetsRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to refresh assets", ex);
        }
    }

    /**
     * Points the database at another project's {@code Assets} folder and refreshes.
     *
     * @param projectRoot new project root directory
     */
    public void rebindProject(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        assetsRoot = this.projectRoot.resolve("Assets");
        refresh();
    }

    private void indexFolder(Path folder) throws IOException {
        MetaFile.MetaData folderMeta = MetaFile.read(projectRoot, assetsRoot, folder);
        StudioAsset folderAsset = toAsset(folder, folderMeta);
        byGuid.put(folderAsset.guid(), folderAsset);
        guidByPath.put(folder.normalize(), folderAsset.guid());
        try (Stream<Path> stream = Files.list(folder)) {
            List<Path> entries = stream.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase())).toList();
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    indexFolder(entry);
                } else {
                    MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, entry);
                    if (AssetType.TEXTURE.name().equals(meta.type)) {
                        if (TextureSpriteImporter.ensureImported(entry, meta)) {
                            MetaFile.write(projectRoot, assetsRoot, entry, meta);
                        }
                    }
                    StudioAsset asset = toAsset(entry, meta);
                    byGuid.put(asset.guid(), asset);
                    guidByPath.put(entry.normalize(), asset.guid());
                    if (asset.type() == AssetType.TEXTURE) {
                        indexTextureSprites(entry, asset, meta);
                    }
                    if (asset.type() == AssetType.ANIMATION) {
                        indexAnimationSet(entry, asset);
                    }
                }
            }
        }
        linkOrphanClipsToParentAnimations();
    }

    private void linkOrphanClipsToParentAnimations() {
        for (StudioAsset asset : new ArrayList<>(byGuid.values())) {
            if (asset.type() != AssetType.ANIMATION_CLIP || asset.parentAnimationGuid() != null) {
                continue;
            }
            Path folder = asset.path().getParent();
            if (folder == null) {
                continue;
            }
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path entry : stream.toList()) {
                    String name = entry.getFileName().toString().toLowerCase();
                    if (!name.endsWith(".animation.json")) {
                        continue;
                    }
                    StudioAsset parent = getByPath(entry);
                    if (parent != null) {
                        registerClipUnderAnimation(asset, parent.guid());
                        break;
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void registerClipUnderAnimation(StudioAsset clipAsset, String animationGuid) {
        StudioAsset updated = new StudioAsset(
                clipAsset.guid(),
                clipAsset.path(),
                clipAsset.type(),
                clipAsset.displayName(),
                clipAsset.lastModified(),
                clipAsset.parentTextureGuid(),
                animationGuid
        );
        byGuid.put(updated.guid(), updated);
        List<String> guids = new ArrayList<>(clipGuidsByAnimation.getOrDefault(animationGuid, List.of()));
        if (!guids.contains(updated.guid())) {
            guids.add(updated.guid());
            clipGuidsByAnimation.put(animationGuid, List.copyOf(guids));
        }
    }

    private void indexAnimationSet(Path animationPath, StudioAsset animationAsset) throws IOException {
        AnimationSetDefinition set;
        if (Files.exists(animationPath)) {
            set = AnimationSetSerializer.load(animationPath);
        } else {
            set = new AnimationSetDefinition();
        }
        Path folder = animationPath.getParent();
        List<AnimationClipEntry> discovered = new ArrayList<>();
        List<String> clipGuids = new ArrayList<>();
        if (folder != null && Files.isDirectory(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path entry : stream.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase())).toList()) {
                    String fileName = entry.getFileName().toString();
                    if (!fileName.toLowerCase().endsWith(".anim.json")) {
                        continue;
                    }
                    MetaFile.MetaData clipMeta = MetaFile.read(projectRoot, assetsRoot, entry);
                    String clipGuid = clipMeta.guid;
                    discovered.add(new AnimationClipEntry(clipGuid, clipBaseName(fileName), fileName));
                    clipGuids.add(clipGuid);
                    StudioAsset existing = getByPath(entry);
                    if (existing != null) {
                        registerClipUnderAnimation(existing, animationAsset.guid());
                    }
                }
            }
        }
        set.clips.clear();
        set.clips.addAll(discovered);
        if (set.defaultState == null || set.defaultState.isBlank()) {
            set.defaultState = "Idle";
        }
        if (set.states.isEmpty() && !discovered.isEmpty()) {
            for (AnimationClipEntry entry : discovered) {
                String stateName = clipBaseName(entry.name());
                set.states.add(new AnimationStateDefinition(stateName, entry.guid()));
            }
            if (set.defaultState.isBlank()) {
                set.defaultState = clipBaseName(discovered.get(0).name());
            }
        }
        AnimationSetSerializer.save(animationPath, set);
        animationSetCache.put(animationAsset.guid(), set);
        clipGuidsByAnimation.put(animationAsset.guid(), List.copyOf(clipGuids));
    }

    private static String clipBaseName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".anim.json")) {
            return fileName.substring(0, fileName.length() - ".anim.json".length());
        }
        return fileName;
    }

    private void indexTextureSprites(Path texturePath, StudioAsset textureAsset, MetaFile.MetaData meta) throws IOException {
        org.llw.render.core.IntSize size = resolveTexturePixelSize(textureAsset.guid(), texturePath);
        List<SpriteDefinition> sprites = TextureSpriteData.parseSprites(
                meta.importer, textureAsset.guid(), size.width(), size.height());
        if (sprites.isEmpty()) {
            SpriteDefinition full = TextureSpriteImporter.fullImageSprite(
                    textureAsset.guid(),
                    textureAsset.displayName(),
                    size.width(),
                    size.height()
            );
            sprites = List.of(full);
        }
        Instant modified = textureAsset.lastModified();
        Set<String> guids = new LinkedHashSet<>();
        String defaultGuid = null;
        for (SpriteDefinition sprite : sprites) {
            if (!guids.add(sprite.guid())) {
                continue;
            }
            spritesByGuid.put(sprite.guid(), sprite);
            StudioAsset sub = new StudioAsset(
                    sprite.guid(),
                    texturePath,
                    AssetType.SPRITE,
                    sprite.name(),
                    modified,
                    textureAsset.guid()
            );
            byGuid.put(sprite.guid(), sub);
            if (defaultGuid == null) {
                defaultGuid = sprite.guid();
            }
        }
        spriteGuidsByTexture.put(textureAsset.guid(), List.copyOf(new ArrayList<>(guids)));
        if (defaultGuid != null) {
            defaultSpriteByTexture.put(textureAsset.guid(), defaultGuid);
        }
        indexTileset(textureAsset.guid(), meta, sprites);
    }

    private void indexTileset(String textureGuid, MetaFile.MetaData meta, List<SpriteDefinition> sprites) {
        TilesetDefinition tileset = TilesetData.parse(meta.importer, textureGuid, sprites);
        tilesetByTextureGuid.put(textureGuid, tileset);
        for (TileDefinition tile : tileset.tiles) {
            if (tile.spriteGuid != null && !tile.spriteGuid.isBlank()) {
                tileBySpriteGuid.put(tile.spriteGuid, tile);
            }
        }
    }

    private org.llw.render.core.IntSize resolveTexturePixelSize(String textureGuid, Path texturePath) {
        if (resources != null) {
            Texture2d loaded = texture(textureGuid);
            if (loaded != null) {
                return loaded.size();
            }
        }
        return TextureImageSize.read(texturePath);
    }

    private StudioAsset toAsset(Path path, MetaFile.MetaData meta) throws IOException {
        AssetType type = AssetType.valueOf(meta.type);
        Instant modified = Files.getLastModifiedTime(path).toInstant();
        String name = path.getFileName().toString();
        if (type == AssetType.SHADER_GRAPH) {
            shaderGraphRevision.put(meta.guid, (int) modified.toEpochMilli());
        }
        if (type == AssetType.PARTICLE_SYSTEM) {
            particleRevision.put(meta.guid, (int) modified.toEpochMilli());
        }
        if (type == AssetType.MATERIAL) {
            materialRevision.put(meta.guid, (int) modified.toEpochMilli());
        }
        return new StudioAsset(meta.guid, path, type, name, modified);
    }

    /**
     * @param path absolute or assets-relative path to a shader graph file
     * @return loaded document, or null on failure
     */
    public ShaderGraphDocument loadShaderGraph(Path path) {
        try {
            return ShaderGraphSerializer.load(path);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param guid shader graph asset GUID
     * @return revision token used by {@link org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache}
     */
    public int shaderGraphRevision(String guid) {
        return shaderGraphRevision.getOrDefault(guid, 0);
    }

    /** Invalidates cached compiled programs for a shader graph asset. */
    public void bumpShaderGraphRevision(String guid) {
        if (guid != null && !guid.isBlank()) {
            shaderGraphRevision.merge(guid, 1, Integer::sum);
        }
    }

    /**
     * @param path absolute or assets-relative path to a particle system file
     * @return loaded document, or null on failure
     */
    public ParticleSystemDocument loadParticleSystem(Path path) {
        try {
            return ParticleSystemSerializer.load(path);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param guid particle system asset GUID
     * @return revision token for editor preview invalidation
     */
    public int particleRevision(String guid) {
        return particleRevision.getOrDefault(guid, 0);
    }

    /** Invalidates cached preview state for a particle system asset. */
    public void bumpParticleRevision(String guid) {
        if (guid != null && !guid.isBlank()) {
            particleRevision.merge(guid, 1, Integer::sum);
        }
    }

    /**
     * @param path absolute or assets-relative path to a material file
     * @return loaded document, or null on failure
     */
    public MaterialDocument loadMaterial(Path path) {
        if (path == null) {
            return null;
        }
        try {
            if (Files.isRegularFile(path)) {
                return MaterialSerializer.load(path);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * @param guid material asset GUID
     * @return loaded document from disk or packed raw bytes
     */
    public MaterialDocument loadMaterial(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        StudioAsset asset = get(guid);
        if (asset != null && asset.type() == AssetType.MATERIAL) {
            MaterialDocument fromDisk = loadMaterial(asset.path());
            if (fromDisk != null) {
                return fromDisk;
            }
        }
        if (resources != null && resources.isRegistered(guid)) {
            try (AssetRef<byte[]> ref = resources.acquireRaw(guid)) {
                return MaterialSerializer.load(ref.get());
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    /**
     * @param guid material asset GUID
     * @return revision token for material program cache
     */
    public int materialRevision(String guid) {
        return materialRevision.getOrDefault(guid, 0);
    }

    /** Invalidates cached programs for a material asset. */
    public void bumpMaterialRevision(String guid) {
        if (guid != null && !guid.isBlank()) {
            materialRevision.merge(guid, 1, Integer::sum);
        }
    }

    /**
     * Persists a material document and bumps its revision for program cache invalidation.
     *
     * @param path     material file path
     * @param document document to write
     */
    public void saveMaterial(Path path, MaterialDocument document) throws IOException {
        MaterialSerializer.save(path, document);
        StudioAsset asset = findByPath(path);
        if (asset != null) {
            bumpMaterialRevision(asset.guid());
        }
        refresh();
    }

    /**
     * @param path normalized asset path key
     * @return asset at that path, or null
     */
    public StudioAsset findByPath(Path path) {
        String guid = guidByPath.get(path.normalize());
        return guid == null ? null : get(guid);
    }

    /**
     * @param folderGuid GUID of a folder asset
     * @return immediate child assets, sorted by file name; empty if the folder is unknown
     */
    public List<StudioAsset> children(String folderGuid) {
        StudioAsset folder = byGuid.get(folderGuid);
        if (folder == null || !folder.isFolder()) {
            return List.of();
        }
        List<StudioAsset> children = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.path())) {
            for (Path entry : stream.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase())).toList()) {
                String guid = guidByPath.get(entry.normalize());
                if (guid != null) {
                    children.add(byGuid.get(guid));
                }
            }
        } catch (IOException ex) {
            return List.of();
        }
        return children;
    }

    /** @return GUID of the {@code Assets} root folder */
    public String rootGuid() {
        return guidByPath.get(assetsRoot.normalize());
    }

    /**
     * @param guid asset or folder GUID
     * @return GUID of the containing folder, or the assets root when unknown
     */
    public String parentFolderGuid(String guid) {
        StudioAsset asset = byGuid.get(guid);
        if (asset == null || asset.path().getParent() == null) {
            return rootGuid();
        }
        String parentGuid = guidByPath.get(asset.path().getParent().normalize());
        return parentGuid != null ? parentGuid : rootGuid();
    }

    /**
     * @param guid asset GUID
     * @return indexed asset, or {@code null} if unknown
     */
    public StudioAsset get(String guid) {
        return byGuid.get(guid);
    }

    /**
     * @param path file or folder path under assets
     * @return indexed asset for that path, or {@code null} if not indexed
     */
    public StudioAsset getByPath(Path path) {
        return byGuid.get(guidByPath.get(path.normalize()));
    }

    /** @return snapshot of every indexed asset */
    public List<StudioAsset> allAssets() {
        return new ArrayList<>(byGuid.values());
    }

    /**
     * @param guid asset to highlight in the browser
     */
    public void select(String guid) {
        selectedGuid = guid;
    }

    /** @return currently selected asset, or {@code null} */
    public StudioAsset selected() {
        return selectedGuid == null ? null : byGuid.get(selectedGuid);
    }

    /** Clears the browser selection. */
    public void clearSelection() {
        selectedGuid = null;
    }

    /**
     * @param guid asset whose inspector/details panel should be shown
     */
    public void showInfo(String guid) {
        infoGuid = guid;
    }

    /** Clears the inspector/details target. */
    public void clearInfo() {
        infoGuid = null;
    }

    /** @return asset targeted by the info panel, or {@code null} */
    public StudioAsset infoTarget() {
        return infoGuid == null ? null : byGuid.get(infoGuid);
    }

    /**
     * Loads or returns a cached {@link Texture2d} for a texture asset GUID.
     *
     * @param guid texture asset GUID
     * @return loaded texture, or {@code null} if the GUID is missing or not a texture
     */
    /**
     * @param spriteGuid sprite sub-asset GUID
     * @return slice definition, or {@code null}
     */
    public SpriteDefinition sprite(String spriteGuid) {
        if (spriteGuid == null || spriteGuid.isBlank()) {
            return null;
        }
        return spritesByGuid.get(spriteGuid);
    }

    /**
     * @param textureGuid parent texture GUID
     * @return virtual sprite sub-assets for the texture
     */
    public List<StudioAsset> spriteChildren(String textureGuid) {
        List<String> guids = spriteGuidsByTexture.get(textureGuid);
        if (guids == null || guids.isEmpty()) {
            return List.of();
        }
        List<StudioAsset> children = new ArrayList<>();
        for (String guid : guids) {
            StudioAsset asset = byGuid.get(guid);
            if (asset != null) {
                children.add(asset);
            }
        }
        return children;
    }

    /**
     * @param textureGuid texture asset GUID
     * @return GUID of the default full-image or first slice sprite
     */
    public String defaultSpriteGuid(String textureGuid) {
        if (textureGuid == null || textureGuid.isBlank()) {
            return "";
        }
        String guid = defaultSpriteByTexture.get(textureGuid);
        return guid == null ? "" : guid;
    }

    /**
     * @param spriteGuid sprite sub-asset GUID
     * @return loaded atlas texture, or {@code null}
     */
    public Texture2d textureForSprite(String spriteGuid) {
        SpriteDefinition sprite = sprite(spriteGuid);
        if (sprite == null) {
            return null;
        }
        return texture(sprite.textureGuid());
    }

    /**
     * @param textureGuid texture GUID
     * @return {@code true} when the texture uses multiple slices
     */
    public boolean isMultipleSpriteMode(String textureGuid) {
        StudioAsset asset = byGuid.get(textureGuid);
        if (asset == null || asset.type() != AssetType.TEXTURE) {
            return false;
        }
        try {
            MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, asset.path());
            return "multiple".equals(TextureSpriteData.spriteMode(meta.importer));
        } catch (IOException ex) {
            return spriteChildren(textureGuid).size() > 1;
        }
    }

    /**
     * Persists spritesheet meta for a texture and refreshes the asset index.
     */
    public void saveTextureSprites(Path texturePath, MetaFile.MetaData meta) throws IOException {
        MetaFile.write(projectRoot, assetsRoot, texturePath, meta);
        refresh();
    }

    /**
     * Persists tileset meta for a texture and refreshes the asset index.
     */
    public void saveTileset(Path texturePath, MetaFile.MetaData meta) throws IOException {
        MetaFile.write(projectRoot, assetsRoot, texturePath, meta);
        refresh();
    }

    /**
     * @param textureGuid parent texture GUID
     * @return tileset definition, or {@code null}
     */
    public TilesetDefinition tileset(String textureGuid) {
        if (textureGuid == null || textureGuid.isBlank()) {
            return null;
        }
        return tilesetByTextureGuid.get(textureGuid);
    }

    /**
     * @param spriteGuid sprite slice GUID
     * @return tile metadata for that slice, or {@code null}
     */
    public TileDefinition tileDefinition(String spriteGuid) {
        if (spriteGuid == null || spriteGuid.isBlank()) {
            return null;
        }
        return tileBySpriteGuid.get(spriteGuid);
    }

    /**
     * @param guid animation clip asset GUID
     * @return loaded clip, or {@code null}
     */
    public AnimationClip animationClip(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        AnimationClip cached = clipCache.get(guid);
        if (cached != null) {
            return cached;
        }
        StudioAsset asset = byGuid.get(guid);
        if (asset == null || asset.type() != AssetType.ANIMATION_CLIP) {
            return null;
        }
        try {
            AnimationClip clip = AnimationClipSerializer.load(asset.path());
            clipCache.put(guid, clip);
            return clip;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param path clip file path
     * @param clip clip to persist
     */
    public void saveAnimationClip(Path path, AnimationClip clip) throws IOException {
        AnimationClipSerializer.save(path, clip);
        refresh();
    }

    /**
     * @param guid parent animation asset GUID
     * @return animation set definition, or {@code null}
     */
    public AnimationSetDefinition animationSet(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        AnimationSetDefinition cached = animationSetCache.get(guid);
        if (cached != null) {
            return cached;
        }
        StudioAsset asset = byGuid.get(guid);
        if (asset == null || asset.type() != AssetType.ANIMATION) {
            return null;
        }
        try {
            AnimationSetDefinition set;
            if (Files.isRegularFile(asset.path())) {
                set = AnimationSetSerializer.load(asset.path());
            } else if (resources.isRegistered(guid)) {
                try (AssetRef<byte[]> ref = resources.acquireRaw(guid)) {
                    set = AnimationSetSerializer.loadJson(new String(ref.get(), java.nio.charset.StandardCharsets.UTF_8));
                }
            } else {
                return null;
            }
            animationSetCache.put(guid, set);
            return set;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param path parent animation file path
     * @param set  definition to persist
     */
    public void saveAnimationSet(Path path, AnimationSetDefinition set) throws IOException {
        AnimationSetSerializer.save(path, set);
        refresh();
    }

    /**
     * @param animationGuid parent animation GUID
     * @return clip sub-assets belonging to the animation
     */
    public List<StudioAsset> clipChildren(String animationGuid) {
        List<String> guids = clipGuidsByAnimation.get(animationGuid);
        if (guids == null || guids.isEmpty()) {
            return List.of();
        }
        List<StudioAsset> children = new ArrayList<>();
        for (String guid : guids) {
            StudioAsset asset = byGuid.get(guid);
            if (asset != null) {
                children.add(asset);
            }
        }
        return children;
    }

    /**
     * @param animationGuid parent animation GUID
     * @param stateName     state name
     * @return clip GUID for that state, or {@code null}
     */
    public String stateClipGuid(String animationGuid, String stateName) {
        AnimationSetDefinition set = animationSet(animationGuid);
        if (set == null) {
            return null;
        }
        String resolvedState = stateName == null || stateName.isBlank() ? set.defaultState : stateName;
        String clipGuid = set.clipGuidForState(resolvedState);
        if (clipGuid != null && !clipGuid.isBlank()) {
            return clipGuid;
        }
        return set.clipGuidForState(set.defaultState);
    }

    /**
     * @return all indexed parent animation assets
     */
    public List<StudioAsset> allAnimations() {
        List<StudioAsset> result = new ArrayList<>();
        for (StudioAsset asset : byGuid.values()) {
            if (asset.type() == AssetType.ANIMATION) {
                result.add(asset);
            }
        }
        result.sort(Comparator.comparing(a -> a.displayName().toLowerCase()));
        return result;
    }

    /**
     * @param guid audio asset GUID (.wav / .ogg under {@code Assets})
     * @return decoded OpenAL buffer, or {@code null} when missing or unsupported
     */
    public SoundBuffer soundBuffer(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        AssetRef<SoundBuffer> cached = soundCache.get(guid);
        if (cached != null) {
            return cached.get();
        }
        StudioAsset asset = byGuid.get(guid);
        if (asset == null || asset.type() != AssetType.AUDIO) {
            return null;
        }
        try {
            if (!resources.isRegistered(guid)) {
                resources.registerSoundFile(guid, asset.path());
            }
            AssetRef<SoundBuffer> ref = resources.acquireSound(guid);
            soundCache.put(guid, ref);
            return ref.get();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** Releases cached sound buffers acquired during play mode. */
    public void clearSoundCache() {
        for (AssetRef<SoundBuffer> ref : soundCache.values()) {
            ref.close();
        }
        soundCache.clear();
    }

    /** Releases system fonts acquired for UI text rendering. */
    public void clearUiFontCache() {
        uiFontCache.dispose();
    }

    public Texture2d texture(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        SpriteDefinition sprite = spritesByGuid.get(guid);
        if (sprite != null) {
            return texture(sprite.textureGuid());
        }
        Texture2d cached = textureCache.get(guid);
        if (cached != null) {
            return cached;
        }
        StudioAsset asset = byGuid.get(guid);
        if (asset == null || asset.type() != AssetType.TEXTURE) {
            return null;
        }
        if (!resources.isRegistered(guid)) {
            resources.registerTextureFile(guid, asset.path());
        }
        Texture2d texture = resources.acquireTexture(guid).get();
        applyImportSettings(texture, asset.path());
        textureCache.put(guid, texture);
        return texture;
    }

    /**
     * @param texturePath file under {@code Assets}
     * @return sampling settings from meta, or defaults when missing
     */
    public TextureImportSettings readTextureImportSettings(Path texturePath) {
        StudioAsset asset = getByPath(texturePath);
        if (asset != null) {
            TextureImportSettings cached = textureImportSettingsByGuid.get(asset.guid());
            if (cached != null) {
                return cached.copy();
            }
        }
        try {
            MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, texturePath);
            return TextureSpriteData.readImportSettings(meta.importer);
        } catch (IOException ex) {
            return new TextureImportSettings();
        }
    }

    /**
     * Persists filter/wrap settings and applies them to a loaded texture if cached.
     */
    public void saveTextureImportSettings(Path texturePath, TextureImportSettings settings) throws IOException {
        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, texturePath);
        TextureSpriteData.writeImportSettings(meta.importer, settings);
        MetaFile.write(projectRoot, assetsRoot, texturePath, meta);
        StudioAsset asset = getByPath(texturePath);
        if (asset != null) {
            Texture2d cached = textureCache.get(asset.guid());
            if (cached != null) {
                cached.applySampling(settings.filter, settings.wrap);
            }
        }
    }

    private void applyImportSettings(Texture2d texture, Path texturePath) {
        TextureImportSettings settings = readTextureImportSettings(texturePath);
        texture.applySampling(settings.filter, settings.wrap);
    }

    /**
     * Evicts cached GPU data for a texture and reloads it from disk.
     *
     * @param guid texture asset GUID
     */
    public void reimport(String guid) {
        textureCache.remove(guid);
        StudioAsset asset = byGuid.get(guid);
        if (asset != null && asset.type() == AssetType.TEXTURE) {
            try {
                MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, asset.path());
                TextureSpriteImporter.ensureImported(asset.path(), meta);
                TextureSpriteData.ensureImportDefaults(meta.importer);
                List<SpriteDefinition> sprites = TextureSpriteData.parseSprites(
                        meta.importer, guid, resolveTexturePixelSize(guid, asset.path()).width(),
                        resolveTexturePixelSize(guid, asset.path()).height());
                if (sprites.isEmpty()) {
                    sprites = List.of(TextureSpriteImporter.fullImageSprite(
                            guid, asset.displayName(),
                            resolveTexturePixelSize(guid, asset.path()).width(),
                            resolveTexturePixelSize(guid, asset.path()).height()));
                }
                TilesetData.ensureDefaults(meta.importer, guid, sprites);
                MetaFile.write(projectRoot, assetsRoot, asset.path(), meta);
            } catch (IOException ignored) {
            }
            refresh();
        }
    }

    /** @return absolute path to the project's {@code Assets} directory */
    public Path assetsRoot() {
        return assetsRoot;
    }

    /** @return absolute path to the project root directory */
    public Path projectRoot() {
        return projectRoot;
    }

    /**
     * @param guid asset GUID
     * @return friendly display name, or {@code "(Missing)"} when unknown
     */
    public String displayName(String guid) {
        StudioAsset asset = byGuid.get(guid);
        if (asset == null) {
            return "(Missing)";
        }
        if (asset.type() == AssetType.SPRITE) {
            SpriteDefinition sprite = spritesByGuid.get(guid);
            if (sprite != null && asset.parentTextureGuid() != null) {
                String parent = displayName(asset.parentTextureGuid());
                return sprite.name() + " (" + parent + ")";
            }
        }
        return asset.friendlyDisplayName();
    }

    /**
     * Moves an asset into {@code targetFolder} on disk and refreshes the index.
     *
     * @param guid asset to move
     * @param targetFolder destination folder under {@code Assets}
     * @return {@code true} if the move succeeded
     */
    public boolean moveAsset(String guid, Path targetFolder) {
        StudioAsset asset = byGuid.get(guid);
        if (asset == null) {
            return false;
        }
        if (guid.equals(rootGuid())) {
            return false;
        }
        AssetFileOperations.OperationResult result =
                AssetFileOperations.moveInto(asset.path(), targetFolder, assetsRoot);
        if (!result.success()) {
            return false;
        }
        String movedGuid = result.resultGuids().isEmpty() ? guid : result.resultGuids().get(0);
        refresh();
        selectedGuid = movedGuid;
        infoGuid = movedGuid;
        return true;
    }

    /**
     * Registers a published asset from pack metadata during player bootstrap.
     */
    public void indexPublishedAsset(StudioAsset asset, MetaFile.MetaData meta) throws IOException {
        byGuid.put(asset.guid(), asset);
        guidByPath.put(asset.path().normalize(), asset.guid());
        if (asset.type() == AssetType.TEXTURE) {
            indexPublishedTextureSprites(asset, meta);
        }
    }

    /** Completes published indexing after all metadata entries are registered. */
    public void finalizePublishedIndex() {
        linkOrphanClipsToParentAnimations();
    }

    private void indexPublishedTextureSprites(StudioAsset textureAsset, MetaFile.MetaData meta) throws IOException {
        textureImportSettingsByGuid.put(
                textureAsset.guid(),
                TextureSpriteData.readImportSettings(meta.importer)
        );
        org.llw.render.core.IntSize size = resolveTexturePixelSize(textureAsset.guid(), textureAsset.path());
        List<SpriteDefinition> sprites = TextureSpriteData.parseSprites(
                meta.importer, textureAsset.guid(), size.width(), size.height());
        if (sprites.isEmpty()) {
            SpriteDefinition full = TextureSpriteImporter.fullImageSprite(
                    textureAsset.guid(),
                    textureAsset.displayName(),
                    size.width(),
                    size.height()
            );
            sprites = List.of(full);
        }
        Instant modified = textureAsset.lastModified();
        Set<String> guids = new LinkedHashSet<>();
        String defaultGuid = null;
        for (SpriteDefinition sprite : sprites) {
            if (!guids.add(sprite.guid())) {
                continue;
            }
            spritesByGuid.put(sprite.guid(), sprite);
            StudioAsset sub = new StudioAsset(
                    sprite.guid(),
                    textureAsset.path(),
                    AssetType.SPRITE,
                    sprite.name(),
                    modified,
                    textureAsset.guid()
            );
            byGuid.put(sprite.guid(), sub);
            if (defaultGuid == null) {
                defaultGuid = sprite.guid();
            }
        }
        spriteGuidsByTexture.put(textureAsset.guid(), List.copyOf(new ArrayList<>(guids)));
        if (defaultGuid != null) {
            defaultSpriteByTexture.put(textureAsset.guid(), defaultGuid);
        }
        indexTileset(textureAsset.guid(), meta, sprites);
    }
}
