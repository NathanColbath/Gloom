package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.panels.ConsolePanel.ConsoleEntry;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.util.log.LogLevel;

/**
 * Renders a single console log line with level-based coloring and repeat count.
 */
public final class ConsoleLogRow {
  private ConsoleLogRow() {}

  public static void render(ConsoleEntry entry) {
    float[] color = colorFor(entry.level());
    EditorStyle.pushLogColor(color);
    String line = "[" + entry.level() + "] " + entry.message();
    if (entry.repeatCount() > 1) {
      line += "  ×" + entry.repeatCount();
    }
    ImGui.textWrapped(line);
    EditorStyle.popLogColor();
  }

  private static float[] colorFor(LogLevel level) {
    return switch (level) {
      case DEBUG, TRACE -> EditorColors.LOG_DEBUG;
      case INFO -> EditorColors.LOG_INFO;
      case WARN -> EditorColors.LOG_WARN;
      case ERROR, FATAL -> EditorColors.LOG_ERROR;
    };
  }
}
