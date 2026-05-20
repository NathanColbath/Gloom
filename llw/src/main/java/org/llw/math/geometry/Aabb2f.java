package org.llw.math.geometry;

/**
 * Axis-aligned bounding box stored as min/max corners in Y-down space.
 */
public final class Aabb2f {
    /** Minimum X (left). */
    public float minX;
    /** Minimum Y (top). */
    public float minY;
    /** Maximum X (right). */
    public float maxX;
    /** Maximum Y (bottom). */
    public float maxY;

    /** Creates an empty box at the origin. */
    public Aabb2f() {}

    /**
     * Creates a box from corner coordinates.
     *
     * @param minX left edge
     * @param minY top edge
     * @param maxX right edge
     * @param maxY bottom edge
     */
    public Aabb2f(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Creates a box from center and half-extents.
     *
     * @param centerX center X
     * @param centerY center Y
     * @param halfWidth half width
     * @param halfHeight half height
     * @return new axis-aligned box
     */
    public static Aabb2f fromCenterExtents(float centerX, float centerY, float halfWidth, float halfHeight) {
        return new Aabb2f(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
    }

    /**
     * Returns whether this box overlaps {@code other}.
     *
     * @param other other box
     * @return {@code true} when overlapping
     */
    public boolean overlaps(Aabb2f other) {
        return minX < other.maxX && maxX > other.minX && minY < other.maxY && maxY > other.minY;
    }

    /**
     * Expands this box to include {@code other}.
     *
     * @param other box to merge
     * @return this box for chaining
     */
    public Aabb2f merge(Aabb2f other) {
        minX = Math.min(minX, other.minX);
        minY = Math.min(minY, other.minY);
        maxX = Math.max(maxX, other.maxX);
        maxY = Math.max(maxY, other.maxY);
        return this;
    }

    /**
     * Converts to a {@link RectF}.
     *
     * @return rectangle with the same bounds
     */
    public RectF toRect() {
        return new RectF(minX, minY, maxX - minX, maxY - minY);
    }
}
