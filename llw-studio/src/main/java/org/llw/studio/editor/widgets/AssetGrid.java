package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.assets.AssetBrowserNest;
import org.llw.studio.editor.assets.AssetBrowserNest.NestChild;
import org.llw.studio.editor.assets.AssetBrowserRename;
import org.llw.studio.editor.assets.EditorIconRegistry;
import org.llw.studio.editor.assets.AssetEditorActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Multi-column icon grid for the project browser with selection, drag sources, and folder drop targets.
 */
public final class AssetGrid {
  private AssetGrid() {}

  public sealed interface GridSlot permits ParentSlot, ChildSlot, UpSlot {
  }

  public record UpSlot() implements GridSlot {}

  public record ParentSlot(StudioAsset asset, AssetBrowserNest.NestableEntry nest) implements GridSlot {}

  public record ChildSlot(NestChild child) implements GridSlot {}

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect
  ) {
    render(assets, previews, folderGuid, onSelect, asset -> {});
  }

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect,
      Consumer<StudioAsset> onShowInfo
  ) {
    render(assets, previews, folderGuid, onSelect, onShowInfo, null);
  }

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect,
      Consumer<StudioAsset> onShowInfo,
      Consumer<StudioAsset> onCreateScript
  ) {
    render(assets, previews, folderGuid, onSelect, onShowInfo, onCreateScript, null, null, null, null, null, null, null);
  }

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect,
      Consumer<StudioAsset> onShowInfo,
      Consumer<StudioAsset> onCreateScript,
      StudioContext context,
      SelectionService selection
  ) {
    render(assets, previews, folderGuid, onSelect, onShowInfo, onCreateScript, context, selection, null, null, null, null, null, null);
  }

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect,
      Consumer<StudioAsset> onShowInfo,
      Consumer<StudioAsset> onCreateScript,
      StudioContext context,
      SelectionService selection,
      AssetEditorActions assetActions,
      Consumer<StudioAsset> onRequestDeleteConfirm,
      AssetBrowserRename rename,
      EditorIconRegistry icons,
      Map<String, Boolean> expandedByGuid
  ) {
    render(
            assets,
            previews,
            folderGuid,
            onSelect,
            onShowInfo,
            onCreateScript,
            context,
            selection,
            assetActions,
            onRequestDeleteConfirm,
            rename,
            icons,
            expandedByGuid,
            null
    );
  }

  public static void render(
      AssetDatabase assets,
      AssetPreviewCache previews,
      String folderGuid,
      Consumer<StudioAsset> onSelect,
      Consumer<StudioAsset> onShowInfo,
      Consumer<StudioAsset> onCreateScript,
      StudioContext context,
      SelectionService selection,
      AssetEditorActions assetActions,
      Consumer<StudioAsset> onRequestDeleteConfirm,
      AssetBrowserRename rename,
      EditorIconRegistry icons,
      Map<String, Boolean> expandedByGuid,
      AssetBrowserItemInteractions.Context interactions
  ) {
    if (icons == null) {
      return;
    }
    AssetBrowserFolderDropState.beginFrame();
    float panelWidth = ImGui.getContentRegionAvailX();
    float cell = AssetGridCell.CELL_SIZE;
    int columns = Math.max(1, (int) (panelWidth / cell));
    List<GridSlot> slots = buildSlots(assets, folderGuid, expandedByGuid);
    ImGui.columns(columns, "assetGrid", false);
    for (GridSlot slot : slots) {
      if (slot instanceof UpSlot) {
        renderUpCell(assets, folderGuid, interactions);
      } else if (slot instanceof ParentSlot parentSlot) {
        renderParentSlot(
                assets,
                previews,
                icons,
                parentSlot,
                expandedByGuid,
                onSelect,
                rename,
                interactions
        );
      } else if (slot instanceof ChildSlot childSlot) {
        renderChildSlot(
                assets,
                previews,
                icons,
                childSlot.child(),
                onSelect,
                rename,
                interactions
        );
      }
      ImGui.nextColumn();
    }
    ImGui.columns(1);
    if (interactions != null) {
      AssetBrowserFolderDropState.submitTargets(
              context,
              selection,
              assets,
              interactions.onAssetsChanged()
      );
    }
  }

  private static List<GridSlot> buildSlots(
      AssetDatabase assets,
      String folderGuid,
      Map<String, Boolean> expandedByGuid
  ) {
    List<GridSlot> slots = new ArrayList<>();
    if (!folderGuid.equals(assets.rootGuid())) {
      slots.add(new UpSlot());
    }
    for (StudioAsset asset : AssetBrowserNest.childrenOf(assets, folderGuid)) {
      AssetBrowserNest.NestableEntry nest = AssetBrowserNest.entryFor(assets, asset);
      slots.add(new ParentSlot(asset, nest));
      if (nest.expandable() && Boolean.TRUE.equals(expandedByGuid.get(asset.guid()))) {
        for (NestChild child : nest.children()) {
          slots.add(new ChildSlot(child));
        }
      }
    }
    return slots;
  }

  private static void renderUpCell(
      AssetDatabase assets,
      String folderGuid,
      AssetBrowserItemInteractions.Context interactions
  ) {
    ImGui.pushID("grid_up");
    if (ImGui.button("..\nUp", AssetGridCell.CELL_SIZE - 8f, AssetGridCell.CELL_SIZE - 8f)) {
      String parent = assets.parentFolderGuid(folderGuid);
      StudioAsset parentAsset = assets.get(parent);
      if (parentAsset != null && interactions != null && interactions.onEnterFolder() != null) {
        interactions.onEnterFolder().accept(parentAsset);
      }
    }
    ImGui.popID();
  }

  private static void renderParentSlot(
      AssetDatabase assets,
      AssetPreviewCache previews,
      EditorIconRegistry icons,
      ParentSlot parentSlot,
      Map<String, Boolean> expandedByGuid,
      Consumer<StudioAsset> onSelect,
      AssetBrowserRename rename,
      AssetBrowserItemInteractions.Context interactions
  ) {
    StudioAsset asset = parentSlot.asset();
    ImGui.pushID(asset.guid());
    boolean selected = assets.selected() == asset;
    boolean expanded = Boolean.TRUE.equals(expandedByGuid.get(asset.guid()));
    if (rename != null && !asset.isFolder() && rename.isEditing(asset.guid())) {
      rename.renderInline(asset, assets, interactions == null ? null : interactions.assetActions());
      ImGui.popID();
      return;
    }
    boolean clicked = AssetGridCell.render(
            asset,
            null,
            org.llw.studio.editor.assets.EditorIconRegistry.kindFor(asset),
            selected,
            parentSlot.nest().expandable(),
            expanded,
            () -> expandedByGuid.put(
                    asset.guid(),
                    !Boolean.TRUE.equals(expandedByGuid.get(asset.guid()))
            ),
            assets,
            previews,
            icons,
            interactions
    );
    if (clicked) {
      boolean wasSelected = selected;
      onSelect.accept(asset);
      if (!asset.isFolder() && rename != null) {
        rename.onAssetClicked(asset, wasSelected);
      }
    }
    ImGui.popID();
  }

  private static void renderChildSlot(
      AssetDatabase assets,
      AssetPreviewCache previews,
      EditorIconRegistry icons,
      NestChild child,
      Consumer<StudioAsset> onSelect,
      AssetBrowserRename rename,
      AssetBrowserItemInteractions.Context interactions
  ) {
    StudioAsset asset = child.asset();
    ImGui.pushID("nest_" + asset.guid());
    boolean selected = assets.selected() == asset;
    if (rename != null && rename.isEditing(asset.guid())) {
      rename.renderInline(asset, assets, interactions == null ? null : interactions.assetActions());
      ImGui.popID();
      return;
    }
    boolean clicked = AssetGridCell.render(
            asset,
            child.displayLabel(),
            child.iconKind(),
            selected,
            false,
            false,
            () -> {},
            assets,
            previews,
            icons,
            interactions
    );
    if (clicked) {
      boolean wasSelected = selected;
      onSelect.accept(asset);
      if (rename != null) {
        rename.onAssetClicked(asset, wasSelected);
      }
    }
    ImGui.popID();
  }
}
