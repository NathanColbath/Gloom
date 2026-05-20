package org.llw.audio.core;

/**
 * Mutable 3D vector used for spatial audio positions and directions.
 */
public final class Vector3f {
    /** X component. */
    public float x;
    /** Y component. */
    public float y;
    /** Z component. */
    public float z;

    /**
     * Creates a zero vector.
     */
    public Vector3f() {
        this(0f, 0f, 0f);
    }

    /**
     * Creates a vector with the given components.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     */
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Sets all components.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     */
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns a copy of this vector.
     *
     * @return a new {@link Vector3f} with the same components
     */
    public Vector3f copy() {
        return new Vector3f(x, y, z);
    }
}
