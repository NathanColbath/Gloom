package org.llw.render.gl;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.ShaderProgram;

/**
 * Lays out a string as batched textured glyph quads through an {@link OpenGlBackend}.
 *
 * <p>Newline characters reset the horizontal cursor and advance by the font line height.
 * Missing glyphs are skipped without advancing the cursor beyond their advance width.
 */
public final class TextRenderer {
    private final OpenGlBackend backend;

    /**
     * Creates a text renderer that submits glyph quads through the given backend.
     *
     * @param backend OpenGL backend used for {@link OpenGlBackend#drawTexturedQuad} calls
     */
    public TextRenderer(OpenGlBackend backend) {
        this.backend = backend;
    }

    /**
     * Draws {@code text} starting at {@code (x, y)} in local space under {@code model}.
     *
     * <p>Flushes the sprite batch after all glyphs are enqueued.
     *
     * @param stateTracker deduplicates GL state for underlying sprite draws
     * @param shader       shader program for glyph quads
     * @param blendMode    blending applied to each glyph quad
     * @param model        transform applied to each glyph quad
     * @param font         bitmap font providing glyphs and atlas texture
     * @param text         string to render
     * @param x            starting X in local space
     * @param y            starting Y in local space
     * @param color        glyph tint in RGBA byte components
     */
    public void draw(
            GlStateTracker stateTracker,
            ShaderProgram shader,
            BlendMode blendMode,
            Matrix3x2 model,
            Font font,
            String text,
            float x,
            float y,
            Color color
    ) {
        float cursorX = x;
        float cursorY = y;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\n') {
                cursorX = x;
                cursorY += font.lineHeight();
                continue;
            }

            Font.Glyph glyph = font.glyph(character);
            if (glyph == null) {
                continue;
            }

            backend.drawTexturedQuad(
                    model,
                    cursorX + glyph.x0(), cursorY + glyph.y0(),
                    cursorX + glyph.x1(), cursorY + glyph.y1(),
                    glyph.s0(), glyph.t0(), glyph.s1(), glyph.t1(),
                    color,
                    font.atlas(),
                    shader,
                    blendMode
            );
            cursorX += glyph.advance();
        }
        backend.flushSprites();
    }
}
