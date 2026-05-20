package org.llw.render.graphics;

/**
 * Logical font style for system font lookup.
 */
public enum FontStyle {
    PLAIN(java.awt.Font.PLAIN),
    BOLD(java.awt.Font.BOLD),
    ITALIC(java.awt.Font.ITALIC),
    BOLD_ITALIC(java.awt.Font.BOLD | java.awt.Font.ITALIC);

    private final int awtStyle;

    FontStyle(int awtStyle) {
        this.awtStyle = awtStyle;
    }

    /**
     * @return corresponding {@link java.awt.Font} style constant
     */
    public int toAwtStyle() {
        return awtStyle;
    }

    /**
     * Maps AWT bold/italic flags to a {@link FontStyle}.
     *
     * @param bold   whether the face is bold
     * @param italic whether the face is italic
     * @return matching style
     */
    public static FontStyle fromAwtFlags(boolean bold, boolean italic) {
        if (bold && italic) {
            return BOLD_ITALIC;
        }
        if (bold) {
            return BOLD;
        }
        if (italic) {
            return ITALIC;
        }
        return PLAIN;
    }
}
