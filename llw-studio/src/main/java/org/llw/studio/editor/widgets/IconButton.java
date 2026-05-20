package org.llw.studio.editor.widgets;

import imgui.ImGui;

/**
 * Square icon button; {@code id} is used as ImGui ID and hover tooltip text.
 */
public final class IconButton {
  private IconButton() {}

  public static boolean render(String id, String label, float size) {
    ImGui.pushID(id);
    boolean clicked = ImGui.button(label, size, size);
    if (ImGui.isItemHovered() && id != null && !id.isBlank()) {
      ImGui.setTooltip(id);
    }
    ImGui.popID();
    return clicked;
  }
}
