package org.llw.studio.editor.theme;

/**
 * @deprecated Renamed to {@link GloomTheme}. This class is a thin forwarding wrapper
 * that will be removed in a future commit.
 */
@Deprecated(since = "2026-05-21", forRemoval = true)
public final class UnityTheme {
  private UnityTheme() {}

  /** Delegates to {@link GloomTheme#apply()}. */
  @SuppressWarnings("DeprecatedIsStillUsed")
  public static void apply() {
    GloomTheme.apply();
  }
}
