package org.llw.studio.prefab;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.serialization.PrefabSerializer;
import org.llw.studio.serialization.PrefabSerializer.PrefabData;
import org.llw.studio.serialization.SceneObjectSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Spawns prefab assets or clones an in-scene subtree into a {@link Scene}.
 *
 * <p><b>Coordinates (Y-down):</b> {@code worldX}/{@code worldY} offset or replace the export
 * root's local {@link Transform2DComponent} position.
 */
public final class PrefabInstantiator {
    private PrefabInstantiator() {
    }

    /**
     * Loads a prefab JSON file and parents it under {@code parent} when provided.
     *
     * @param scene                target scene
     * @param assets               asset database for GUID resolution
     * @param projectRoot          project root for relative paths
     * @param prefabGuidOrPath     prefab GUID or path under {@code Assets/}
     * @param parent               optional parent game object
     * @param worldX               world X applied to the export root
     * @param worldY               world Y applied to the export root
     * @param replaceRootPosition  when {@code true}, sets root position; otherwise adds offset
     * @param rootNameOverride     optional name for the export root
     * @return export root game object, or {@code null} when the prefab has no objects
     * @throws IOException when the prefab file cannot be read
     */
    public static GameObject instantiate(
            Scene scene,
            AssetDatabase assets,
            Path projectRoot,
            String prefabGuidOrPath,
            GameObject parent,
            float worldX,
            float worldY,
            boolean replaceRootPosition,
            String rootNameOverride
    ) throws IOException {
        StudioAsset asset = assets == null ? null : assets.get(prefabGuidOrPath);
        if (asset != null && asset.type() != AssetType.PREFAB) {
            throw new IllegalArgumentException("Asset is not a prefab: " + prefabGuidOrPath);
        }
        if (asset == null) {
            Path prefabPath = resolvePrefabPath(assets, projectRoot, prefabGuidOrPath);
            if (prefabPath != null
                    && !Files.isRegularFile(prefabPath)
                    && !prefabPath.getFileName().toString().endsWith(".prefab.json")) {
                throw new IllegalArgumentException("Asset is not a prefab: " + prefabGuidOrPath);
            }
        }
        PrefabData prefab = loadPrefabData(assets, projectRoot, prefabGuidOrPath);
        Map<Integer, GameObject> byPrefabId = new HashMap<>();
        Map<Integer, Integer> idRemap = new HashMap<>();

        for (var objectNode : prefab.objectNodes()) {
            int prefabId = objectNode.path("id").asInt();
            int newSceneId = SceneObjectIds.allocate(scene);
            GameObject object = SceneObjectSerializer.readObject(scene, objectNode, newSceneId);
            byPrefabId.put(prefabId, object);
            idRemap.put(prefabId, newSceneId);
        }

        SceneObjectSerializer.remapScriptEntityRefs(byPrefabId, idRemap);
        SceneObjectSerializer.wireParents(prefab.objectNodes(), byPrefabId);

        GameObject exportRoot = byPrefabId.get(prefab.exportRootId());
        if (exportRoot == null && !byPrefabId.isEmpty()) {
            exportRoot = byPrefabId.values().iterator().next();
        }

        for (var entry : byPrefabId.entrySet()) {
            var objectNode = findNode(prefab, entry.getKey());
            if (objectNode != null && objectNode.path("parentId").asInt(-1) < 0 && parent != null) {
                entry.getValue().setParent(parent, false);
            }
        }

        if (exportRoot != null) {
            if (rootNameOverride != null && !rootNameOverride.isBlank()) {
                exportRoot.setName(rootNameOverride);
            }
            if (replaceRootPosition) {
                exportRoot.transform().x = worldX;
                exportRoot.transform().y = worldY;
            } else if (worldX != 0f || worldY != 0f) {
                exportRoot.transform().x += worldX;
                exportRoot.transform().y += worldY;
            }
        }

        return exportRoot;
    }

    private static PrefabData loadPrefabData(AssetDatabase assets, Path projectRoot, String prefabGuidOrPath)
            throws IOException {
        StudioAsset asset = assets == null ? null : assets.get(prefabGuidOrPath);
        String guid = asset != null ? asset.guid() : prefabGuidOrPath;
        Path prefabPath = resolvePrefabPath(assets, projectRoot, prefabGuidOrPath);
        if (prefabPath != null && Files.isRegularFile(prefabPath)) {
            return PrefabSerializer.load(prefabPath);
        }
        if (assets != null && assets.resourceManager().isRegistered(guid)) {
            try (var ref = assets.resourceManager().acquireRaw(guid)) {
                return PrefabSerializer.loadJson(new String(ref.get(), StandardCharsets.UTF_8));
            }
        }
        throw new IllegalArgumentException("Prefab not found: " + prefabGuidOrPath);
    }

    /**
     * Deep-clones {@code sourceRoot} and its descendants into {@code scene}.
     *
     * @param scene                target scene
     * @param sourceRoot           entity to duplicate
     * @param parent               optional parent for the cloned root
     * @param worldX               world X applied to the cloned root
     * @param worldY               world Y applied to the cloned root
     * @param replaceRootPosition  when {@code true}, sets root position; otherwise adds offset
     * @param rootNameOverride     optional name for the cloned root
     * @return cloned root, or {@code null} when {@code sourceRoot} is not in the scene
     */
    public static GameObject cloneSubtree(
            Scene scene,
            EntityId sourceRoot,
            GameObject parent,
            float worldX,
            float worldY,
            boolean replaceRootPosition,
            String rootNameOverride
    ) {
        GameObject source = scene.find(sourceRoot);
        if (source == null) {
            return null;
        }
        java.util.List<GameObject> subtree = PrefabSerializer.collectSubtree(source);
        Map<EntityId, GameObject> cloneBySource = new HashMap<>();
        Map<Integer, Integer> sceneIdRemap = new HashMap<>();

        for (GameObject src : subtree) {
            GameObject clone = scene.createGameObject(src.name());
            copyComponents(scene, src.entity(), clone);
            cloneBySource.put(src.entity(), clone);
            int oldId = SceneObjectIds.get(scene.world(), src.entity());
            int newId = SceneObjectIds.get(scene.world(), clone.entity());
            if (oldId >= 0 && newId >= 0) {
                sceneIdRemap.put(oldId, newId);
            }
        }

        Map<Integer, GameObject> byOldSceneId = new HashMap<>();
        for (Map.Entry<EntityId, GameObject> entry : cloneBySource.entrySet()) {
            int oldId = SceneObjectIds.get(scene.world(), entry.getKey());
            if (oldId >= 0) {
                byOldSceneId.put(oldId, entry.getValue());
            }
        }
        SceneObjectSerializer.remapScriptEntityRefs(byOldSceneId, sceneIdRemap);

        for (GameObject src : subtree) {
            GameObject srcParent = src.parent();
            GameObject clone = cloneBySource.get(src.entity());
            if (clone == null) {
                continue;
            }
            if (srcParent != null && cloneBySource.containsKey(srcParent.entity())) {
                clone.setParent(cloneBySource.get(srcParent.entity()), false);
            }
        }

        GameObject exportRoot = cloneBySource.get(sourceRoot);
        if (exportRoot != null && parent != null) {
            exportRoot.setParent(parent, false);
        }
        if (exportRoot != null) {
            if (rootNameOverride != null && !rootNameOverride.isBlank()) {
                exportRoot.setName(rootNameOverride);
            }
            if (replaceRootPosition) {
                exportRoot.transform().x = worldX;
                exportRoot.transform().y = worldY;
            } else if (worldX != 0f || worldY != 0f) {
                exportRoot.transform().x += worldX;
                exportRoot.transform().y += worldY;
            }
        }
        return exportRoot;
    }

    private static void copyComponents(Scene scene, EntityId sourceId, GameObject target) {
        NameComponent srcIdentity = scene.world().getComponent(sourceId, NameComponent.class);
        if (srcIdentity != null) {
            target.setName(srcIdentity.name());
            target.setTag(srcIdentity.tag());
        }
        Transform2DComponent srcTransform = scene.world().getComponent(sourceId, Transform2DComponent.class);
        if (srcTransform != null) {
            target.transform().x = srcTransform.x;
            target.transform().y = srcTransform.y;
            target.transform().rotation = srcTransform.rotation;
            target.transform().scaleX = srcTransform.scaleX;
            target.transform().scaleY = srcTransform.scaleY;
        }
        ActiveComponent srcActive = scene.world().getComponent(sourceId, ActiveComponent.class);
        if (srcActive != null) {
            target.getComponent(ActiveComponent.class).selfActive = srcActive.selfActive;
        }
        SpriteRendererComponent srcSprite = scene.world().getComponent(sourceId, SpriteRendererComponent.class);
        if (srcSprite != null) {
            target.addComponent(SpriteRendererComponent.class, srcSprite.copy());
        }
        var srcAnim = scene.world().getComponent(sourceId, org.llw.studio.ecs.components.Animation2DComponent.class);
        if (srcAnim != null) {
            target.addComponent(org.llw.studio.ecs.components.Animation2DComponent.class, srcAnim.copy());
        }
        ScriptComponent srcScript = scene.world().getComponent(sourceId, ScriptComponent.class);
        if (srcScript != null) {
            target.addComponent(ScriptComponent.class, srcScript.copy());
        }
        Camera2DComponent srcCamera = scene.world().getComponent(sourceId, Camera2DComponent.class);
        if (srcCamera != null) {
            target.addComponent(Camera2DComponent.class, srcCamera.copy());
        }
        AudioSourceComponent srcAudio = scene.world().getComponent(sourceId, AudioSourceComponent.class);
        if (srcAudio != null) {
            target.addComponent(AudioSourceComponent.class, srcAudio.copy());
        }
        Rigidbody2DComponent srcRb = scene.world().getComponent(sourceId, Rigidbody2DComponent.class);
        if (srcRb != null) {
            target.addComponent(Rigidbody2DComponent.class, srcRb.copy());
        }
        BoxCollider2DComponent srcBox = scene.world().getComponent(sourceId, BoxCollider2DComponent.class);
        if (srcBox != null) {
            target.addComponent(BoxCollider2DComponent.class, srcBox.copy());
        }
        CircleCollider2DComponent srcCircle = scene.world().getComponent(sourceId, CircleCollider2DComponent.class);
        if (srcCircle != null) {
            target.addComponent(CircleCollider2DComponent.class, srcCircle.copy());
        }
        EdgeCollider2DComponent srcEdge = scene.world().getComponent(sourceId, EdgeCollider2DComponent.class);
        if (srcEdge != null) {
            target.addComponent(EdgeCollider2DComponent.class, srcEdge.copy());
        }
    }

    /**
     * Resolves a prefab GUID or sanitized {@code Assets/} path to an on-disk file.
     *
     * @return absolute prefab path, or {@code null} when not found or outside {@code Assets/}
     */
    public static Path resolvePrefabPath(AssetDatabase assets, Path projectRoot, String prefabGuidOrPath) {
        if (prefabGuidOrPath == null || prefabGuidOrPath.isBlank()) {
            return null;
        }
        StudioAsset byGuid = assets == null ? null : assets.get(prefabGuidOrPath);
        if (byGuid != null) {
            return byGuid.path();
        }
        Path candidate = projectRoot.resolve(sanitizeAssetPath(prefabGuidOrPath)).normalize();
        Path assetsRoot = projectRoot.resolve("Assets").normalize();
        if (!candidate.startsWith(assetsRoot)) {
            return null;
        }
        return candidate;
    }

    private static com.fasterxml.jackson.databind.JsonNode findNode(PrefabData prefab, int id) {
        for (var node : prefab.objectNodes()) {
            if (node.path("id").asInt() == id) {
                return node;
            }
        }
        return null;
    }

    private static String sanitizeAssetPath(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Asset path must stay within Assets/: " + path);
        }
        if (!normalized.startsWith("Assets/")) {
            normalized = "Assets/" + normalized;
        }
        return normalized;
    }
}
