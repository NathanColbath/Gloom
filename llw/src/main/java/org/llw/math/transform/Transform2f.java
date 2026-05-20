package org.llw.math.transform;

import org.llw.math.matrix.Matrix3x2;
import org.llw.math.vector.Vector2f;

/**
 * 2D transform with position, rotation, scale, and origin pivot.
 *
 * <p>Produces the same matrix layout used by renderable transforms in the render backend.
 */
public final class Transform2f {
    private final Vector2f position = new Vector2f();
    private final Vector2f scale = new Vector2f(1f, 1f);
    private final Vector2f origin = new Vector2f();
    private float rotation;
    private final Matrix3x2 matrix = new Matrix3x2();
    private boolean dirty = true;

    /** Creates an identity transform at the origin. */
    public Transform2f() {}

    /**
     * Sets the world position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return this transform for chaining
     */
    public Transform2f setPosition(float x, float y) {
        position.set(x, y);
        dirty = true;
        return this;
    }

    /** Returns a copy of the position. */
    public Vector2f getPosition() {
        return position.copy();
    }

    /**
     * Sets the rotation in radians.
     *
     * @param radians rotation angle
     * @return this transform for chaining
     */
    public Transform2f setRotation(float radians) {
        rotation = radians;
        dirty = true;
        return this;
    }

    /** Returns the rotation in radians. */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets non-uniform scale.
     *
     * @param x horizontal scale
     * @param y vertical scale
     * @return this transform for chaining
     */
    public Transform2f setScale(float x, float y) {
        scale.set(x, y);
        dirty = true;
        return this;
    }

    /** Returns a copy of the scale. */
    public Vector2f getScale() {
        return scale.copy();
    }

    /**
     * Sets the local origin (pivot) for rotation and scale.
     *
     * @param x origin X in local space
     * @param y origin Y in local space
     * @return this transform for chaining
     */
    public Transform2f setOrigin(float x, float y) {
        origin.set(x, y);
        dirty = true;
        return this;
    }

    /** Returns a copy of the origin. */
    public Vector2f getOrigin() {
        return origin.copy();
    }

    /** Marks the cached matrix stale for the next {@link #toMatrix()} call. */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Returns the composite transform matrix, recomputing when dirty.
     *
     * @return copy of the current matrix
     */
    public Matrix3x2 toMatrix() {
        if (dirty) {
            matrix.set(Matrix3x2.fromTransform(position, rotation, scale, origin));
            dirty = false;
        }
        return matrix.copy();
    }
}
