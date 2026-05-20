package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Centers muted placeholder text in the remaining panel area.
 */
public final class EmptyState {
  private EmptyState() {}

  public static void render(String message) {
    float width = ImGui.getContentRegionAvailX();
    float height = ImGui.getContentRegionAvailY();
    if (height < ImGui.getTextLineHeight()) {
      height = ImGui.getTextLineHeight() * 2f;
    }
    ImGui.setCursorPosY(ImGui.getCursorPosY() + Math.max(0f, height * 0.35f));
    float textWidth = ImGui.calcTextSize(message).x;
    ImGui.setCursorPosX(ImGui.getCursorPosX() + Math.max(0f, (width - textWidth) * 0.5f));
    EditorStyle.pushMutedText();
    ImGui.text(message);
    EditorStyle.popMutedText();
  }
}
