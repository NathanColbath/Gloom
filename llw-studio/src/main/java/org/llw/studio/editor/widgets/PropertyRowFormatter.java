package org.llw.studio.editor.widgets;

/**
 * Pure-Java label formatting for inspector property rows.
 * Does NOT depend on ImGui native bindings, making it fully unit-testable.
 */
public final class PropertyRowFormatter {
  private PropertyRowFormatter() {}

  /** Result of formatting a label for display. */
  public static final class FormatResult {
    /** The text to show in the row label area. */
    public final String display;
    /**
     * The full original text, set only when the label was truncated.
     * {@code null} when the label fits without truncation.
     */
    public final String tooltip;

    public FormatResult(String display, String tooltip) {
      this.display = display;
      this.tooltip = tooltip;
    }
  }

  /**
   * Formats a label for display in a property row.
   *
   * @param text     the raw label text (may be null)
   * @param maxChars maximum number of characters allowed in the display area
   * @return a {@link FormatResult} with the formatted display and optional tooltip
   */
  public static FormatResult formatLabel(String text, int maxChars) {
    if (text == null) {
      return new FormatResult("", null);
    }
    if (maxChars <= 0) {
      return new FormatResult("", text.isEmpty() ? null : text);
    }
    if (text.length() <= maxChars) {
      return new FormatResult(text, null);
    }
    // Text exceeds maxChars — truncate with ellipsis
    String ellipsis = "...";
    int remaining = maxChars - ellipsis.length();
    if (remaining <= 0) {
      // Not enough room for ellipsis — just take first maxChars chars
      return new FormatResult(text.substring(0, maxChars), text);
    }
    int left = remaining / 2;
    int right = remaining - left;
    String display = text.substring(0, left) + ellipsis + text.substring(text.length() - right);
    return new FormatResult(display, text);
  }
}
