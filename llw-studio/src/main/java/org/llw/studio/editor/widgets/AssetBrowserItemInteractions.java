package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.assets.AssetEditorActions;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Shared drag-drop, context menu, and double-click behavior for project browser cells.
 */
public final class AssetBrowserItemInteractions {
    private AssetBrowserItemInteractions() {
    }

    /**
     * Callbacks for grid/list asset row interactions.
     */
    public record Context(
            StudioContext studioContext,
            SelectionService selection,
            AssetDatabase assets,
            AssetEditorActions assetActions,
            String pasteFolderGuid,
            Consumer<StudioAsset> onShowInfo,
            Consumer<StudioAsset> onCreateScript,
            Consumer<StudioAsset> onCreateFolder,
            Consumer<StudioAsset> onRequestDeleteConfirm,
            Consumer<StudioAsset> onEnterFolder,
            Consumer<Path> onOpenScene,
            Consumer<Path> onOpenScript,
            Consumer<StudioAsset> onOpenShaderGraph,
            Runnable onAssetsChanged
    ) {
        public Context(
                StudioContext studioContext,
                SelectionService selection,
                AssetDatabase assets,
                AssetEditorActions assetActions,
                String pasteFolderGuid,
                Consumer<StudioAsset> onShowInfo,
                Consumer<StudioAsset> onCreateScript,
                Consumer<StudioAsset> onRequestDeleteConfirm,
                Consumer<StudioAsset> onEnterFolder,
                Consumer<Path> onOpenScene,
                Consumer<Path> onOpenScript
        ) {
            this(
                    studioContext,
                    selection,
                    assets,
                    assetActions,
                    pasteFolderGuid,
                    onShowInfo,
                    onCreateScript,
                    null,
                    onRequestDeleteConfirm,
                    onEnterFolder,
                    onOpenScene,
                    onOpenScript,
                    null,
                    null
            );
        }
    }

    /**
     * Attaches to the last ImGui item (the cell hit target). Call immediately after {@code invisibleButton}.
     */
    public static void attach(StudioAsset asset, Context context) {
        attach(asset, context, true);
    }

    /**
     * @param acceptFolderDrops when false, folder move targets are omitted (grid defers them)
     */
    public static void attach(StudioAsset asset, Context context, boolean acceptFolderDrops) {
        if (asset == null || context == null) {
            return;
        }
        if (context.assetActions() != null && context.onRequestDeleteConfirm() != null) {
            AssetContextMenu.render(
                    context.studioContext(),
                    context.assetActions(),
                    asset,
                    context.pasteFolderGuid(),
                    context.onShowInfo(),
                    context.onCreateScript(),
                    context.onCreateFolder(),
                    () -> context.onRequestDeleteConfirm().accept(asset)
            );
        }
        dragAsset(asset);
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
            if (asset.isFolder()) {
                if (context.onEnterFolder() != null) {
                    context.onEnterFolder().accept(asset);
                }
            } else if (asset.type() == AssetType.SCENE) {
                if (context.onOpenScene() != null) {
                    context.onOpenScene().accept(asset.path());
                }
            } else if (asset.type() == AssetType.SCRIPT) {
                if (context.onOpenScript() != null) {
                    context.onOpenScript().accept(asset.path());
                }
            } else if (asset.type() == AssetType.SHADER_GRAPH) {
                if (context.onOpenShaderGraph() != null) {
                    context.onOpenShaderGraph().accept(asset);
                }
            }
        }
        if (acceptFolderDrops && asset.isFolder()) {
            AssetDropTargets.acceptFolderDrops(
                    context.studioContext(),
                    context.selection(),
                    context.assets(),
                    asset.path(),
                    context.onAssetsChanged()
            );
        }
    }

    private static void dragAsset(StudioAsset asset) {
        int dragFlags = ImGuiDragDropFlags.SourceAllowNullID | ImGuiDragDropFlags.SourceNoDisableHover;
        if (ImGui.beginDragDropSource(dragFlags)) {
            ImGui.setDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, asset.guid());
            ImGui.text(asset.friendlyDisplayName());
            ImGui.endDragDropSource();
        }
    }
}
