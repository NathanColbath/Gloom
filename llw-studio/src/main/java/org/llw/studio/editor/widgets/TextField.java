package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.type.ImString;

/**
 * Text input helpers aligned with {@link PropertyRow} inspector layout.
 */
public final class TextField {
  private TextField() {}

  public static void draw(String id, ImString buffer) {
    ImGui.inputText(id, buffer);
  }

  public static void drawInRow(String id, ImString buffer) {
    PropertyRow.beginControlColumn();
    ImGui.inputText(id, buffer);
    PropertyRow.end();
  }
}
