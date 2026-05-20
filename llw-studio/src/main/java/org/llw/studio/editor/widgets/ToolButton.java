package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.llw.studio.editor.theme.EditorColors;

/**
 * Fixed-width toolbar buttons for scene tools (toggle highlights the active mode).
 */
public final class ToolButton {
  private ToolButton() {}

  public static boolean toggle(String label, boolean active, float width) {
    if (active) {
      ImGui.pushStyleColor(ImGuiCol.Button, EditorColors.ACCENT[0], EditorColors.ACCENT[1], EditorColors.ACCENT[2],
          EditorColors.ACCENT[3]);
      ImGui.pushStyleColor(ImGuiCol.ButtonHovered, EditorColors.ACCENT_HOVER[0], EditorColors.ACCENT_HOVER[1],
          EditorColors.ACCENT_HOVER[2], EditorColors.ACCENT_HOVER[3]);
    }
    boolean clicked = ImGui.button(label, width, 0f);
    if (active) {
      ImGui.popStyleColor(2);
    }
    return clicked;
  }

  public static boolean action(String label, float width) {
    return ImGui.button(label, width, 0f);
  }
}
