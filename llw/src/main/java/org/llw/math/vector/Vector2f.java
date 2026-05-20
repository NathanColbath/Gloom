package org.llw.math.vector;

import org.llw.math.util.MathUtils;

/**
 * Mutable two-component vector in Y-down screen space.
 *
 * <p>{@code x} increases to the right; {@code y} increases downward from the top-left origin.
 */
public final class Vector2f {
    /** Horizontal component; increases to the right. */
    public float x;
    /** Vertical component; increases downward. */
    public float y;

    /** Creates a zero vector {@code (0, 0)}. */
    public Vector2f() {
        this(0f, 0f);
    }

    /**
     * Creates a vector with the given components.
     *
     * @param x horizontal component (+ right)
     * @param y vertical component (+ down)
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a copy of {@code other}.
     *
     * @param other source vector
     */
    public Vector2f(Vector2f other) {
        this(other.x, other.y);
    }

    /**
     * Replaces both components with {@code (x, y)}.
     *
     * @param x horizontal component
     * @param y vertical component
     * @return this vector for chaining
     */
    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Copies components from {@code other}.
     *
     * @param other source vector
     * @return this vector for chaining
     */
    public Vector2f set(Vector2f other) {
        return set(other.x, other.y);
    }

    /**
     * Adds {@code (x, y)} to this vector in place.
     *
     * @param x horizontal delta
     * @param y vertical delta
     * @return this vector for chaining
     */
    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds {@code other} to this vector in place.
     *
     * @param other vector to add
     * @return this vector for chaining
     */
    public Vector2f add(Vector2f other) {
        return add(other.x, other.y);
    }

    /**
     * Subtracts {@code other} from this vector in place.
     *
     * @param other vector to subtract
     * @return this vector for chaining
     */
    public Vector2f subtract(Vector2f other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    /**
     * Multiplies both components by {@code factor} in place.
     *
     * @param factor uniform scale factor
     * @return this vector for chaining
     */
    public Vector2f scale(float factor) {
        x *= factor;
        y *= factor;
        return this;
    }

    /**
     * Returns the Euclidean length of this vector.
     *
     * @return magnitude √(x² + y²)
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Returns the squared length (avoids {@code sqrt}).
     *
     * @return x² + y²
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Scales this vector to unit length in place.
     *
     * @return this vector for chaining
     */
    public Vector2f normalize() {
        float len = length();
        if (len > 0f) {
            x /= len;
            y /= len;
        }
        return this;
    }

    /**
     * Returns the dot product with {@code other}.
     *
     * @param other second vector
     * @return scalar dot product
     */
    public float dot(Vector2f other) {
        return x * other.x + y * other.y;
    }

    /**
     * Returns the 2D perpendicular vector {@code (-y, x)} (90° counter-clockwise in Y-down space).
     *
     * @return new perpendicular vector
     */
    public Vector2f perp() {
        return new Vector2f(-y, x);
    }

    /**
     * Returns the Euclidean distance to {@code other}.
     *
     * @param other target point
     * @return distance between the two points
     */
    public float distance(Vector2f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns the angle from this vector to {@code other} in radians.
     *
     * @param other second direction
     * @return signed angle in [-π, π]
     */
    public float angleBetween(Vector2f other) {
        return (float) Math.atan2(perp().dot(other), dot(other));
    }

    /**
     * Projects this vector onto {@code onto} and returns the result as a new vector.
     *
     * @param onto axis to project onto (need not be normalized)
     * @return projection of this onto {@code onto}
     */
    public Vector2f project(Vector2f onto) {
        float denom = onto.lengthSquared();
        if (denom == 0f) {
            return new Vector2f();
        }
        float scale = dot(onto) / denom;
        return new Vector2f(onto.x * scale, onto.y * scale);
    }

    /**
     * Returns the component of this vector rejected from {@code onto}.
     *
     * @param onto axis to reject from
     * @return {@code this - project(onto)}
     */
    public Vector2f reject(Vector2f onto) {
        Vector2f p = project(onto);
        return new Vector2f(x - p.x, y - p.y);
    }

    /**
     * Linearly interpolates between {@code a} and {@code b}.
     *
     * @param a start
     * @param b end
     * @param t interpolation factor, typically in [0, 1]
     * @return interpolated vector
     */
    public static Vector2f lerp(Vector2f a, Vector2f b, float t) {
        return new Vector2f(
                MathUtils.lerp(a.x, b.x, t),
                MathUtils.lerp(a.y, b.y, t)
        );
    }

    /**
     * Returns whether this vector is approximately equal to {@code other}.
     *
     * @param other comparison vector
     * @param epsilon tolerance
     * @return {@code true} when components differ by at most {@code epsilon}
     */
    public boolean isApproxEqual(Vector2f other, float epsilon) {
        return MathUtils.approxEqual(x, other.x, epsilon)
                && MathUtils.approxEqual(y, other.y, epsilon);
    }

    /**
     * Returns a new vector with the same components as this one.
     *
     * @return a copy of this vector
     */
    public Vector2f copy() {
        return new Vector2f(x, y);
    }

    @Override
    public String toString() {
        return "Vector2f(" + x + ", " + y + ")";
    }
}
