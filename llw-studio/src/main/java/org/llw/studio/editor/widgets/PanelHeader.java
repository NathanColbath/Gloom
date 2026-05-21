package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Shared panel chrome: tightened spacing, separator, search field, and right-aligned buttons.
 */
public final class PanelHeader {
  private PanelHeader() {}

  public static void begin() {
    EditorStyle.pushPanelHeaderSpacing();
  }

  public static void end() {
    EditorStyle.popPanelHeaderSpacing();
    ImGui.separator();
  }

  public static boolean searchField(String id, ImString buffer, String hint) {
    return SearchInput.render(id, buffer, hint, -1f);
  }

  public static boolean smallButtonRight(String label, float width) {
    float x = ImGui.getWindowContentRegionMaxX() - width;
    if (x > ImGui.getCursorPosX()) {
      ImGui.sameLine(x);
    }
    return ImGui.button(label, width, 0f);
  }
}
