package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.prefab.PrefabEditorActions;
import org.llw.studio.ecs.EntityId;

import java.nio.file.Path;

/**
 * Drag-drop targets on project folders: save entity as prefab or move an asset into the folder.
 */
public final class AssetDropTargets {
    private AssetDropTargets() {
    }

    public static void acceptFolderDrops(
            StudioContext context,
            SelectionService selection,
            AssetDatabase assets,
            Path folder
    ) {
        acceptFolderDrops(context, selection, assets, folder, null);
    }

    public static void acceptFolderDrops(
            StudioContext context,
            SelectionService selection,
            AssetDatabase assets,
            Path folder,
            Runnable onAssetsChanged
    ) {
        if (folder == null || assets == null) {
            return;
        }
        if (context != null && context.isPlaying()) {
            return;
        }
        if (!ImGui.beginDragDropTarget()) {
            return;
        }
        // Entity drop: save selection as prefab in this folder.
        String entityPayload = ImGui.acceptDragDropPayload(
                SelectionService.PAYLOAD_ENTITY,
                ImGuiDragDropFlags.AcceptNoDrawDefaultRect,
                String.class
        );
        if (entityPayload != null && context != null) {
            EntityId entity = PrefabEditorActions.parseEntityPayload(entityPayload);
            String prefabGuid = PrefabEditorActions.trySavePrefabFromEntity(context, assets, entity, folder);
            if (prefabGuid != null) {
                notifyChanged(selection, onAssetsChanged);
            }
        }
        // Asset drop: move file into folder (no-op when already parented here or dropping folder onto itself).
        String assetGuid = ImGui.acceptDragDropPayload(
                AssetDatabase.PAYLOAD_ASSET_GUID,
                ImGuiDragDropFlags.AcceptNoDrawDefaultRect,
                String.class
        );
        if (assetGuid != null && canMoveAssetToFolder(assets, assetGuid, folder)
                && PrefabEditorActions.tryMoveAssetToFolder(assets, assetGuid, folder)) {
            notifyChanged(selection, onAssetsChanged);
        }
        ImGui.endDragDropTarget();
    }

    private static boolean canMoveAssetToFolder(AssetDatabase assets, String assetGuid, Path folder) {
        StudioAsset asset = assets.get(assetGuid);
        if (asset == null || folder == null) {
            return false;
        }
        if (asset.isFolder() && asset.path().normalize().equals(folder.normalize())) {
            return false;
        }
        Path parent = asset.path().getParent();
        return parent == null || !parent.normalize().equals(folder.normalize());
    }

    private static void notifyChanged(SelectionService selection, Runnable onAssetsChanged) {
        if (onAssetsChanged != null) {
            onAssetsChanged.run();
        }
        // Clear scene selection after asset-tree mutation so inspector does not show stale entity.
        if (selection != null) {
            selection.clear();
        }
    }
}
