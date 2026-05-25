package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Two-column inspector rows: fixed-width label on the left, control filling the remainder.
 *
 * <p>Labels longer than {@link #MAX_LABEL_CHARS} characters are end-truncated with
 * {@code ...} and the full text is shown on hover via tooltip.
 */
public final class PropertyRow {
  private PropertyRow() {}

  /**
   * Maximum characters for labels before truncation with ellipsis.
   * Approximately matches {@code INSPECTOR_LABEL_WIDTH} (110px) at ~8px per character.
   */
  public static final int MAX_LABEL_CHARS = 14;

  public static void begin(String label) {
    ImGui.pushID(label);
    PropertyRowFormatter.FormatResult fmt = PropertyRowFormatter.formatLabel(label, MAX_LABEL_CHARS);
    ImGui.textUnformatted(fmt.display);
    if (fmt.tooltip != null && ImGui.isItemHovered()) {
      ImGui.setTooltip(fmt.tooltip);
    }
    ImGui.sameLine(EditorColors.INSPECTOR_LABEL_WIDTH);
    ImGui.setNextItemWidth(-1f);
  }

  public static void end() {
    ImGui.popID();
  }

  /**
   * Draws a read-only sub-label in the value column (e.g. muted axis labels).
   * Does <b>not</b> push a new row — use within an existing row's value area.
   */
  public static void label(String label) {
    PropertyRowFormatter.FormatResult fmt = PropertyRowFormatter.formatLabel(label, MAX_LABEL_CHARS);
    ImGui.textUnformatted(fmt.display);
    if (fmt.tooltip != null && ImGui.isItemHovered()) {
      ImGui.setTooltip(fmt.tooltip);
    }
    ImGui.sameLine(EditorColors.INSPECTOR_LABEL_WIDTH);
  }

  public static void beginControlColumn() {
    ImGui.setNextItemWidth(-1f);
  }

  public static float controlColumnStart() {
    return EditorColors.INSPECTOR_LABEL_WIDTH;
  }

  /** Push muted text color for read-only / disabled value display. */
  public static void pushReadOnly() {
    EditorStyle.pushMutedText();
  }

  /** Pop muted text color. */
  public static void popReadOnly() {
    EditorStyle.popMutedText();
  }

  /**
   * Draws a read-only row with label on the left and a muted value on the right.
   * The value text is truncated to fit the available width.
   */
  public static void readOnlyValue(String label, String value) {
    begin(label);
    pushReadOnly();
    float maxWidth = ImGui.getContentRegionAvailX();
    String display = EditorStyle.truncate(value == null ? "" : value, maxWidth);
    ImGui.textUnformatted(display);
    if (value != null && !value.equals(display) && ImGui.isItemHovered()) {
      ImGui.setTooltip(value);
    }
    popReadOnly();
    end();
  }
}
