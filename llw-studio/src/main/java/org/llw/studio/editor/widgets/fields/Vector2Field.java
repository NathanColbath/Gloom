package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import imgui.type.ImFloat;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * X/Y float pair on one inspector row with axis labels.
 */
public final class Vector2Field {
  private Vector2Field() {}

  public static float[] draw(String label, float x, float y) {
    ImGui.pushID(label);
    PropertyRow.label(label);
    float axisLabelW = 14f;
    float spacing = 4f;
    float fieldW = (ImGui.getContentRegionAvailX() - axisLabelW * 2f - spacing * 2f) / 2f;
    ImFloat fx = new ImFloat(x);
    ImFloat fy = new ImFloat(y);

    EditorStyleMuted.axisLabel("X");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##x", fx);
    ImGui.sameLine(0f, spacing);
    EditorStyleMuted.axisLabel("Y");
    ImGui.sameLine();
    ImGui.setNextItemWidth(fieldW);
    ImGui.inputFloat("##y", fy);
    ImGui.popID();
    return new float[]{fx.get(), fy.get()};
  }

  /** Muted axis labels for compact multi-field rows. */
  private static final class EditorStyleMuted {
    static void axisLabel(String text) {
      ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.55f, 0.55f, 0.55f, 1f);
      ImGui.text(text);
      ImGui.popStyleColor();
    }
  }
}
