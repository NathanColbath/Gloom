package org.llw.studio.ui;

import org.llw.render.graphics.Font;

/**
 * Text measurement helpers for UI layout.
 */
public final class UiTextMetrics {
    private UiTextMetrics() {
    }

    /**
     * @param font bitmap font
     * @param text string to measure
     * @return horizontal advance in pixels
     */
    public static float measureWidth(Font font, String text) {
        if (font == null || text == null || text.isEmpty()) {
            return 0f;
        }
        float width = 0f;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\n') {
                break;
            }
            Font.Glyph glyph = font.glyph(character);
            if (glyph != null) {
                width += glyph.advance();
            }
        }
        return width;
    }
}
