package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Two-column inspector rows: fixed-width label on the left, control filling the remainder.
 */
public final class PropertyRow {
  private PropertyRow() {}

  public static void begin(String label) {
    ImGui.pushID(label);
    ImGui.textUnformatted(label);
    ImGui.sameLine(EditorColors.INSPECTOR_LABEL_WIDTH);
    ImGui.setNextItemWidth(-1f);
  }

  public static void end() {
    ImGui.popID();
  }

  public static void label(String label) {
    ImGui.textUnformatted(label);
    ImGui.sameLine(EditorColors.INSPECTOR_LABEL_WIDTH);
  }

  public static void beginControlColumn() {
    ImGui.setNextItemWidth(-1f);
  }

  public static float controlColumnStart() {
    return EditorColors.INSPECTOR_LABEL_WIDTH;
  }

  public static void readOnlyValue(String label, String value) {
    begin(label);
    float maxWidth = ImGui.getContentRegionAvailX();
    String display = EditorStyle.truncate(value == null ? "" : value, maxWidth);
    ImGui.textUnformatted(display);
    if (value != null && !value.equals(display) && ImGui.isItemHovered()) {
      ImGui.setTooltip(value);
    }
    end();
  }
}
