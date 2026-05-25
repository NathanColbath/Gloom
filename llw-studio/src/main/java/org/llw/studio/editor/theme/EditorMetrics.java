package org.llw.studio.editor.theme;

/**
 * Shared ImGui layout metrics for the modern gray editor theme.
 * Applied by {@link GloomTheme#applyMetrics(ImGuiStyle)}.
 */
public final class EditorMetrics {
    public static final float ROUNDING_WINDOW = 0f;
    public static final float ROUNDING_CHILD = 0f;
    public static final float ROUNDING_FRAME = 3f;
    public static final float ROUNDING_TAB = 3f;
    public static final float ROUNDING_POPUP = 4f;
    public static final float ROUNDING_GRAB = 3f;
    public static final float ROUNDING_SCROLLBAR = 3f;

    public static final float BORDER_WINDOW = 1f;
    public static final float BORDER_CHILD = 1f;
    public static final float BORDER_POPUP = 1f;
    public static final float BORDER_FRAME = 0f;
    public static final float BORDER_TAB = 0f;

    public static final float PADDING_WINDOW_X = 10f;
    public static final float PADDING_WINDOW_Y = 10f;
    public static final float PADDING_FRAME_X = 6f;
    public static final float PADDING_FRAME_Y = 4f;
    public static final float PADDING_CELL_X = 4f;
    public static final float PADDING_CELL_Y = 3f;
    public static final float SPACING_ITEM_X = 6f;
    public static final float SPACING_ITEM_Y = 5f;
    public static final float SPACING_INNER_X = 5f;
    public static final float SPACING_INNER_Y = 4f;
    public static final float INDENT_SPACING = 18f;
    public static final float SCROLLBAR_SIZE = 10f;
    public static final float GRAB_MIN_SIZE = 8f;

    public static final float SPACING_PANEL_COMPACT_X = 4f;
    public static final float SPACING_PANEL_COMPACT_Y = 2f;
    public static final float INSPECTOR_SECTION_GAP = 8f;
    public static final float INSPECTOR_BODY_INDENT = 10f;
    public static final float INSPECTOR_BODY_PAD_BOTTOM = 6f;
    public static final float INSPECTOR_HEADER_PAD_Y = 4f;
    public static final float INSPECTOR_CARD_ROUNDING = 3f;
    public static final float INSPECTOR_ITEM_SPACING_X = 8f;
    public static final float INSPECTOR_ITEM_SPACING_Y = 3f;
    public static final float INSPECTOR_FRAME_PAD_X = 5f;
    public static final float INSPECTOR_FRAME_PAD_Y = 3f;
    public static final float HIERARCHY_INDENT = 14f;

    public static final float FONT_SIZE_UI = 15f;
    public static final float ICON_MIN_ADVANCE = 15f;

    private EditorMetrics() {
    }
}
