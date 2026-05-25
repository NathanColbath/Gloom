package org.llw.studio.editor.widgets.fields;

/**
 * RGBA tint editor using ImGui's color picker ({@link ColorPickerField}).
 */
public final class ColorField {
  private ColorField() {}

  public static float[] draw(String label, float r, float g, float b, float a) {
    return ColorPickerField.draw(label, r, g, b, a);
  }
}
