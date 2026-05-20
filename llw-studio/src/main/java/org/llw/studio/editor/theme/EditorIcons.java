package org.llw.studio.editor.theme;

/**
 * Font Awesome 6 solid icon glyphs (merged into the default ImGui font).
 * Requires {@link EditorFonts#load()} at startup.
 */
public final class EditorIcons {
  public static final short ICON_MIN = (short) 0xe005;
  public static final short ICON_MAX = (short) 0xf8ff;
  public static final short[] ICON_RANGE = new short[]{ICON_MIN, ICON_MAX, 0};

  public static final String GAME_OBJECT = "\uf1b2"; // cube
  public static final String COMPONENT = "\uf013"; // cog
  public static final String IMAGE = "\uf03e";
  public static final String SEARCH = "\uf002";
  public static final String PLUS = "\uf067";
  public static final String ELLIPSIS = "\uf141";
  public static final String TIMES = "\uf00d";
  public static final String EYE = "\uf06e";
  public static final String EYE_SLASH = "\uf070";
  public static final String CAMERA = "\uf03d";

  private EditorIcons() {}
}
