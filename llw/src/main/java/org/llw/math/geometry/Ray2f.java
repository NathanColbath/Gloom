package org.llw.math.geometry;

import org.llw.math.vector.Vector2f;

/**
 * Semi-infinite ray in 2D: {@code origin + t * direction} for {@code t >= 0}.
 */
public final class Ray2f {
    private final Vector2f origin = new Vector2f();
    private final Vector2f direction = new Vector2f(1f, 0f);

    /** Creates a ray from the origin pointing right. */
    public Ray2f() {}

    /**
     * Creates a ray with the given origin and direction (need not be normalized).
     *
     * @param ox origin X
     * @param oy origin Y
     * @param dx direction X
     * @param dy direction Y
     */
    public Ray2f(float ox, float oy, float dx, float dy) {
        origin.set(ox, oy);
        direction.set(dx, dy);
    }

    /** Returns the ray origin. */
    public Vector2f origin() {
        return origin;
    }

    /** Returns the ray direction. */
    public Vector2f direction() {
        return direction;
    }

    /**
     * Returns the point at parameter {@code t}.
     *
     * @param t distance along the ray
     * @return point on the ray
     */
    public Vector2f at(float t) {
        return new Vector2f(origin.x + direction.x * t, origin.y + direction.y * t);
    }
}
