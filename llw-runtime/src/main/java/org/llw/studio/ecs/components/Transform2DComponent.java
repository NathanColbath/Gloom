package org.llw.studio.ecs.components;

/**
 * Local 2D transform relative to the parent entity.
 * <p>
 * Edited in the scene hierarchy and simulated in play mode. {@link #x} and {@link #y} are in
 * Y-down world space (positive Y moves downward on screen).
 */
public final class Transform2DComponent implements Cloneable {
    /** Local X position in parent space (Y-down). */
    public float x;
    /** Local Y position in parent space (Y-down). */
    public float y;
    /** Local rotation in degrees. */
    public float rotation;
    /** Local horizontal scale. */
    public float scaleX = 1f;
    /** Local vertical scale. */
    public float scaleY = 1f;

    /** Creates a transform at the origin with unit scale. */
    public Transform2DComponent() {
    }

    /**
     * @param x local X in parent space (Y-down)
     * @param y local Y in parent space (Y-down)
     */
    public Transform2DComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return deep copy of this transform
     */
    public Transform2DComponent copy() {
        Transform2DComponent copy = new Transform2DComponent(x, y);
        copy.rotation = rotation;
        copy.scaleX = scaleX;
        copy.scaleY = scaleY;
        return copy;
    }

    @Override
    public Transform2DComponent clone() {
        return copy();
    }
}
