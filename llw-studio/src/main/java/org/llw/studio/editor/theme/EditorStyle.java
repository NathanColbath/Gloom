package org.llw.studio.editor.theme;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

/**
 * Scoped ImGui style pushes for panels, selection, headers, and log lines.
 */
public final class EditorStyle {
  private EditorStyle() {}

  public static void pushPanelChrome() {
    ImGui.pushStyleColor(ImGuiCol.ChildBg, EditorColors.SURFACE[0], EditorColors.SURFACE[1],
        EditorColors.SURFACE[2], EditorColors.SURFACE[3]);
    ImGui.pushStyleColor(ImGuiCol.Border, EditorColors.BORDER[0], EditorColors.BORDER[1],
        EditorColors.BORDER[2], EditorColors.BORDER[3]);
  }

  public static void popPanelChrome() {
    ImGui.popStyleColor(2);
  }

  public static void pushAccent() {
    ImGui.pushStyleColor(ImGuiCol.Button, EditorColors.ACCENT[0], EditorColors.ACCENT[1], EditorColors.ACCENT[2],
        EditorColors.ACCENT[3]);
    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, EditorColors.ACCENT_HOVER[0], EditorColors.ACCENT_HOVER[1],
        EditorColors.ACCENT_HOVER[2], EditorColors.ACCENT_HOVER[3]);
    ImGui.pushStyleColor(ImGuiCol.ButtonActive, EditorColors.ACCENT[0], EditorColors.ACCENT[1],
        EditorColors.ACCENT[2], EditorColors.ACCENT[3]);
  }

  public static void popAccent() {
    ImGui.popStyleColor(3);
  }

  public static void pushDangerText() {
    ImGui.pushStyleColor(ImGuiCol.Text, EditorColors.DANGER[0], EditorColors.DANGER[1], EditorColors.DANGER[2],
        EditorColors.DANGER[3]);
  }

  public static void popDangerText() {
    ImGui.popStyleColor();
  }

  public static void pushSelection() {
    ImGui.pushStyleColor(ImGuiCol.Header, EditorColors.SELECTION_BG[0], EditorColors.SELECTION_BG[1],
        EditorColors.SELECTION_BG[2], EditorColors.SELECTION_BG[3]);
    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EditorColors.SELECTION_BG[0], EditorColors.SELECTION_BG[1],
        EditorColors.SELECTION_BG[2], EditorColors.SELECTION_BG[3]);
    ImGui.pushStyleColor(ImGuiCol.HeaderActive, EditorColors.SELECTION_BG[0], EditorColors.SELECTION_BG[1],
        EditorColors.SELECTION_BG[2], EditorColors.SELECTION_BG[3]);
  }

  public static void popSelection() {
    ImGui.popStyleColor(3);
  }

  public static void pushMutedText() {
    ImGui.pushStyleColor(ImGuiCol.Text, EditorColors.TEXT_MUTED[0], EditorColors.TEXT_MUTED[1],
        EditorColors.TEXT_MUTED[2], EditorColors.TEXT_MUTED[3]);
  }

  public static void popMutedText() {
    ImGui.popStyleColor();
  }

  public static void pushLogColor(float[] rgba) {
    ImGui.pushStyleColor(ImGuiCol.Text, rgba[0], rgba[1], rgba[2], rgba[3]);
  }

  public static void popLogColor() {
    ImGui.popStyleColor();
  }

  public static void pushComponentHeader() {
    ImGui.pushStyleColor(ImGuiCol.Header, EditorColors.COMPONENT_HEADER_BG[0], EditorColors.COMPONENT_HEADER_BG[1],
        EditorColors.COMPONENT_HEADER_BG[2], EditorColors.COMPONENT_HEADER_BG[3]);
    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EditorColors.COMPONENT_HEADER_HOVER[0],
        EditorColors.COMPONENT_HEADER_HOVER[1], EditorColors.COMPONENT_HEADER_HOVER[2],
        EditorColors.COMPONENT_HEADER_HOVER[3]);
    ImGui.pushStyleColor(ImGuiCol.HeaderActive, EditorColors.COMPONENT_HEADER_HOVER[0],
        EditorColors.COMPONENT_HEADER_HOVER[1], EditorColors.COMPONENT_HEADER_HOVER[2],
        EditorColors.COMPONENT_HEADER_HOVER[3]);
  }

  public static void popComponentHeader() {
    ImGui.popStyleColor(3);
  }

  public static void pushHierarchyRowHover() {
    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EditorColors.HIERARCHY_ROW_HOVER[0],
        EditorColors.HIERARCHY_ROW_HOVER[1], EditorColors.HIERARCHY_ROW_HOVER[2],
        EditorColors.HIERARCHY_ROW_HOVER[3]);
  }

  public static void popHierarchyRowHover() {
    ImGui.popStyleColor();
  }

  /**
   * Shortens text to fit {@code maxWidth} pixels, appending {@code ...} when clipped.
   */
  public static String truncate(String text, float maxWidth) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    if (ImGui.calcTextSize(text).x <= maxWidth) {
      return text;
    }
    String ellipsis = "...";
    float ellipsisWidth = ImGui.calcTextSize(ellipsis).x;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      builder.append(text.charAt(i));
      if (ImGui.calcTextSize(builder.toString()).x + ellipsisWidth > maxWidth) {
        builder.setLength(Math.max(0, builder.length() - 1));
        break;
      }
    }
    return builder + ellipsis;
  }

  // ────────────────────────────────────────────────────────────────────
  // Axis label helpers
  // ────────────────────────────────────────────────────────────────────

  /** Push subdued text color for axis/sublabels (e.g. R/G/B/A, X/Y). */
  public static void pushAxisLabel() {
    ImGui.pushStyleColor(ImGuiCol.Text, EditorColors.TEXT_SUBDUED[0], EditorColors.TEXT_SUBDUED[1],
        EditorColors.TEXT_SUBDUED[2], EditorColors.TEXT_SUBDUED[3]);
  }

  public static void popAxisLabel() {
    ImGui.popStyleColor();
  }

  /** Convenience: push subdued text color, draw label, pop. */
  public static void axisLabel(String text) {
    pushAxisLabel();
    ImGui.text(text);
    popAxisLabel();
  }

  // ────────────────────────────────────────────────────────────────────
  // Panel header spacing
  // ────────────────────────────────────────────────────────────────────

  /** Tighten item spacing for compact panel header toolbars. */
  public static void pushPanelHeaderSpacing() {
    ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4f, 4f);
  }

  public static void popPanelHeaderSpacing() {
    ImGui.popStyleVar();
  }

  // ────────────────────────────────────────────────────────────────────
  // Character-based middle-truncation (no ImGui dependency)
  // ────────────────────────────────────────────────────────────────────

  /**
   * Truncates {@code text} so the result is at most {@code maxChars} characters,
   * keeping both prefix and suffix separated by {@code ...}.
   * Falls back to simple end‑truncation when {@code maxChars < 4}.
   */
  public static String middleTruncate(String text, int maxChars) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    if (text.length() <= maxChars) {
      return text;
    }
    if (maxChars < 4) {
      return text.substring(0, maxChars);
    }
    String ellipsis = "...";
    int remaining = maxChars - ellipsis.length();
    int left = remaining / 2;
    int right = remaining - left;
    return text.substring(0, left) + ellipsis + text.substring(text.length() - right);
  }
}
