package org.llw.studio.editor.theme;

/**
 * Semantic editor color tokens (RGBA 0–1). Mapped to ImGui colors in {@link UnityTheme}.
 */
public final class EditorColors {
  public static final float[] EDITOR_BG = {0.15f, 0.15f, 0.15f, 1f};
  public static final float[] EDITOR_SURFACE = {0.18f, 0.18f, 0.18f, 1f};
  public static final float[] EDITOR_SURFACE_LIGHT = {0.22f, 0.22f, 0.22f, 1f};
  public static final float[] EDITOR_PANEL_BG = {0.20f, 0.20f, 0.20f, 1f};
  public static final float[] EDITOR_PANEL_HEADER = {0.24f, 0.24f, 0.24f, 1f};
  public static final float[] EDITOR_BORDER = {0.10f, 0.10f, 0.10f, 1f};
  public static final float[] EDITOR_BORDER_STRONG = {0.35f, 0.35f, 0.35f, 1f};
  public static final float[] TEXT_PRIMARY = {0.90f, 0.90f, 0.90f, 1f};
  public static final float[] TEXT_SECONDARY = {0.65f, 0.65f, 0.65f, 1f};
  public static final float[] TEXT_MUTED = {0.50f, 0.50f, 0.50f, 1f};
  /** Axis label / sub-label text (labels in multi-field rows, e.g. R/G/B/A, X/Y). */
  public static final float[] TEXT_SUBDUED = {0.55f, 0.55f, 0.55f, 1f};
  public static final float[] ACCENT = {0.26f, 0.59f, 0.98f, 1f};
  public static final float[] ACCENT_HOVER = {0.35f, 0.65f, 1.00f, 1f};
  public static final float[] SELECTION_BG = {0.20f, 0.45f, 0.75f, 0.55f};
  public static final float[] INPUT_BG = {0.12f, 0.12f, 0.12f, 1f};
  public static final float[] BUTTON_BG = {0.24f, 0.24f, 0.24f, 1f};
  public static final float[] BUTTON_HOVER = {0.30f, 0.30f, 0.30f, 1f};
  public static final float[] DANGER = {0.90f, 0.35f, 0.35f, 1f};
  public static final float[] PLAY_ACTIVE = {0.30f, 0.75f, 0.40f, 1f};
  public static final float[] STOP_ACTIVE = {0.85f, 0.30f, 0.30f, 1f};
  public static final float[] LOG_DEBUG = {0.55f, 0.55f, 0.55f, 1f};
  public static final float[] LOG_INFO = {0.85f, 0.85f, 0.85f, 1f};
  public static final float[] LOG_WARN = {0.95f, 0.75f, 0.30f, 1f};
  public static final float[] LOG_ERROR = {0.95f, 0.40f, 0.40f, 1f};
  public static final float[] INSPECTOR_OBJECT_HEADER_BG = {0.21f, 0.21f, 0.21f, 1f};
  public static final float[] COMPONENT_HEADER_BG = {0.23f, 0.23f, 0.23f, 1f};
  public static final float[] COMPONENT_HEADER_HOVER = {0.28f, 0.28f, 0.28f, 1f};
  public static final float[] HIERARCHY_ROW_HOVER = {0.25f, 0.25f, 0.25f, 0.65f};
  public static final float[] HIERARCHY_ROW_SELECTED = {0.20f, 0.45f, 0.75f, 0.55f};
  public static final float[] ASSET_BROWSER_HOVER = {0.28f, 0.28f, 0.28f, 0.85f};

  public static final float INSPECTOR_LABEL_WIDTH = 110f;
  public static final int VIEWPORT_BG_R = 38;
  public static final int VIEWPORT_BG_G = 38;
  public static final int VIEWPORT_BG_B = 38;

  private EditorColors() {}
}
