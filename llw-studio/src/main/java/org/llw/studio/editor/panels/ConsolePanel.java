package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.ConsoleLogRow;
import org.llw.studio.editor.widgets.SearchInput;
import org.llw.util.log.LogLevel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Filterable log console fed by {@link org.llw.studio.log.ConsoleLogSink}.
 */
public final class ConsolePanel implements EditorPanel {
  private static final int MAX_LINES = 500;
  private static final float SCROLL_BOTTOM_THRESHOLD = 4f;

  private final Deque<ConsoleEntry> lines = new ArrayDeque<>();
  private final ImString searchBuffer = new ImString("", 256);
  private boolean showInfo = true;
  private boolean showWarning = true;
  private boolean showError = true;

  /**
   * One console line with optional repeat count for consecutive duplicates.
   *
   * @param level        log severity
   * @param message      display text
   * @param repeatCount  number of consecutive identical lines merged
   */
  public record ConsoleEntry(LogLevel level, String message, int repeatCount) {
    /**
     * @param level   log severity
     * @param message display text
     */
    public ConsoleEntry(LogLevel level, String message) {
      this(level, message, 1);
    }
  }

  /**
   * Appends a line, coalescing repeats of the same level and message.
   *
   * @param level   severity
   * @param message text to display
   */
  public void append(LogLevel level, String message) {
    if (!lines.isEmpty()) {
      ConsoleEntry last = lines.peekLast();
      if (last.level() == level && last.message().equals(message)) {
        lines.removeLast();
        lines.addLast(new ConsoleEntry(level, message, last.repeatCount() + 1));
        trimLines();
        return;
      }
    }
    lines.addLast(new ConsoleEntry(level, message));
    trimLines();
  }

  /** Removes all lines from the buffer. */
  public void clear() {
    lines.clear();
  }

  /**
   * @param line message logged at {@link LogLevel#INFO}
   * @deprecated use {@link #append(LogLevel, String)}
   */
  @Deprecated
  public void append(String line) {
    append(LogLevel.INFO, line);
  }

  /** {@inheritDoc} */
  @Override
  public String id() {
    return "console";
  }

  /** {@inheritDoc} */
  @Override
  public String title() {
    return "Console";
  }

  /** {@inheritDoc} */
  @Override
  public void render(StudioContext context) {
    if (!ImGui.begin(title(), ImGuiWindowFlags.HorizontalScrollbar)) {
      ImGui.end();
      return;
    }

    renderToolbar();

    ImGui.beginChild("##console_scroll", 0f, 0f, false, ImGuiWindowFlags.HorizontalScrollbar);
    float scrollY = ImGui.getScrollY();
    float scrollMaxY = ImGui.getScrollMaxY();
    boolean stickToBottom = scrollMaxY <= 0f || scrollY >= scrollMaxY - SCROLL_BOTTOM_THRESHOLD;

    String search = searchBuffer.get().trim().toLowerCase(Locale.ROOT);
    for (ConsoleEntry entry : lines) {
      if (!matchesLevelFilter(entry.level())) {
        continue;
      }
      if (!search.isEmpty() && !matchesSearch(entry, search)) {
        continue;
      }
      ConsoleLogRow.render(entry);
    }

    if (stickToBottom) {
      ImGui.setScrollHereY(1f);
    }
    ImGui.endChild();
    ImGui.end();
  }

  private void renderToolbar() {
    float avail = ImGui.getContentRegionAvailX();
    float clearWidth = 52f;
    SearchInput.render("##console_search", searchBuffer, "Search", avail - clearWidth - 6f);
    ImGui.sameLine();
    if (ImGui.button("Clear", clearWidth, 0f)) {
      clear();
    }

    EditorStyle.pushMutedText();
    if (ImGui.checkbox("Info", showInfo)) {
      showInfo = !showInfo;
    }
    ImGui.sameLine();
    if (ImGui.checkbox("Warn", showWarning)) {
      showWarning = !showWarning;
    }
    ImGui.sameLine();
    if (ImGui.checkbox("Error", showError)) {
      showError = !showError;
    }
    EditorStyle.popMutedText();
  }

  private boolean matchesLevelFilter(LogLevel level) {
    return switch (level) {
      case TRACE, DEBUG, INFO -> showInfo;
      case WARN -> showWarning;
      case ERROR, FATAL -> showError;
    };
  }

  private static boolean matchesSearch(ConsoleEntry entry, String search) {
    if (entry.message().toLowerCase(Locale.ROOT).contains(search)) {
      return true;
    }
    return entry.level().name().toLowerCase(Locale.ROOT).contains(search);
  }

  private void trimLines() {
    while (lines.size() > MAX_LINES) {
      lines.removeFirst();
    }
  }

  Iterable<ConsoleEntry> linesForTest() {
    return lines;
  }
}
