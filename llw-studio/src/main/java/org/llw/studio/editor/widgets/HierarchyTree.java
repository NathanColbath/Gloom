package org.llw.studio.editor.widgets;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.scene.GameObject;

/**
 * Renders the scene hierarchy as a filtered tree of {@link GameObject} nodes.
 */
public final class HierarchyTree {
  private String filter = "";

  /** Sets case-insensitive substring filter applied to object names. */
  public void setFilter(String filter) {
    this.filter = filter == null ? "" : filter.toLowerCase();
  }

  public void render(StudioContext context, SelectionService selection, AssetDatabase assets) {
    for (GameObject root : context.activeScene().rootObjects()) {
      drawNode(context, selection, assets, root);
    }
  }

  private void drawNode(StudioContext context, SelectionService selection, AssetDatabase assets, GameObject object) {
    HierarchyRow.DrawResult result = HierarchyRow.draw(context, selection, assets, object, filter);
    if (result.recurseChildren()) {
      for (GameObject child : object.children()) {
        drawNode(context, selection, assets, child);
      }
    }
    if (result.treePopAfter()) {
      imgui.ImGui.treePop();
    }
  }
}
