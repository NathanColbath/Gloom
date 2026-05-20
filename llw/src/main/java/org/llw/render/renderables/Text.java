package org.llw.render.renderables;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.Renderable;
import org.llw.render.gl.OpenGlBackend;

/**
 * Text {@link Renderable} that lays out glyphs from a {@link Font} atlas at the local origin.
 *
 * <p>Content is drawn starting at {@code (0, 0)} in local space. Newline characters advance
 * the cursor to the next line using the font's line height.
 */
public final class Text extends AbstractTransformable implements Renderable {
    private Font font;
    private String content = "";
    private Color fillColor = Color.WHITE;

    /**
     * Creates a text renderable with the given font and empty content.
     *
     * @param font bitmap font used to resolve glyphs; may be {@code null}, in which case
     *             {@link #render} is a no-op
     */
    public Text(Font font) {
        this.font = font;
    }

    /**
     * Returns the font used to resolve and draw glyphs.
     *
     * @return active font, or {@code null} if none is set
     */
    public Font getFont() {
        return font;
    }

    /**
     * Assigns the font used to resolve and draw glyphs.
     *
     * @param font bitmap font; may be {@code null}
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Returns the string drawn when rendered.
     *
     * @return text content; never {@code null}
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the string drawn when rendered.
     *
     * @param content text to display; {@code null} is stored as an empty string
     */
    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    /**
     * Returns the color applied to each glyph quad.
     *
     * @return fill color in RGBA byte components
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets the color applied to each glyph quad.
     *
     * @param fillColor glyph color in RGBA byte components; must not be {@code null}
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Submits glyph quads to the backend, combining this text object's transform with any
     * transform in {@code state}.
     *
     * <p>Does nothing if the font is {@code null} or content is empty.
     *
     * @param backend OpenGL backend that batches textured glyph quads
     * @param state per-draw blend mode, shader, and optional parent transform
     */
    @Override
    public void render(OpenGlBackend backend, DrawState state) {
        if (font == null || content.isEmpty()) {
            return;
        }
        Matrix3x2 model = combineModel(state);
        backend.drawText(model, font, content, 0f, 0f, fillColor, state.shader(), state.blendMode());
    }

    private Matrix3x2 combineModel(DrawState state) {
        Matrix3x2 model = getTransform();
        if (state.transform() != null) {
            Matrix3x2 combined = state.transform().copy();
            combined.multiply(model);
            return combined;
        }
        return model;
    }
}
