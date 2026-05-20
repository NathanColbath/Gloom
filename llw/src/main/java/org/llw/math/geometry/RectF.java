package org.llw.math.geometry;

import org.llw.math.vector.Vector2f;

/**
 * Axis-aligned rectangle in Y-down screen space.
 *
 * <p>Stored as top-left {@code (left, top)} plus {@code width} and {@code height}.
 */
public final class RectF {
    /** Left edge X coordinate. */
    public float left;
    /** Top edge Y coordinate (smaller Y in Y-down space). */
    public float top;
    /** Width along +X. */
    public float width;
    /** Height along +Y. */
    public float height;

    /** Creates a zero rectangle at the origin. */
    public RectF() {
        this(0f, 0f, 0f, 0f);
    }

    /**
     * Creates a rectangle with the given top-left corner and size.
     *
     * @param left X of the left edge
     * @param top Y of the top edge
     * @param width horizontal extent
     * @param height vertical extent
     */
    public RectF(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    /** Returns the X coordinate of the right edge. */
    public float right() {
        return left + width;
    }

    /** Returns the Y coordinate of the bottom edge. */
    public float bottom() {
        return top + height;
    }

    /**
     * Returns whether the point {@code (x, y)} lies inside this rectangle (inclusive edges).
     *
     * @param x point X
     * @param y point Y
     * @return {@code true} when contained
     */
    public boolean contains(float x, float y) {
        return x >= left && x <= right() && y >= top && y <= bottom();
    }

    /**
     * Returns whether this rectangle overlaps {@code other}.
     *
     * @param other other rectangle
     * @return {@code true} when the interiors or edges overlap
     */
    public boolean intersects(RectF other) {
        return left < other.right() && right() > other.left
                && top < other.bottom() && bottom() > other.top;
    }

    /**
     * Returns the intersection of this rectangle with {@code other}, or {@code null} if disjoint.
     *
     * @param other other rectangle
     * @return intersection bounds, or {@code null}
     */
    public RectF intersection(RectF other) {
        float l = Math.max(left, other.left);
        float t = Math.max(top, other.top);
        float r = Math.min(right(), other.right());
        float b = Math.min(bottom(), other.bottom());
        if (l >= r || t >= b) {
            return null;
        }
        return new RectF(l, t, r - l, b - t);
    }

    /**
     * Returns the smallest rectangle enclosing this and {@code other}.
     *
     * @param other other rectangle
     * @return bounding union
     */
    public RectF union(RectF other) {
        float l = Math.min(left, other.left);
        float t = Math.min(top, other.top);
        float r = Math.max(right(), other.right());
        float b = Math.max(bottom(), other.bottom());
        return new RectF(l, t, r - l, b - t);
    }

    /** Returns this rectangle as an {@link Aabb2f}. */
    public Aabb2f asAabb() {
        return new Aabb2f(left, top, right(), bottom());
    }

    /** Returns a copy of this rectangle. */
    public RectF copy() {
        return new RectF(left, top, width, height);
    }
}
