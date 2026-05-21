package org.llw.studio.editor.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * Applies the Gloom dark theme to ImGui — deep slate surfaces with a warm amber accent.
 *
 * <p>Every Dear ImGui colour slot is explicitly set so no default-blue remnants leak
 * through. Style metrics are tuned for a clean, modern editor look:
 * <ul>
 *   <li>Zero rounding on window/dock nodes (clean edge-to-edge feel).</li>
 *   <li>Subtle rounding on interactive elements (buttons, inputs, tabs).</li>
 *   <li>Generous but not wasteful padding and spacing.</li>
 * </ul>
 *
 * <p>Colour tokens are defined in {@link EditorColors} and shared by the scoped
 * helpers in {@link EditorStyle}.
 */
public final class GloomTheme {
  private GloomTheme() {}

  public static void apply() {
    ImGuiStyle style = ImGui.getStyle();

    // ── Sizing / Metrics ────────────────────────────────────────────
    // Keep dock/window edges razor-sharp; round interactive controls.
    style.setWindowRounding(0f);
    style.setChildRounding(0f);
    style.setFrameRounding(3f);
    style.setPopupRounding(4f);
    style.setGrabRounding(3f);
    style.setScrollbarRounding(3f);
    style.setTabRounding(3f);

    // Borders: subtle lines on window/child boundaries, none on controls.
    style.setWindowBorderSize(1f);
    style.setChildBorderSize(1f);
    style.setPopupBorderSize(1f);
    style.setFrameBorderSize(0f);
    style.setTabBorderSize(0f);

    // Padding
    style.setWindowPadding(10f, 10f);
    style.setFramePadding(6f, 4f);
    style.setCellPadding(4f, 3f);
    style.setItemSpacing(6f, 5f);
    style.setItemInnerSpacing(5f, 4f);
    style.setIndentSpacing(18f);
    style.setScrollbarSize(10f);
    style.setGrabMinSize(8f);

    // ── Colors ──────────────────────────────────────────────────────
    float[][] colors = style.getColors();

    // Text
    set(colors, ImGuiCol.Text,                 EditorColors.TEXT_PRIMARY);
    set(colors, ImGuiCol.TextDisabled,         EditorColors.TEXT_MUTED);

    // Windows / backgrounds
    set(colors, ImGuiCol.WindowBg,             EditorColors.WINDOW_BG);
    set(colors, ImGuiCol.ChildBg,              EditorColors.SURFACE);
    set(colors, ImGuiCol.PopupBg,              EditorColors.SURFACE_LIGHT);
    set(colors, ImGuiCol.Border,               EditorColors.BORDER);
    set(colors, ImGuiCol.BorderShadow,         EditorColors.BORDER);

    // Input fields
    set(colors, ImGuiCol.FrameBg,              EditorColors.INPUT_BG);
    set(colors, ImGuiCol.FrameBgHovered,       EditorColors.SURFACE_LIGHT);
    set(colors, ImGuiCol.FrameBgActive,        EditorColors.PANEL_HEADER);

    // Title bars
    set(colors, ImGuiCol.TitleBg,              EditorColors.PANEL_BG);
    set(colors, ImGuiCol.TitleBgActive,        EditorColors.PANEL_HEADER);
    set(colors, ImGuiCol.TitleBgCollapsed,     EditorColors.WINDOW_BG);

    // Menu bar
    set(colors, ImGuiCol.MenuBarBg,            EditorColors.SURFACE);

    // Scrollbar
    set(colors, ImGuiCol.ScrollbarBg,          EditorColors.WINDOW_BG);
    set(colors, ImGuiCol.ScrollbarGrab,        EditorColors.BUTTON_BG);
    set(colors, ImGuiCol.ScrollbarGrabHovered, EditorColors.BUTTON_HOVER);
    set(colors, ImGuiCol.ScrollbarGrabActive,  EditorColors.ACCENT);

    // Checkbox / toggle / slider grab
    set(colors, ImGuiCol.CheckMark,            EditorColors.ACCENT);
    set(colors, ImGuiCol.SliderGrab,           EditorColors.ACCENT);
    set(colors, ImGuiCol.SliderGrabActive,     EditorColors.ACCENT_HOVER);

    // Buttons
    set(colors, ImGuiCol.Button,               EditorColors.BUTTON_BG);
    set(colors, ImGuiCol.ButtonHovered,        EditorColors.BUTTON_HOVER);
    set(colors, ImGuiCol.ButtonActive,         EditorColors.PANEL_HEADER);

    // Headers (collapsible, tree, selectable)
    set(colors, ImGuiCol.Header,               EditorColors.ACCENT);
    set(colors, ImGuiCol.HeaderHovered,        EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.HeaderActive,         EditorColors.SELECTION_BG);

    // Separators
    set(colors, ImGuiCol.Separator,            EditorColors.BORDER);
    set(colors, ImGuiCol.SeparatorHovered,     EditorColors.BORDER_STRONG);
    set(colors, ImGuiCol.SeparatorActive,      EditorColors.ACCENT);

    // Resize grips
    set(colors, ImGuiCol.ResizeGrip,           EditorColors.BORDER);
    set(colors, ImGuiCol.ResizeGripHovered,    EditorColors.ACCENT_HOVER);
    set(colors, ImGuiCol.ResizeGripActive,     EditorColors.ACCENT);

    // Tabs
    set(colors, ImGuiCol.Tab,                  EditorColors.PANEL_BG);
    set(colors, ImGuiCol.TabHovered,           EditorColors.SURFACE_LIGHT);
    set(colors, ImGuiCol.TabActive,            EditorColors.SURFACE);
    set(colors, ImGuiCol.TabUnfocused,         EditorColors.PANEL_BG);
    set(colors, ImGuiCol.TabUnfocusedActive,   EditorColors.SURFACE);

    // Docking
    set(colors, ImGuiCol.DockingPreview,       EditorColors.ACCENT);
    set(colors, ImGuiCol.DockingEmptyBg,       EditorColors.WINDOW_BG);

    // ── NEW: Table colors ───────────────────────────────────────────
    set(colors, ImGuiCol.TableHeaderBg,        EditorColors.TABLE_HEADER_BG);
    set(colors, ImGuiCol.TableBorderStrong,    EditorColors.TABLE_BORDER_STRONG);
    set(colors, ImGuiCol.TableBorderLight,     EditorColors.TABLE_BORDER_LIGHT);
    set(colors, ImGuiCol.TableRowBg,           EditorColors.TABLE_ROW_BG);
    set(colors, ImGuiCol.TableRowBgAlt,        EditorColors.TABLE_ROW_BG_ALT);

    // ── NEW: Plot colors ────────────────────────────────────────────
    set(colors, ImGuiCol.PlotLines,            EditorColors.PLOT_LINES);
    set(colors, ImGuiCol.PlotLinesHovered,     EditorColors.PLOT_LINES_HOVERED);
    set(colors, ImGuiCol.PlotHistogram,        EditorColors.PLOT_HISTOGRAM);
    set(colors, ImGuiCol.PlotHistogramHovered, EditorColors.PLOT_HISTOGRAM_HOVERED);

    // ── NEW: Navigation / Modal ─────────────────────────────────────
    set(colors, ImGuiCol.NavHighlight,         EditorColors.NAV_HIGHLIGHT);
    set(colors, ImGuiCol.NavWindowingHighlight,EditorColors.NAV_HIGHLIGHT);
    set(colors, ImGuiCol.NavWindowingDimBg,    EditorColors.NAV_DIM_BG);
    set(colors, ImGuiCol.ModalWindowDimBg,     EditorColors.MODAL_DIM_BG);

    // Misc
    set(colors, ImGuiCol.TextSelectedBg,       EditorColors.SELECTION_BG);
    set(colors, ImGuiCol.DragDropTarget,       EditorColors.ACCENT_HOVER);
  }

  private static void set(float[][] colors, int index, float[] rgba) {
    colors[index][0] = rgba[0];
    colors[index][1] = rgba[1];
    colors[index][2] = rgba[2];
    colors[index][3] = rgba[3];
  }
}
