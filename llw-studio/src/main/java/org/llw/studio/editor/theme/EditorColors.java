package org.llw.studio.editor.theme;

/**
 * Gloom editor color tokens (RGBA 0–1). Each token has a clear semantic role.
 * Mapped to ImGui colors in {@link GloomTheme}.
 *
 * <p>Palette: deep warm slate backgrounds, warm amber accent, with a distinct
 * hierarchy of surfaces. Semantic colors (play, stop, log) use intuitive
 * greens/reds/ambers rather than the accent color.
 */
public final class EditorColors {
  // ── Backgrounds ───────────────────────────────────────────────────────
  /** Darkest background — main window, popup menu fills. */
  public static final float[] WINDOW_BG       = {0.071f, 0.075f, 0.086f, 1f}; // #121316
  /** Default child/surface background. */
  public static final float[] SURFACE         = {0.098f, 0.106f, 0.118f, 1f}; // #191B1E
  /** Lighter surface — hover states, popup frames, tooltips. */
  public static final float[] SURFACE_LIGHT   = {0.145f, 0.153f, 0.169f, 1f}; // #25272B
  /** Panel background (title bars, collapsible areas). */
  public static final float[] PANEL_BG        = {0.118f, 0.125f, 0.141f, 1f}; // #1E2024
  /** Panel header accent — active title bars, toolbar fills. */
  public static final float[] PANEL_HEADER    = {0.157f, 0.165f, 0.180f, 1f}; // #282A2E
  /** Inspector object header card background. */
  public static final float[] INSPECTOR_OBJECT_HEADER_BG = {0.133f, 0.141f, 0.157f, 1f}; // #222427
  /** Component section header fill. */
  public static final float[] COMPONENT_HEADER_BG       = {0.125f, 0.133f, 0.145f, 1f}; // #202225
  /** Component header hovered state. */
  public static final float[] COMPONENT_HEADER_HOVER    = {0.165f, 0.176f, 0.188f, 1f}; // #2A2D30

  // ── Borders ───────────────────────────────────────────────────────────
  /** Subtle separator and default border. */
  public static final float[] BORDER           = {0.047f, 0.051f, 0.059f, 1f}; // #0C0D0F
  /** Stronger border for active/drag-over states. */
  public static final float[] BORDER_STRONG    = {0.220f, 0.231f, 0.251f, 1f}; // #383B40

  // ── Text ──────────────────────────────────────────────────────────────
  /** Primary body text. */
  public static final float[] TEXT_PRIMARY     = {0.894f, 0.902f, 0.922f, 1f}; // #E4E6EB
  /** Secondary/label text. */
  public static final float[] TEXT_SECONDARY   = {0.608f, 0.631f, 0.663f, 1f}; // #9BA1A9
  /** Muted/hint text. */
  public static final float[] TEXT_MUTED       = {0.424f, 0.447f, 0.478f, 1f}; // #6C727A
  /** Axis label / sub-label text (labels in multi-field rows, e.g. R/G/B/A, X/Y). */
  public static final float[] TEXT_SUBDUED     = {0.608f, 0.631f, 0.663f, 1f}; // same as TEXT_SECONDARY

  // ── Accent (warm amber) ──────────────────────────────────────────────
  /** Primary accent: amber — active tabs, checkmarks, grabbers, active toggles. */
  public static final float[] ACCENT          = {0.831f, 0.573f, 0.259f, 1f}; // #D49242
  /** Accent hover state. */
  public static final float[] ACCENT_HOVER    = {0.875f, 0.639f, 0.341f, 1f}; // #DFA357
  /** Accent active/pressed state. */
  public static final float[] ACCENT_ACTIVE   = {0.769f, 0.510f, 0.196f, 1f}; // #C48232

  // ── Interactive Controls ──────────────────────────────────────────────
  /** Selection highlight (cool blue to contrast with warm accent). */
  public static final float[] SELECTION_BG    = {0.231f, 0.510f, 0.965f, 0.55f}; // #3B82F6 @ 55%
  /** Input field background. */
  public static final float[] INPUT_BG        = {0.055f, 0.063f, 0.071f, 1f}; // #0E1012
  /** Default button background. */
  public static final float[] BUTTON_BG       = {0.157f, 0.165f, 0.180f, 1f}; // #282A2E
  /** Button hover state. */
  public static final float[] BUTTON_HOVER    = {0.188f, 0.196f, 0.212f, 1f}; // #303236
  /** Hierarchy row hover overlay. */
  public static final float[] HIERARCHY_ROW_HOVER     = {0.157f, 0.169f, 0.184f, 0.65f};
  /** Hierarchy row selected overlay. */
  public static final float[] HIERARCHY_ROW_SELECTED  = {0.231f, 0.510f, 0.965f, 0.55f}; // same as SELECTION_BG
  /** Asset browser grid cell hover. */
  public static final float[] ASSET_BROWSER_HOVER     = {0.173f, 0.184f, 0.200f, 0.85f};

  // ── Semantic Colors ───────────────────────────────────────────────────
  /** Danger / error text and icons. */
  public static final float[] DANGER          = {0.937f, 0.267f, 0.267f, 1f}; // #EF4444
  /** Play mode indicator — emerald green. */
  public static final float[] PLAY_ACTIVE     = {0.133f, 0.773f, 0.369f, 1f}; // #22C55E
  /** Stop mode indicator — red. */
  public static final float[] STOP_ACTIVE     = {0.863f, 0.149f, 0.149f, 1f}; // #DC2626

  // ── Console Log Colors ────────────────────────────────────────────────
  public static final float[] LOG_DEBUG       = {0.424f, 0.447f, 0.478f, 1f}; // same as TEXT_MUTED
  public static final float[] LOG_INFO        = {0.894f, 0.902f, 0.922f, 1f}; // same as TEXT_PRIMARY
  public static final float[] LOG_WARN        = {0.961f, 0.620f, 0.043f, 1f}; // #F59E0B
  public static final float[] LOG_ERROR       = {0.937f, 0.267f, 0.267f, 1f}; // same as DANGER

  // ── Table Colors ────────────────────────────────────────────────────
  /** Table header background. */
  public static final float[] TABLE_HEADER_BG     = {0.157f, 0.165f, 0.180f, 1f}; // #282A2E — same as PANEL_HEADER
  /** Strong table border (between header and body, column dividers). */
  public static final float[] TABLE_BORDER_STRONG  = {0.180f, 0.188f, 0.204f, 1f}; // #2E3034
  /** Light table border (row separators). */
  public static final float[] TABLE_BORDER_LIGHT   = {0.118f, 0.125f, 0.141f, 1f}; // #1E2024 — same as PANEL_BG
  /** Default table row background. */
  public static final float[] TABLE_ROW_BG         = {0.098f, 0.106f, 0.118f, 1f}; // #191B1E — same as SURFACE
  /** Alternate table row background. */
  public static final float[] TABLE_ROW_BG_ALT     = {0.110f, 0.118f, 0.133f, 1f}; // #1C1E22

  // ── Plot Colors ──────────────────────────────────────────────────────
  /** Plot line color (line graphs). */
  public static final float[] PLOT_LINES           = {0.608f, 0.631f, 0.663f, 1f}; // #9BA1A9 — same as TEXT_SECONDARY
  /** Plot line hovered/selected. */
  public static final float[] PLOT_LINES_HOVERED   = {0.875f, 0.639f, 0.341f, 1f}; // #DFA357 — same as ACCENT_HOVER
  /** Plot histogram fill. */
  public static final float[] PLOT_HISTOGRAM       = {0.831f, 0.573f, 0.259f, 0.60f}; // #D49242 @ 60%
  /** Plot histogram hovered/selected. */
  public static final float[] PLOT_HISTOGRAM_HOVERED = {0.875f, 0.639f, 0.341f, 0.75f}; // #DFA357 @ 75%

  // ── Navigation / Modal Overlay ───────────────────────────────────────
  /** Navigation windowing highlight (Alt+Tab window picker). */
  public static final float[] NAV_HIGHLIGHT         = {0.831f, 0.573f, 0.259f, 1f}; // #D49242 — same as ACCENT
  /** Navigation windowing dimming overlay. */
  public static final float[] NAV_DIM_BG            = {0.071f, 0.075f, 0.086f, 0.60f}; // #121316 @ 60%
  /** Modal window dimming overlay. */
  public static final float[] MODAL_DIM_BG          = {0.071f, 0.075f, 0.086f, 0.55f}; // #121316 @ 55%

  // ── Layout Constants ──────────────────────────────────────────────────
  /** Fixed label column width for inspector property rows. */
  public static final float INSPECTOR_LABEL_WIDTH = 110f;
  /** Viewport clear colour (RGB 0–255). */
  public static final int VIEWPORT_BG_R = 20;
  public static final int VIEWPORT_BG_G = 20;
  public static final int VIEWPORT_BG_B = 20;

  private EditorColors() {}
}
