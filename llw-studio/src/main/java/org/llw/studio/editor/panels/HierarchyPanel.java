package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorDragDrop;
import org.llw.studio.editor.prefab.PrefabEditorActions;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.HierarchyTree;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.widgets.IconButton;
import org.llw.studio.editor.widgets.SearchInput;
import org.llw.studio.scene.GameObject;

/**
 * Scene hierarchy tree with search, create-empty, and prefab drag-drop.
 */
public final class HierarchyPanel implements EditorPanel {
  private static final float ICON_BUTTON_SIZE = 22f;
  private static final float FOOTER_HEIGHT = 20f;

  private final SelectionService selection;
  private final AssetDatabase assets;
  private final HierarchyTree tree = new HierarchyTree();
  private final ImString searchBuffer = new ImString("", 256);

  /**
   * @param selection entity selection updated by the tree
   * @param assets    assets for prefab instantiation
   */
  public HierarchyPanel(SelectionService selection, AssetDatabase assets) {
    this.selection = selection;
    this.assets = assets;
  }

  /** {@inheritDoc} */
  @Override
  public String id() {
    return "hierarchy";
  }

  /** {@inheritDoc} */
  @Override
  public String title() {
    return "Hierarchy";
  }

  /** {@inheritDoc} */
  @Override
  public void render(StudioContext context) {
    if (!ImGui.begin(title())) {
      ImGui.end();
      return;
    }

    float searchWidth = ImGui.getContentRegionAvailX() - ICON_BUTTON_SIZE - 6f;
    SearchInput.render("##hierarchy_search", searchBuffer, "Search", searchWidth);
    ImGui.sameLine();
    if (IconButton.render("Create Empty", EditorIcons.PLUS, ICON_BUTTON_SIZE) && !context.isPlaying()) {
      GameObject created = context.editScene().createGameObject("GameObject");
      selection.select(created.entity());
    }

    ImGui.separator();

    tree.setFilter(searchBuffer.get());
    float treeHeight = ImGui.getContentRegionAvailY() - (selection.count() > 1 ? FOOTER_HEIGHT : 0f);
    ImGui.beginChild("##hierarchy_tree", 0f, treeHeight, false);
    // Mark hierarchy as drag source so inspector stays on pre-drag entity for drop targets.
    if (EditorDragDrop.isActive()) {
      EditorDragDrop.markHierarchyDragFrame();
    }
    tree.render(context, selection, assets);
    acceptPrefabDrop(context);
    ImGui.endChild();

    if (selection.count() > 1) {
      EditorStyle.pushMutedText();
      ImGui.text(selection.count() + " selected");
      EditorStyle.popMutedText();
    }

    ImGui.end();
  }

  private void acceptPrefabDrop(StudioContext context) {
    if (context.isPlaying()) {
      return;
    }
    if (ImGui.beginDragDropTarget()) {
      String guid = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
      if (guid != null) {
        // Drop on tree background instantiates at origin; row-specific drops pass cursor world coords elsewhere.
        PrefabEditorActions.tryInstantiatePrefab(
                context, assets, selection, guid, null, 0f, 0f, false, false);
      }
      ImGui.endDragDropTarget();
    }
  }
}
