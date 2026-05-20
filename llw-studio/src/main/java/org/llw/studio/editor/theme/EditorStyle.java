package org.llw.studio.editor.theme;

import imgui.ImGui;
import imgui.flag.ImGuiCol;

/**
 * Scoped ImGui style pushes for panels, selection, headers, and log lines.
 */
public final class EditorStyle {
  private EditorStyle() {}

  public static void pushPanelChrome() {
    ImGui.pushStyleColor(ImGuiCol.ChildBg, EditorColors.EDITOR_SURFACE[0], EditorColors.EDITOR_SURFACE[1],
        EditorColors.EDITOR_SURFACE[2], EditorColors.EDITOR_SURFACE[3]);
    ImGui.pushStyleColor(ImGuiCol.Border, EditorColors.EDITOR_BORDER[0], EditorColors.EDITOR_BORDER[1],
        EditorColors.EDITOR_BORDER[2], EditorColors.EDITOR_BORDER[3]);
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
}
