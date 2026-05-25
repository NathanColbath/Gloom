package org.llw.studio.editor.theme;

/**
 * Modern flat gray editor palette (RGBA 0–1). Mapped to ImGui in {@link GloomTheme}.
 *
 * <p>Change colors here only; widgets should use these tokens or {@link EditorStyle} helpers.
 */
public final class EditorColors {
    // ── Backgrounds ───────────────────────────────────────────────────────
    public static final float[] WINDOW_BG = ThemeColors.rgb(30, 30, 30);
    public static final float[] SURFACE = ThemeColors.rgb(37, 37, 38);
    public static final float[] SURFACE_LIGHT = ThemeColors.rgb(45, 45, 48);
    public static final float[] PANEL_BG = ThemeColors.rgb(37, 37, 38);
    public static final float[] PANEL_HEADER = ThemeColors.rgb(51, 51, 55);
    public static final float[] INSPECTOR_OBJECT_HEADER_BG = ThemeColors.rgb(40, 40, 42);
    public static final float[] INSPECTOR_COMPONENT_BG = ThemeColors.rgb(34, 34, 36);
    public static final float[] INSPECTOR_COMPONENT_BORDER = ThemeColors.rgb(50, 50, 54);
    public static final float[] INSPECTOR_COMPONENT_HEADER = ThemeColors.rgb(42, 42, 45);
    public static final float[] COMPONENT_HEADER_BG = INSPECTOR_COMPONENT_HEADER;
    public static final float[] COMPONENT_HEADER_HOVER = ThemeColors.rgb(52, 52, 56);

    // ── Borders ───────────────────────────────────────────────────────────
    public static final float[] BORDER = ThemeColors.rgb(58, 58, 61);
    public static final float[] BORDER_STRONG = ThemeColors.rgb(74, 74, 80);

    // ── Text ──────────────────────────────────────────────────────────────
    public static final float[] TEXT_PRIMARY = ThemeColors.rgb(212, 212, 212);
    public static final float[] TEXT_SECONDARY = ThemeColors.rgb(168, 168, 168);
    public static final float[] TEXT_MUTED = ThemeColors.rgb(119, 119, 119);
    public static final float[] TEXT_SUBDUED = TEXT_SECONDARY;

    // ── Accent (muted blue-gray, use sparingly) ───────────────────────────
    public static final float[] ACCENT = ThemeColors.rgb(106, 140, 175);
    public static final float[] ACCENT_HOVER = ThemeColors.rgb(122, 156, 191);
    public static final float[] ACCENT_ACTIVE = ThemeColors.rgb(90, 124, 159);

    // ── Interactive ───────────────────────────────────────────────────────
    public static final float[] SELECTION_BG = ThemeColors.rgba(76, 86, 106, 140);
    public static final float[] INPUT_BG = ThemeColors.rgb(32, 33, 36);
    public static final float[] BUTTON_BG = ThemeColors.rgb(52, 52, 54);
    public static final float[] BUTTON_HOVER = ThemeColors.rgb(64, 64, 69);
    public static final float[] BUTTON_ACTIVE = ThemeColors.rgb(74, 74, 80);
    public static final float[] HIERARCHY_ROW_HOVER = ThemeColors.rgba(64, 64, 69, 166);
    public static final float[] HIERARCHY_ROW_SELECTED = SELECTION_BG;
    public static final float[] ASSET_BROWSER_HOVER = ThemeColors.rgba(64, 64, 69, 217);

    // ── Semantic ───────────────────────────────────────────────────────────
    public static final float[] DANGER = ThemeColors.rgb(200, 90, 90);
    public static final float[] PLAY_ACTIVE = ThemeColors.rgb(100, 180, 120);
    public static final float[] STOP_ACTIVE = ThemeColors.rgb(200, 90, 90);

    // ── Console ───────────────────────────────────────────────────────────
    public static final float[] LOG_DEBUG = TEXT_MUTED;
    public static final float[] LOG_INFO = TEXT_PRIMARY;
    public static final float[] LOG_WARN = ThemeColors.rgb(220, 170, 80);
    public static final float[] LOG_ERROR = DANGER;

    // ── Tables ────────────────────────────────────────────────────────────
    public static final float[] TABLE_HEADER_BG = PANEL_HEADER;
    public static final float[] TABLE_BORDER_STRONG = BORDER_STRONG;
    public static final float[] TABLE_BORDER_LIGHT = BORDER;
    public static final float[] TABLE_ROW_BG = SURFACE;
    public static final float[] TABLE_ROW_BG_ALT = SURFACE_LIGHT;

    // ── Plots ─────────────────────────────────────────────────────────────
    public static final float[] PLOT_LINES = TEXT_SECONDARY;
    public static final float[] PLOT_LINES_HOVERED = ACCENT_HOVER;
    public static final float[] PLOT_HISTOGRAM = ThemeColors.rgba(106, 140, 175, 153);
    public static final float[] PLOT_HISTOGRAM_HOVERED = ThemeColors.rgba(122, 156, 191, 191);

    // ── Navigation / modal ─────────────────────────────────────────────────
    public static final float[] NAV_HIGHLIGHT = ACCENT;
    public static final float[] NAV_DIM_BG = ThemeColors.rgba(30, 30, 30, 153);
    public static final float[] MODAL_DIM_BG = ThemeColors.rgba(30, 30, 30, 140);

    // ── Animation timeline (draw lists) ───────────────────────────────────
    public static final float[] TIMELINE_GRID = ThemeColors.rgb(42, 42, 42);
    public static final float[] TIMELINE_GRID_MINOR = ThemeColors.rgb(34, 34, 34);
    public static final float[] TIMELINE_RULER = ThemeColors.rgb(30, 30, 30);
    public static final float[] TIMELINE_RULER_TEXT = TEXT_SECONDARY;
    public static final float[] TIMELINE_RULER_TICK = ThemeColors.rgb(136, 136, 136);
    public static final float[] TIMELINE_PLAYHEAD = ThemeColors.rgb(212, 212, 212);
    public static final float[] TIMELINE_KEYFRAME = ThemeColors.rgb(224, 224, 224);
    public static final float[] TIMELINE_KEYFRAME_SELECTED = ThemeColors.rgb(200, 180, 120);
    public static final float[] TIMELINE_DROP = ThemeColors.rgba(106, 140, 175, 153);
    public static final float[] TIMELINE_SHEET = ThemeColors.rgb(24, 24, 24);

    // ── Shader graph canvas (draw lists) ────────────────────────────────────
    public static final float[] SHADER_GRID = ThemeColors.rgba(255, 255, 255, 32);
    public static final float[] SHADER_GRID_AXIS = ThemeColors.rgba(255, 255, 255, 80);
    public static final float[] SHADER_NODE_BG = ThemeColors.rgb(30, 30, 36);
    public static final float[] SHADER_NODE_HEADER = ThemeColors.rgb(42, 58, 80);
    public static final float[] SHADER_NODE_HEADER_SELECTED = ThemeColors.rgb(58, 74, 96);
    public static final float[] SHADER_NODE_HEADER_ERROR = ThemeColors.rgb(68, 52, 80);
    public static final float[] SHADER_NODE_TEXT = TEXT_PRIMARY;
    public static final float[] SHADER_PIN_TEXT = TEXT_SECONDARY;
    public static final float[] SHADER_LINK = ThemeColors.rgba(136, 204, 255, 255);
    public static final float[] SHADER_LINK_SELECTED = ThemeColors.rgba(255, 204, 68, 255);
    public static final float[] SHADER_LINK_PREVIEW = ThemeColors.rgba(170, 238, 255, 255);
    public static final float[] SHADER_PIN_FLOAT = ThemeColors.rgb(136, 200, 136);
    public static final float[] SHADER_PIN_VEC2 = ThemeColors.rgb(136, 180, 220);
    public static final float[] SHADER_PIN_VEC3 = ThemeColors.rgb(220, 200, 136);
    public static final float[] SHADER_PIN_VEC4 = ThemeColors.rgb(220, 136, 180);

    public static final float INSPECTOR_LABEL_WIDTH = 110f;
    public static final int VIEWPORT_BG_R = 30;
    public static final int VIEWPORT_BG_G = 30;
    public static final int VIEWPORT_BG_B = 30;

    private EditorColors() {
    }
}
