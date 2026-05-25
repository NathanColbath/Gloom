package org.llw.render.renderables;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.math.vector.Vector2f;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.Vertex;
import org.llw.render.backend.RenderBackend;

/**
 * Axis-aligned rectangle {@link Renderable} with optional fill and outlined border.
 *
 * <p>The rectangle is defined in local space from {@code (0, 0)} to {@code (width, height)}.
 * When outlined, the border is drawn as an inner ring with the configured thickness.
 */
public final class Rectangle extends AbstractTransformable implements Renderable {
    private float width = 100f;
    private float height = 100f;
    private Color fillColor = Color.WHITE;
    private Color outlineColor = Color.BLACK;
    private float outlineThickness = 1f;
    private boolean filled = true;
    private boolean outlined;

    /**
     * Returns the rectangle width in local pixels.
     *
     * @return width along the positive X axis
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets the rectangle dimensions in local pixels.
     *
     * @param width  extent along the positive X axis
     * @param height extent along the positive Y axis
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the rectangle height in local pixels.
     *
     * @return height along the positive Y axis
     */
    public float getHeight() {
        return height;
    }

    /**
     * Returns the fill color used when {@link #isFilled()} is {@code true}.
     *
     * @return fill color in RGBA byte components
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets the fill color used when {@link #isFilled()} is {@code true}.
     *
     * @param fillColor fill color in RGBA byte components; must not be {@code null}
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Returns the outline color used when {@link #isOutlined()} is {@code true}.
     *
     * @return outline color in RGBA byte components
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Sets the outline color used when {@link #isOutlined()} is {@code true}.
     *
     * @param outlineColor outline color in RGBA byte components; must not be {@code null}
     */
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    /**
     * Returns the outline thickness in local pixels.
     *
     * @return border width in pixels
     */
    public float getOutlineThickness() {
        return outlineThickness;
    }

    /**
     * Sets the outline thickness in local pixels.
     *
     * @param outlineThickness border width in pixels
     */
    public void setOutlineThickness(float outlineThickness) {
        this.outlineThickness = outlineThickness;
    }

    /**
     * Returns whether the interior is filled when rendered.
     *
     * @return {@code true} if a filled quad is submitted to the backend
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * Enables or disables filling the rectangle interior when rendered.
     *
     * @param filled {@code true} to draw a filled quad
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    /**
     * Returns whether a border ring is drawn when rendered.
     *
     * @return {@code true} if an outline is submitted to the backend
     */
    public boolean isOutlined() {
        return outlined;
    }

    /**
     * Enables or disables drawing the outline border when rendered.
     *
     * @param outlined {@code true} to draw the border ring
     */
    public void setOutlined(boolean outlined) {
        this.outlined = outlined;
    }

    /**
     * Submits fill and/or outline geometry to the backend, combining this rectangle's transform
     * with any transform in {@code state}.
     *
     * @param backend OpenGL backend used for immediate-mode vertex draws
     * @param state per-draw blend mode, shader, and optional parent transform
     */
    @Override
    public void render(RenderBackend backend, DrawState state) {
        Matrix3x2 model = combineModel(state);
        if (filled) {
            Vertex[] vertices = {
                    vertex(0f, 0f, fillColor),
                    vertex(width, 0f, fillColor),
                    vertex(width, height, fillColor),
                    vertex(0f, height, fillColor)
            };
            backend.drawVertices(model, vertices, PrimitiveType.TRIANGLE_FAN, state.shader(), state.blendMode());
        }
        if (outlined) {
            float t = outlineThickness;
            Vertex[] outline = {
                    vertex(0f, 0f, outlineColor),
                    vertex(width, 0f, outlineColor),
                    vertex(width, t, outlineColor),
                    vertex(t, t, outlineColor),
                    vertex(t, height - t, outlineColor),
                    vertex(width - t, height - t, outlineColor),
                    vertex(width - t, height, outlineColor),
                    vertex(0f, height, outlineColor)
            };
            backend.drawVertices(model, outline, PrimitiveType.TRIANGLE_FAN, state.shader(), state.blendMode());
        }
    }

    private static Vertex vertex(float x, float y, Color color) {
        return new Vertex(x, y, 0f, 0f, color);
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
