package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Search field with a leading magnifier icon and optional fixed width.
 */
public final class SearchInput {
  private SearchInput() {}

  public static boolean render(String id, ImString buffer, String hint, float width) {
    EditorStyle.pushMutedText();
    ImGui.text(EditorIcons.SEARCH);
    EditorStyle.popMutedText();
    ImGui.sameLine();
    ImGui.setNextItemWidth(width - 20f);
    return ImGui.inputTextWithHint(id, hint, buffer);
  }
}
