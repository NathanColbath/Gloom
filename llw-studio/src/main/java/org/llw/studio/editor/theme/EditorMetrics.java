package org.llw.studio.editor.theme;

/**
 * Shared ImGui layout metrics for the Gloom editor theme.
 * Used by {@link GloomTheme}, {@link EditorStyle}, and draw-list widgets.
 */
public final class EditorMetrics {
  public static final float ROUNDING_WINDOW = 6f;
  public static final float ROUNDING_CHILD = 6f;
  public static final float ROUNDING_FRAME = 4f;
  public static final float ROUNDING_TAB = 4f;
  public static final float ROUNDING_POPUP = 4f;
  public static final float ROUNDING_GRAB = 4f;

  public static final float PADDING_WINDOW_X = 12f;
  public static final float PADDING_WINDOW_Y = 10f;
  public static final float PADDING_FRAME_X = 6f;
  public static final float PADDING_FRAME_Y = 4f;
  public static final float SPACING_ITEM_X = 6f;
  public static final float SPACING_ITEM_Y = 4f;
  public static final float SPACING_INNER_X = 4f;
  public static final float SPACING_INNER_Y = 3f;

  /** Tighter spacing inside hierarchy / inspector list panels. */
  public static final float SPACING_PANEL_COMPACT_X = 4f;
  public static final float SPACING_PANEL_COMPACT_Y = 2f;

  /** Gap between inspector component cards. */
  public static final float INSPECTOR_SECTION_GAP = 6f;
  /** Indent inside a component card body. */
  public static final float INSPECTOR_BODY_INDENT = 6f;
  /** Hierarchy tree indent per level. */
  public static final float HIERARCHY_INDENT = 14f;

  public static final float FONT_SIZE_UI = 14f;
  public static final float ICON_MIN_ADVANCE = 14f;

  public static final float CHILD_BORDER_SIZE = 1f;

  private EditorMetrics() {}
}
