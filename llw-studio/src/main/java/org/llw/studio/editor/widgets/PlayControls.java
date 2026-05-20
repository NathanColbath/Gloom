package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.llw.studio.editor.theme.EditorColors;

/**
 * Play/Stop toggle styled for the editor toolbar.
 */
public final class PlayControls {
  private PlayControls() {}

  public static boolean render(boolean playing) {
    return render(playing, true);
  }

  public static boolean render(boolean playing, boolean enabled) {
    if (!enabled) {
      ImGui.beginDisabled();
    }
    if (playing) {
      ImGui.pushStyleColor(ImGuiCol.Button, EditorColors.STOP_ACTIVE[0], EditorColors.STOP_ACTIVE[1],
          EditorColors.STOP_ACTIVE[2], EditorColors.STOP_ACTIVE[3]);
      ImGui.pushStyleColor(ImGuiCol.ButtonHovered, EditorColors.STOP_ACTIVE[0], EditorColors.STOP_ACTIVE[1],
          EditorColors.STOP_ACTIVE[2], EditorColors.STOP_ACTIVE[3]);
    } else {
      ImGui.pushStyleColor(ImGuiCol.Button, EditorColors.PLAY_ACTIVE[0], EditorColors.PLAY_ACTIVE[1],
          EditorColors.PLAY_ACTIVE[2], EditorColors.PLAY_ACTIVE[3]);
      ImGui.pushStyleColor(ImGuiCol.ButtonHovered, EditorColors.PLAY_ACTIVE[0], EditorColors.PLAY_ACTIVE[1],
          EditorColors.PLAY_ACTIVE[2], EditorColors.PLAY_ACTIVE[3]);
    }
    boolean clicked = ImGui.button(playing ? "Stop" : "Play", 60f, 0f);
    ImGui.popStyleColor(2);
    if (!enabled) {
      ImGui.endDisabled();
    }
    return enabled && clicked;
  }
}
