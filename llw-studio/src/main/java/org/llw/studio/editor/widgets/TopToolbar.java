package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SceneToolMode;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Main editor toolbar: branding, play controls, scene tools, and edit/play status.
 */
public final class TopToolbar {
  private final EditorSession session;
  private final Runnable togglePlay;

  public TopToolbar(EditorSession session, Runnable togglePlay) {
    this.session = session;
    this.togglePlay = togglePlay;
  }

  public void render(StudioContext context) {
    ImGui.begin("Toolbar", imgui.flag.ImGuiWindowFlags.NoScrollbar | imgui.flag.ImGuiWindowFlags.NoScrollWithMouse);

    EditorStyle.pushMutedText();
    ImGui.text("LLW Studio");
    EditorStyle.popMutedText();

    ImGui.sameLine(0f, 24f);
    boolean playControlsEnabled = !context.isPlayPreparing();
    if (PlayControls.render(context.isPlaying(), playControlsEnabled)) {
      togglePlay.run();
    }

    ImGui.sameLine(0f, 24f);
    boolean toolsEnabled = !context.isPlaying() && !context.isPlayPreparing();
    if (!toolsEnabled) {
      ImGui.beginDisabled();
    }
    drawSceneTools();
    if (!toolsEnabled) {
      ImGui.endDisabled();
    }

    float statusWidth = context.isPlayPreparing() ? 220f : 80f;
    float statusX = ImGui.getWindowContentRegionMaxX() - statusWidth;
    if (statusX > ImGui.getCursorPosX()) {
      ImGui.sameLine(statusX);
    } else {
      ImGui.sameLine();
    }
    if (context.isPlayPreparing()) {
      EditorStyle.pushMutedText();
      ImGui.text(context.playPrepareStatus());
      EditorStyle.popMutedText();
    } else if (context.isPlaying()) {
      ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, org.llw.studio.editor.theme.EditorColors.PLAY_ACTIVE[0],
          org.llw.studio.editor.theme.EditorColors.PLAY_ACTIVE[1],
          org.llw.studio.editor.theme.EditorColors.PLAY_ACTIVE[2],
          org.llw.studio.editor.theme.EditorColors.PLAY_ACTIVE[3]);
      ImGui.text("Playing");
      ImGui.popStyleColor();
    } else {
      EditorStyle.pushMutedText();
      ImGui.text("Editing");
      EditorStyle.popMutedText();
    }

    ImGui.end();
  }

  private void drawSceneTools() {
    var toolState = session.toolState();
    if (ToolButton.toggle("Hand", toolState.mode() == SceneToolMode.HAND, 48f)) {
      toolState.setMode(SceneToolMode.HAND);
    }
    ImGui.sameLine();
    if (ToolButton.toggle("Move", toolState.mode() == SceneToolMode.MOVE, 48f)) {
      toolState.setMode(SceneToolMode.MOVE);
    }
    ImGui.sameLine();
    if (ToolButton.toggle("Rotate", toolState.mode() == SceneToolMode.ROTATE, 56f)) {
      toolState.setMode(SceneToolMode.ROTATE);
    }
    ImGui.sameLine();
    if (ToolButton.toggle("Scale", toolState.mode() == SceneToolMode.SCALE, 48f)) {
      toolState.setMode(SceneToolMode.SCALE);
    }
    ImGui.sameLine();
    if (ToolButton.toggle("Paint", toolState.mode() == SceneToolMode.TILE_PAINT, 52f)) {
      toolState.setMode(SceneToolMode.TILE_PAINT);
    }
    ImGui.sameLine();
    if (ToolButton.toggle("Erase", toolState.mode() == SceneToolMode.TILE_ERASE, 52f)) {
      toolState.setMode(SceneToolMode.TILE_ERASE);
    }
    ImGui.sameLine();
    if (ToolButton.action("Frame All", 72f)) {
      session.frameScene(null);
    }
  }
}
