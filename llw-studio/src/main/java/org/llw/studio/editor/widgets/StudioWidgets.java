package org.llw.studio.editor.widgets;

/**
 * Thin entry points for shared editor controls used outside the widgets package.
 */
public final class StudioWidgets {
  private StudioWidgets() {}

  public static boolean playButton(boolean playing) {
    return PlayControls.render(playing);
  }
}
