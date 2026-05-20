package org.llw.render.graphics;

import org.llw.math.matrix.Matrix3x2;
import org.llw.math.vector.Vector2f;

/**
 * SFML-style 2D transform: position, rotation, scale, and origin in Y-down world space.
 *
 * <p>{@link #getTransform()} composes translation, rotation about the origin, and scale
 * into a single {@link Matrix3x2} suitable for {@link DrawState#withTransform(Matrix3x2)}
 * or {@link DrawState#combineTransform(Matrix3x2)}.
 */
public interface Transformable {
    /**
     * Returns the world-space position (top-left reference unless overridden by origin).
     *
     * @return current position in Y-down world units
     */
    Vector2f getPosition();

    /**
     * Sets the world-space position.
     *
     * @param x X coordinate in world units
     * @param y Y coordinate in world units (Y-down)
     */
    void setPosition(float x, float y);

    /**
     * Sets the world-space position from a vector.
     *
     * @param position new position
     */
    void setPosition(Vector2f position);

    /**
     * Returns the rotation angle in radians.
     *
     * @return rotation about the origin, counter-clockwise in Y-down space
     */
    float getRotation();

    /**
     * Sets the rotation angle in radians.
     *
     * @param radians rotation about the origin
     */
    void setRotation(float radians);

    /**
     * Returns the non-uniform scale factors.
     *
     * @return scale along X and Y
     */
    Vector2f getScale();

    /**
     * Sets the scale factors.
     *
     * @param x horizontal scale
     * @param y vertical scale
     */
    void setScale(float x, float y);

    /**
     * Sets the scale from a vector.
     *
     * @param scale horizontal and vertical scale factors
     */
    void setScale(Vector2f scale);

    /**
     * Returns the local origin used as the pivot for rotation and scaling.
     *
     * @return origin offset in local space (Y-down)
     */
    Vector2f getOrigin();

    /**
     * Sets the local origin used as the pivot for rotation and scaling.
     *
     * @param x origin X in local units
     * @param y origin Y in local units
     */
    void setOrigin(float x, float y);

    /**
     * Sets the local origin from a vector.
     *
     * @param origin pivot offset in local space
     */
    void setOrigin(Vector2f origin);

    /**
     * Returns the composed model matrix for this transform.
     *
     * @return translation, rotation, and scale combined for drawing
     */
    Matrix3x2 getTransform();
}
