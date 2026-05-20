package org.llw.math.spline;

import org.llw.math.vector.Vector2f;

/**
 * Cubic Bézier curve in 2D with control points p0–p3.
 */
public final class CubicBezier2f {
    private final Vector2f p0;
    private final Vector2f p1;
    private final Vector2f p2;
    private final Vector2f p3;

    /**
     * Creates a cubic Bézier from four control points.
     *
     * @param p0 start point
     * @param p1 first handle
     * @param p2 second handle
     * @param p3 end point
     */
    public CubicBezier2f(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3) {
        this.p0 = p0.copy();
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        this.p3 = p3.copy();
    }

    /**
     * Evaluates position at parameter {@code t} in [0, 1].
     *
     * @param t curve parameter
     * @return point on the curve
     */
    public Vector2f position(float t) {
        float u = 1f - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        float x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x;
        float y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y;
        return new Vector2f(x, y);
    }

    /**
     * Evaluates the tangent vector at {@code t}.
     *
     * @param t curve parameter
     * @return tangent direction (not normalized)
     */
    public Vector2f tangent(float t) {
        float u = 1f - t;
        float x = 3f * u * u * (p1.x - p0.x) + 6f * u * t * (p2.x - p1.x) + 3f * t * t * (p3.x - p2.x);
        float y = 3f * u * u * (p1.y - p0.y) + 6f * u * t * (p2.y - p1.y) + 3f * t * t * (p3.y - p2.y);
        return new Vector2f(x, y);
    }
}
