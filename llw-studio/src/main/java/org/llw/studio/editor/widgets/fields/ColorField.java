package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import imgui.type.ImFloat;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * RGBA tint editor: four fields on one row when wide, stacked rows when the panel is narrow.
 */
public final class ColorField {
  private static final float NARROW_THRESHOLD = 220f;

  private ColorField() {}

  public static float[] draw(String label, float r, float g, float b, float a) {
    if (ImGui.getContentRegionAvailX() < NARROW_THRESHOLD) {
      r = FloatField.draw("Tint R", r);
      g = FloatField.draw("Tint G", g);
      b = FloatField.draw("Tint B", b);
      a = FloatField.draw("Tint A", a);
      return new float[]{r, g, b, a};
    }

    ImGui.pushID(label);
    PropertyRow.label(label);
    float axisLabelW = 14f;
    float spacing = 4f;
    float fieldW = (ImGui.getContentRegionAvailX() - axisLabelW * 4f - spacing * 3f) / 4f;
    ImFloat fr = new ImFloat(r);
    ImFloat fg = new ImFloat(g);
    ImFloat fb = new ImFloat(b);
    ImFloat fa = new ImFloat(a);

    axisLabel("R");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##r", fr);
    ImGui.sameLine(0f, spacing);
    axisLabel("G");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##g", fg);
    ImGui.sameLine(0f, spacing);
    axisLabel("B");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##b", fb);
    ImGui.sameLine(0f, spacing);
    axisLabel("A");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##a", fa);
    ImGui.popID();
    return new float[]{fr.get(), fg.get(), fb.get(), fa.get()};
  }

  private static void axisLabel(String text) {
    ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.55f, 0.55f, 0.55f, 1f);
    ImGui.text(text);
    ImGui.popStyleColor();
  }
}
