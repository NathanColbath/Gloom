package org.llw.studio.editor.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * Applies the modern flat gray ImGui editor theme globally at startup.
 *
 * <p>Colour tokens: {@link EditorColors}. Scoped overrides: {@link EditorStyle}.
 * Metrics: {@link EditorMetrics}.
 */
public final class GloomTheme {
    private GloomTheme() {
    }

    /** Alias for {@link #apply()} — modern flat gray palette. */
    public static void applyModernGrayTheme() {
        apply();
    }

    public static void apply() {
        ImGuiStyle style = ImGui.getStyle();
        applyMetrics(style);
        // getColors() returns a copy; setColors() must persist changes to native ImGui.
        float[][] colors = style.getColors();
        applyColors(colors);
        style.setColors(colors);
    }

    static void applyMetrics(ImGuiStyle style) {
        style.setWindowRounding(EditorMetrics.ROUNDING_WINDOW);
        style.setChildRounding(EditorMetrics.ROUNDING_CHILD);
        style.setFrameRounding(EditorMetrics.ROUNDING_FRAME);
        style.setPopupRounding(EditorMetrics.ROUNDING_POPUP);
        style.setGrabRounding(EditorMetrics.ROUNDING_GRAB);
        style.setScrollbarRounding(EditorMetrics.ROUNDING_SCROLLBAR);
        style.setTabRounding(EditorMetrics.ROUNDING_TAB);

        style.setWindowBorderSize(EditorMetrics.BORDER_WINDOW);
        style.setChildBorderSize(EditorMetrics.BORDER_CHILD);
        style.setPopupBorderSize(EditorMetrics.BORDER_POPUP);
        style.setFrameBorderSize(EditorMetrics.BORDER_FRAME);
        style.setTabBorderSize(EditorMetrics.BORDER_TAB);

        style.setWindowPadding(EditorMetrics.PADDING_WINDOW_X, EditorMetrics.PADDING_WINDOW_Y);
        style.setFramePadding(EditorMetrics.PADDING_FRAME_X, EditorMetrics.PADDING_FRAME_Y);
        style.setCellPadding(EditorMetrics.PADDING_CELL_X, EditorMetrics.PADDING_CELL_Y);
        style.setItemSpacing(EditorMetrics.SPACING_ITEM_X, EditorMetrics.SPACING_ITEM_Y);
        style.setItemInnerSpacing(EditorMetrics.SPACING_INNER_X, EditorMetrics.SPACING_INNER_Y);
        style.setIndentSpacing(EditorMetrics.INDENT_SPACING);
        style.setScrollbarSize(EditorMetrics.SCROLLBAR_SIZE);
        style.setGrabMinSize(EditorMetrics.GRAB_MIN_SIZE);
    }

    static void applyColors(float[][] colors) {
        set(colors, ImGuiCol.Text, EditorColors.TEXT_PRIMARY);
        set(colors, ImGuiCol.TextDisabled, EditorColors.TEXT_MUTED);

        set(colors, ImGuiCol.WindowBg, EditorColors.WINDOW_BG);
        set(colors, ImGuiCol.ChildBg, EditorColors.SURFACE);
        set(colors, ImGuiCol.PopupBg, EditorColors.SURFACE_LIGHT);
        set(colors, ImGuiCol.Border, EditorColors.BORDER);
        set(colors, ImGuiCol.BorderShadow, EditorColors.BORDER);

        set(colors, ImGuiCol.FrameBg, EditorColors.INPUT_BG);
        set(colors, ImGuiCol.FrameBgHovered, EditorColors.SURFACE_LIGHT);
        set(colors, ImGuiCol.FrameBgActive, EditorColors.PANEL_HEADER);

        set(colors, ImGuiCol.TitleBg, EditorColors.PANEL_BG);
        set(colors, ImGuiCol.TitleBgActive, EditorColors.PANEL_HEADER);
        set(colors, ImGuiCol.TitleBgCollapsed, EditorColors.WINDOW_BG);

        set(colors, ImGuiCol.MenuBarBg, EditorColors.SURFACE);

        set(colors, ImGuiCol.ScrollbarBg, EditorColors.WINDOW_BG);
        set(colors, ImGuiCol.ScrollbarGrab, EditorColors.BUTTON_BG);
        set(colors, ImGuiCol.ScrollbarGrabHovered, EditorColors.BUTTON_HOVER);
        set(colors, ImGuiCol.ScrollbarGrabActive, EditorColors.ACCENT);

        set(colors, ImGuiCol.CheckMark, EditorColors.ACCENT);
        set(colors, ImGuiCol.SliderGrab, EditorColors.ACCENT);
        set(colors, ImGuiCol.SliderGrabActive, EditorColors.ACCENT_HOVER);

        set(colors, ImGuiCol.Button, EditorColors.BUTTON_BG);
        set(colors, ImGuiCol.ButtonHovered, EditorColors.BUTTON_HOVER);
        set(colors, ImGuiCol.ButtonActive, EditorColors.BUTTON_ACTIVE);

        // Neutral headers for trees/selectables; selection uses SELECTION_BG not accent.
        set(colors, ImGuiCol.Header, EditorColors.BUTTON_BG);
        set(colors, ImGuiCol.HeaderHovered, EditorColors.BUTTON_HOVER);
        set(colors, ImGuiCol.HeaderActive, EditorColors.SELECTION_BG);

        set(colors, ImGuiCol.Separator, EditorColors.BORDER);
        set(colors, ImGuiCol.SeparatorHovered, EditorColors.BORDER_STRONG);
        set(colors, ImGuiCol.SeparatorActive, EditorColors.ACCENT);

        set(colors, ImGuiCol.ResizeGrip, EditorColors.BORDER);
        set(colors, ImGuiCol.ResizeGripHovered, EditorColors.BUTTON_HOVER);
        set(colors, ImGuiCol.ResizeGripActive, EditorColors.ACCENT);

        set(colors, ImGuiCol.Tab, EditorColors.PANEL_BG);
        set(colors, ImGuiCol.TabHovered, EditorColors.SURFACE_LIGHT);
        set(colors, ImGuiCol.TabActive, EditorColors.SURFACE);
        set(colors, ImGuiCol.TabUnfocused, EditorColors.PANEL_BG);
        set(colors, ImGuiCol.TabUnfocusedActive, EditorColors.SURFACE_LIGHT);

        set(colors, ImGuiCol.DockingPreview, EditorColors.ACCENT);
        set(colors, ImGuiCol.DockingEmptyBg, EditorColors.WINDOW_BG);

        set(colors, ImGuiCol.TableHeaderBg, EditorColors.TABLE_HEADER_BG);
        set(colors, ImGuiCol.TableBorderStrong, EditorColors.TABLE_BORDER_STRONG);
        set(colors, ImGuiCol.TableBorderLight, EditorColors.TABLE_BORDER_LIGHT);
        set(colors, ImGuiCol.TableRowBg, EditorColors.TABLE_ROW_BG);
        set(colors, ImGuiCol.TableRowBgAlt, EditorColors.TABLE_ROW_BG_ALT);

        set(colors, ImGuiCol.PlotLines, EditorColors.PLOT_LINES);
        set(colors, ImGuiCol.PlotLinesHovered, EditorColors.PLOT_LINES_HOVERED);
        set(colors, ImGuiCol.PlotHistogram, EditorColors.PLOT_HISTOGRAM);
        set(colors, ImGuiCol.PlotHistogramHovered, EditorColors.PLOT_HISTOGRAM_HOVERED);

        set(colors, ImGuiCol.NavHighlight, EditorColors.NAV_HIGHLIGHT);
        set(colors, ImGuiCol.NavWindowingHighlight, EditorColors.NAV_HIGHLIGHT);
        set(colors, ImGuiCol.NavWindowingDimBg, EditorColors.NAV_DIM_BG);
        set(colors, ImGuiCol.ModalWindowDimBg, EditorColors.MODAL_DIM_BG);

        set(colors, ImGuiCol.TextSelectedBg, EditorColors.SELECTION_BG);
        set(colors, ImGuiCol.DragDropTarget, EditorColors.ACCENT_HOVER);
    }

    private static void set(float[][] colors, int index, float[] rgba) {
        colors[index][0] = rgba[0];
        colors[index][1] = rgba[1];
        colors[index][2] = rgba[2];
        colors[index][3] = rgba[3];
    }
}
