package org.llw.studio.editor.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * Applies Unity-like ImGui style metrics and maps {@link EditorColors} onto ImGui color indices.
 */
public final class UnityTheme {
  private UnityTheme() {}

  public static void apply() {
    ImGuiStyle style = ImGui.getStyle();
    style.setWindowRounding(0f);
    style.setChildRounding(0f);
    style.setFrameRounding(2f);
    style.setGrabRounding(2f);
    style.setTabRounding(0f);
    style.setWindowBorderSize(1f);
    style.setChildBorderSize(1f);
    style.setFrameBorderSize(0f);
    style.setWindowPadding(6f, 6f);
    style.setFramePadding(4f, 3f);
    style.setItemSpacing(6f, 4f);
    style.setItemInnerSpacing(4f, 4f);
    style.setIndentSpacing(16f);
    style.setScrollbarSize(12f);

    float[][] colors = style.getColors();
    set(colors, ImGuiCol.Text, EditorColors.TEXT_PRIMARY);
    set(colors, ImGuiCol.TextDisabled, EditorColors.TEXT_MUTED);
    set(colors, ImGuiCol.WindowBg, EditorColors.EDITOR_BG);
    set(colors, ImGuiCol.ChildBg, EditorColors.EDITOR_SURFACE);
    set(colors, ImGuiCol.PopupBg, EditorColors.EDITOR_SURFACE_LIGHT);
    set(colors, ImGuiCol.Border, EditorColors.EDITOR_BORDER);
    set(colors, ImGuiCol.BorderShadow, EditorColors.EDITOR_BORDER);
    set(colors, ImGuiCol.FrameBg, EditorColors.INPUT_BG);
    set(colors, ImGuiCol.FrameBgHovered, EditorColors.EDITOR_SURFACE_LIGHT);
    set(colors, ImGuiCol.FrameBgActive, EditorColors.EDITOR_PANEL_HEADER);
    set(colors, ImGuiCol.TitleBg, EditorColors.EDITOR_PANEL_BG);
    set(colors, ImGuiCol.TitleBgActive, EditorColors.EDITOR_PANEL_HEADER);
    set(colors, ImGuiCol.TitleBgCollapsed, EditorColors.EDITOR_PANEL_BG);
    set(colors, ImGuiCol.MenuBarBg, EditorColors.EDITOR_SURFACE);
    set(colors, ImGuiCol.ScrollbarBg, EditorColors.EDITOR_BG);
    set(colors, ImGuiCol.ScrollbarGrab, EditorColors.BUTTON_BG);
    set(colors, ImGuiCol.ScrollbarGrabHovered, EditorColors.BUTTON_HOVER);
    set(colors, ImGuiCol.ScrollbarGrabActive, EditorColors.ACCENT);
    set(colors, ImGuiCol.CheckMark, EditorColors.ACCENT);
    set(colors, ImGuiCol.SliderGrab, EditorColors.ACCENT);
    set(colors, ImGuiCol.SliderGrabActive, EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.Button, EditorColors.BUTTON_BG);
    set(colors, ImGuiCol.ButtonHovered, EditorColors.BUTTON_HOVER);
    set(colors, ImGuiCol.ButtonActive, EditorColors.ACCENT);
    set(colors, ImGuiCol.Header, EditorColors.ACCENT);
    set(colors, ImGuiCol.HeaderHovered, EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.HeaderActive, EditorColors.SELECTION_BG);
    set(colors, ImGuiCol.Separator, EditorColors.EDITOR_BORDER);
    set(colors, ImGuiCol.SeparatorHovered, EditorColors.EDITOR_BORDER_STRONG);
    set(colors, ImGuiCol.SeparatorActive, EditorColors.ACCENT);
    set(colors, ImGuiCol.ResizeGrip, EditorColors.EDITOR_BORDER);
    set(colors, ImGuiCol.ResizeGripHovered, EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.ResizeGripActive, EditorColors.ACCENT);
    set(colors, ImGuiCol.Tab, EditorColors.EDITOR_PANEL_BG);
    set(colors, ImGuiCol.TabHovered, EditorColors.EDITOR_SURFACE_LIGHT);
    set(colors, ImGuiCol.TabActive, EditorColors.EDITOR_SURFACE);
    set(colors, ImGuiCol.TabUnfocused, EditorColors.EDITOR_PANEL_BG);
    set(colors, ImGuiCol.TabUnfocusedActive, EditorColors.EDITOR_SURFACE);
    set(colors, ImGuiCol.DockingPreview, EditorColors.ACCENT);
    set(colors, ImGuiCol.DockingEmptyBg, EditorColors.EDITOR_BG);
    set(colors, ImGuiCol.TextSelectedBg, EditorColors.SELECTION_BG);
    set(colors, ImGuiCol.DragDropTarget, EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.NavHighlight, EditorColors.ACCENT);
  }

  private static void set(float[][] colors, int index, float[] rgba) {
    colors[index][0] = rgba[0];
    colors[index][1] = rgba[1];
    colors[index][2] = rgba[2];
    colors[index][3] = rgba[3];
  }
}
