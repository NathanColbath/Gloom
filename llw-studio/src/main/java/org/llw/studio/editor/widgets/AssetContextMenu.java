package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.assets.AssetEditorActions;

import java.util.function.Consumer;

/**
 * Right-click context menu for a project asset (copy/cut/paste, delete, create script, info).
 */
public final class AssetContextMenu {
    private AssetContextMenu() {
    }

    public static void render(
            StudioContext context,
            AssetEditorActions assetActions,
            StudioAsset asset,
            String pasteFolderGuid,
            Consumer<StudioAsset> onShowInfo,
            Consumer<StudioAsset> onCreateScript,
            Consumer<StudioAsset> onCreateFolder,
            Runnable onRequestDeleteConfirm
    ) {
        String popupId = "asset_ctx##" + asset.guid();
        if (!ImGui.beginPopupContextItem(popupId)) {
            return;
        }
        boolean blocked = context != null && context.isPlaying();
        boolean isRoot = assetActions.isRootAsset(asset);
        boolean editable = !blocked && !isRoot;
        boolean canPaste = assetActions.clipboard().canPaste();

        if (!isRoot) {
            if (ImGui.menuItem("Copy", "", false, editable)) {
                assetActions.copy(asset);
            }
            if (ImGui.menuItem("Cut", "", false, editable)) {
                assetActions.cut(asset);
            }
            if (ImGui.menuItem("Paste", "", false, editable && canPaste)) {
                assetActions.pasteIntoFolder(pasteFolderGuid);
            }
            if (ImGui.menuItem("Duplicate", "", false, editable)) {
                assetActions.duplicate(asset);
            }
            if (ImGui.menuItem("Delete", "", false, editable)) {
                onRequestDeleteConfirm.run();
            }
            ImGui.separator();
        }

        if (asset.isFolder()) {
            if (onCreateFolder != null && ImGui.menuItem("Create Folder", "", false, !blocked)) {
                onCreateFolder.accept(asset);
            }
            if (onCreateScript != null && ImGui.menuItem("Create Script", "", false, !blocked)) {
                onCreateScript.accept(asset);
            }
        }
        if (ImGui.menuItem("Info")) {
            onShowInfo.accept(asset);
        }
        ImGui.endPopup();
    }
}
