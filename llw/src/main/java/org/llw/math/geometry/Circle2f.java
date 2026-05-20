package org.llw.math.geometry;

import org.llw.math.vector.Vector2f;

/**
 * Circle in 2D defined by a center and radius.
 */
public final class Circle2f {
    private final Vector2f center = new Vector2f();
    private float radius;

    /** Creates a unit circle at the origin. */
    public Circle2f() {
        radius = 1f;
    }

    /**
     * Creates a circle with the given center and radius.
     *
     * @param cx center X
     * @param cy center Y
     * @param radius radius (non-negative)
     */
    public Circle2f(float cx, float cy, float radius) {
        center.set(cx, cy);
        this.radius = radius;
    }

    /**
     * Returns the center.
     *
     * @return mutable center vector
     */
    public Vector2f center() {
        return center;
    }

    /**
     * Returns the radius.
     *
     * @return circle radius
     */
    public float radius() {
        return radius;
    }

    /**
     * Sets the radius.
     *
     * @param radius new radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns whether the point lies inside the circle (inclusive boundary).
     *
     * @param x point X
     * @param y point Y
     * @return {@code true} when inside
     */
    public boolean contains(float x, float y) {
        float dx = x - center.x;
        float dy = y - center.y;
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Returns whether this circle overlaps {@code other}.
     *
     * @param other other circle
     * @return {@code true} when overlapping
     */
    public boolean overlaps(Circle2f other) {
        float dx = center.x - other.center.x;
        float dy = center.y - other.center.y;
        float r = radius + other.radius;
        return dx * dx + dy * dy <= r * r;
    }

    /**
     * Returns whether this circle overlaps {@code rect}.
     *
     * @param rect axis-aligned rectangle
     * @return {@code true} when overlapping
     */
    public boolean overlaps(RectF rect) {
        float closestX = Math.max(rect.left, Math.min(center.x, rect.right()));
        float closestY = Math.max(rect.top, Math.min(center.y, rect.bottom()));
        float dx = center.x - closestX;
        float dy = center.y - closestY;
        return dx * dx + dy * dy <= radius * radius;
    }
}
