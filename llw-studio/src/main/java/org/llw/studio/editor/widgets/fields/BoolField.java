package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Boolean checkbox in a standard inspector row.
 */
public final class BoolField {
  private BoolField() {}

  public static boolean draw(String label, boolean value) {
    PropertyRow.begin(label);
    ImBoolean boxed = new ImBoolean(value);
    ImGui.checkbox("##value", boxed);
    PropertyRow.end();
    return boxed.get();
  }
}
