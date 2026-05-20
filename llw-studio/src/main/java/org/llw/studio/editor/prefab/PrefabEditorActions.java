package org.llw.studio.editor.prefab;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.prefab.PrefabInstantiator;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.serialization.PrefabSerializer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Static helpers to instantiate, save, and move prefab assets from editor UI.
 */
public final class PrefabEditorActions {
    private PrefabEditorActions() {
    }

    /**
     * Instantiates a prefab into a scene at a world position.
     *
     * @param intoActiveScene when true, uses {@link StudioContext#activeScene()} (play scene while playing)
     * @return root game object, or null on failure
     */
    public static GameObject tryInstantiatePrefab(
            StudioContext context,
            AssetDatabase assets,
            SelectionService selection,
            String guid,
            GameObject parent,
            float worldX,
            float worldY,
            boolean replaceRootPosition,
            boolean intoActiveScene
    ) {
        if (context == null || guid == null || guid.isBlank()) {
            return null;
        }
        if (!intoActiveScene && context.isPlaying()) {
            return null;
        }
        StudioAsset asset = assets.get(guid);
        if (asset == null || asset.type() != AssetType.PREFAB) {
            return null;
        }
        Scene scene = intoActiveScene ? context.activeScene() : context.editScene();
        if (scene == null) {
            return null;
        }
        try {
            GameObject root = PrefabInstantiator.instantiate(
                    scene,
                    assets,
                    context.projectRoot(),
                    guid,
                    parent,
                    worldX,
                    worldY,
                    replaceRootPosition,
                    null
            );
            if (root != null && selection != null && !intoActiveScene) {
                selection.select(root.entity());
                assets.clearSelection();
            }
            return root;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Saves a scene entity subtree as a new prefab file under {@code folder}.
     *
     * @return new prefab asset GUID, or null on failure
     */
    public static String trySavePrefabFromEntity(
            StudioContext context,
            AssetDatabase assets,
            EntityId entityId,
            Path folder
    ) {
        if (context == null || context.isPlaying() || entityId == null || entityId.isNone() || folder == null) {
            return null;
        }
        GameObject object = context.editScene().find(entityId);
        if (object == null) {
            return null;
        }
        String baseName = object.name();
        if (baseName == null || baseName.isBlank()) {
            baseName = "GameObject";
        }
        try {
            Path path = uniquePrefabPath(folder, baseName);
            PrefabSerializer.saveSubtree(context.editScene(), object.entity(), path);
            assets.refresh();
            MetaFile.MetaData meta = MetaFile.read(
                    assets.projectRoot(), assets.assetsRoot(), path);
            assets.select(meta.guid);
            return meta.guid;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Parses an ImGui drag-drop entity payload ({@code index:generation}).
     *
     * @param payload payload string
     * @return entity id, or {@link EntityId#none()}
     */
    public static EntityId parseEntityPayload(String payload) {
        if (payload == null || !payload.contains(":")) {
            return EntityId.none();
        }
        String[] parts = payload.split(":");
        return new EntityId(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * @return true if the asset was moved under {@code folder}
     */
    public static boolean tryMoveAssetToFolder(AssetDatabase assets, String assetGuid, Path folder) {
        if (assets == null || assetGuid == null || assetGuid.isBlank() || folder == null) {
            return false;
        }
        StudioAsset asset = assets.get(assetGuid);
        if (asset == null || assetGuid.equals(assets.rootGuid())) {
            return false;
        }
        try {
            return assets.moveAsset(assetGuid, folder);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Path uniquePrefabPath(Path folder, String baseName) throws java.io.IOException {
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        if (sanitized.isEmpty()) {
            sanitized = "GameObject";
        }
        Path candidate = folder.resolve(sanitized + ".prefab.json");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = folder.resolve(sanitized + " " + suffix + ".prefab.json");
            suffix++;
        }
        return candidate;
    }
}
