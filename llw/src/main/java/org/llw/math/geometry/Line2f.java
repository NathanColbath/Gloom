package org.llw.math.geometry;

import org.llw.math.vector.Vector2f;

/**
 * Line segment between two endpoints in 2D.
 */
public final class Line2f {
    private final Vector2f start = new Vector2f();
    private final Vector2f end = new Vector2f();

    /** Creates a zero-length segment at the origin. */
    public Line2f() {}

    /**
     * Creates a segment from {@code start} to {@code end}.
     *
     * @param x0 start X
     * @param y0 start Y
     * @param x1 end X
     * @param y1 end Y
     */
    public Line2f(float x0, float y0, float x1, float y1) {
        start.set(x0, y0);
        end.set(x1, y1);
    }

    /**
     * Returns the start point.
     *
     * @return mutable start endpoint
     */
    public Vector2f start() {
        return start;
    }

    /**
     * Returns the end point.
     *
     * @return mutable end endpoint
     */
    public Vector2f end() {
        return end;
    }

    /**
     * Returns the closest point on this segment to {@code point}.
     *
     * @param point query point
     * @return closest point on the segment
     */
    public Vector2f closestPoint(Vector2f point) {
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float lenSq = dx * dx + dy * dy;
        if (lenSq == 0f) {
            return start.copy();
        }
        float t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lenSq;
        t = Math.max(0f, Math.min(1f, t));
        return new Vector2f(start.x + dx * t, start.y + dy * t);
    }

    /**
     * Returns the distance from {@code point} to this segment.
     *
     * @param point query point
     * @return shortest distance
     */
    public float distanceTo(Vector2f point) {
        return point.distance(closestPoint(point));
    }
}
