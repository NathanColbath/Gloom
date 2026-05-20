package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import imgui.type.ImFloat;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Single float property in a standard inspector row.
 */
public final class FloatField {
  private FloatField() {}

  public static float draw(String label, float value) {
    PropertyRow.begin(label);
    ImFloat boxed = new ImFloat(value);
    ImGui.inputFloat("##value", boxed);
    PropertyRow.end();
    return boxed.get();
  }

  /**
   * Compact float input without a property-row label (e.g. multi-axis rows).
   */
  public static float drawInline(String id, float value, float width) {
    ImGui.setNextItemWidth(width);
    ImFloat boxed = new ImFloat(value);
    ImGui.inputFloat(id, boxed);
    return boxed.get();
  }
}
