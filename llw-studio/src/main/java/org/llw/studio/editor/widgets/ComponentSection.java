package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Framed collapsing section for grouped inspector content (legacy layout helper).
 */
public final class ComponentSection {
  private ComponentSection() {}

  public static boolean begin(String title, boolean defaultOpen) {
    int flags = ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.SpanAvailWidth;
    if (defaultOpen) {
      flags |= ImGuiTreeNodeFlags.DefaultOpen;
    }
    EditorStyle.pushPanelChrome();
    boolean open = ImGui.collapsingHeader(title, flags);
    EditorStyle.popPanelChrome();
    return open;
  }

  public static boolean removeButton(String id) {
    float width = 56f;
    float x = ImGui.getWindowContentRegionMaxX() - width;
    if (x > ImGui.getCursorPosX()) {
      ImGui.sameLine(x);
    }
    EditorStyle.pushDangerText();
    boolean clicked = ImGui.smallButton("Remove##" + id);
    EditorStyle.popDangerText();
    return clicked;
  }
}
