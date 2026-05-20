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

        if (blocked) {
            ImGui.beginDisabled();
        }
        if (!isRoot) {
            if (ImGui.menuItem("Copy")) {
                assetActions.copy(asset);
            }
            if (ImGui.menuItem("Cut")) {
                assetActions.cut(asset);
            }
            if (!assetActions.clipboard().canPaste()) {
                ImGui.beginDisabled();
            }
            if (ImGui.menuItem("Paste")) {
                assetActions.pasteIntoFolder(pasteFolderGuid);
            }
            if (!assetActions.clipboard().canPaste()) {
                ImGui.endDisabled();
            }
            if (ImGui.menuItem("Duplicate")) {
                assetActions.duplicate(asset);
            }
            if (ImGui.menuItem("Delete")) {
                onRequestDeleteConfirm.run();
            }
            ImGui.separator();
        }
        if (blocked) {
            ImGui.endDisabled();
        }

        if (asset.isFolder()) {
            if (onCreateFolder != null && ImGui.menuItem("Create Folder")) {
                onCreateFolder.accept(asset);
            }
            if (onCreateScript != null && ImGui.menuItem("Create Script")) {
                onCreateScript.accept(asset);
            }
        }
        if (ImGui.menuItem("Info")) {
            onShowInfo.accept(asset);
        }
        ImGui.endPopup();
    }
}
