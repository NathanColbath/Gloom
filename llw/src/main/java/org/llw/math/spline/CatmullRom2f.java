package org.llw.math.spline;

import org.llw.math.vector.Vector2f;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Uniform Catmull-Rom spline through a sequence of control points.
 */
public final class CatmullRom2f {
    private static final Logger log = Log.get(Loggers.MATH);

    private final Vector2f[] points;

    /**
     * Creates a spline through the given control points (minimum 4).
     *
     * @param points control polyline
     */
    public CatmullRom2f(Vector2f... points) {
        if (points.length < 4) {
            log.debug("CatmullRom2f requires at least 4 points, got {}", points.length);
            throw new IllegalArgumentException("Catmull-Rom requires at least 4 points");
        }
        this.points = new Vector2f[points.length];
        for (int i = 0; i < points.length; i++) {
            this.points[i] = points[i].copy();
        }
    }

    /**
     * Evaluates position along the full spline at global parameter {@code t} in [0, 1].
     *
     * @param t normalized arc parameter
     * @return interpolated point
     */
    public Vector2f position(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float scaled = t * (points.length - 3);
        int segment = Math.min((int) scaled, points.length - 4);
        float localT = scaled - segment;
        return catmull(points[segment], points[segment + 1], points[segment + 2], points[segment + 3], localT);
    }

    private static Vector2f catmull(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        float x = 0.5f * ((2f * p1.x)
                + (-p0.x + p2.x) * t
                + (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2
                + (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3);
        float y = 0.5f * ((2f * p1.y)
                + (-p0.y + p2.y) * t
                + (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2
                + (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3);
        return new Vector2f(x, y);
    }
}
