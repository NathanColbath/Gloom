package org.llw.render.renderables;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.Vertex;
import org.llw.render.backend.RenderBackend;

/**
 * Circle {@link Renderable} approximated by a triangle fan or strip with configurable segment count.
 *
 * <p>The circle is centered at the local origin {@code (0, 0)} with the given radius.
 * Non-uniform scale on the transform can produce an ellipse.
 */
public final class Circle extends AbstractTransformable implements Renderable {
    private float radius = 50f;
    private int pointCount = 48;
    private Color fillColor = Color.WHITE;
    private Color outlineColor = Color.BLACK;
    private float outlineThickness = 1f;
    private boolean filled = true;
    private boolean outlined;

    /**
     * Returns the circle radius in local pixels.
     *
     * @return radius from the origin to the perimeter
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the circle radius in local pixels.
     *
     * @param radius distance from the origin to the perimeter
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns the number of line segments used to approximate the circumference.
     *
     * @return segment count (always at least 3 after {@link #setPointCount(int)})
     */
    public int getPointCount() {
        return pointCount;
    }

    /**
     * Sets the number of segments used to approximate the circumference.
     *
     * <p>Values below 3 are clamped to 3.
     *
     * @param pointCount desired segment count
     */
    public void setPointCount(int pointCount) {
        this.pointCount = Math.max(3, pointCount);
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
     * Returns the outline ring thickness in local pixels.
     *
     * @return border width in pixels
     */
    public float getOutlineThickness() {
        return outlineThickness;
    }

    /**
     * Sets the outline ring thickness in local pixels.
     *
     * @param outlineThickness border width in pixels
     */
    public void setOutlineThickness(float outlineThickness) {
        this.outlineThickness = outlineThickness;
    }

    /**
     * Returns whether the interior is filled when rendered.
     *
     * @return {@code true} if a triangle fan is submitted to the backend
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * Enables or disables filling the circle interior when rendered.
     *
     * @param filled {@code true} to draw a filled disk
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    /**
     * Returns whether an outline ring is drawn when rendered.
     *
     * @return {@code true} if a triangle strip ring is submitted to the backend
     */
    public boolean isOutlined() {
        return outlined;
    }

    /**
     * Enables or disables drawing the outline ring when rendered.
     *
     * @param outlined {@code true} to draw the border ring
     */
    public void setOutlined(boolean outlined) {
        this.outlined = outlined;
    }

    /**
     * Submits fill and/or outline geometry to the backend, combining this circle's transform
     * with any transform in {@code state}.
     *
     * @param backend OpenGL backend used for immediate-mode vertex draws
     * @param state per-draw blend mode, shader, and optional parent transform
     */
    @Override
    public void render(RenderBackend backend, DrawState state) {
        Matrix3x2 model = combineModel(state);
        if (filled) {
            backend.drawVertices(model, buildVertices(radius, fillColor), PrimitiveType.TRIANGLE_FAN, state.shader(), state.blendMode());
        }
        if (outlined) {
            backend.drawVertices(model, buildRingVertices(radius, outlineThickness, outlineColor), PrimitiveType.TRIANGLE_STRIP, state.shader(), state.blendMode());
        }
    }

    private Vertex[] buildVertices(float radius, Color color) {
        Vertex[] vertices = new Vertex[pointCount + 2];
        vertices[0] = new Vertex(0f, 0f, 0f, 0f, color);
        for (int i = 0; i <= pointCount; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / pointCount));
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            vertices[i + 1] = new Vertex(x, y, 0f, 0f, color);
        }
        return vertices;
    }

    private Vertex[] buildRingVertices(float outerRadius, float thickness, Color color) {
        int segments = pointCount + 1;
        Vertex[] vertices = new Vertex[segments * 2];
        float innerRadius = Math.max(0f, outerRadius - thickness);
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / pointCount));
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            vertices[i * 2] = new Vertex(cos * innerRadius, sin * innerRadius, 0f, 0f, color);
            vertices[i * 2 + 1] = new Vertex(cos * outerRadius, sin * outerRadius, 0f, 0f, color);
        }
        return vertices;
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
