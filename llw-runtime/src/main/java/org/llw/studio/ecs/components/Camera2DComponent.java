package org.llw.studio.ecs.components;

/**
 * Orthographic 2D camera settings for viewport rendering.
 * <p>
 * Used in editor scene view and play mode. The camera entity's {@link Transform2DComponent}
 * positions the view in Y-down world space.
 */
public final class Camera2DComponent implements Cloneable {
    /** Half-height of the orthographic view in world units. */
    public float orthographicSize = 360f;
    /** Draw order when multiple cameras are present (higher draws later). */
    public float depth;
    /** When {@code true}, this camera is preferred as the main scene view camera. */
    public boolean mainCamera = true;
    /** Clear color red channel, {@code [0, 1]}. */
    public float backgroundR = 38f / 255f;
    /** Clear color green channel, {@code [0, 1]}. */
    public float backgroundG = 38f / 255f;
    /** Clear color blue channel, {@code [0, 1]}. */
    public float backgroundB = 38f / 255f;
    /** Clear color alpha channel, {@code [0, 1]}. */
    public float backgroundA = 1f;

    /**
     * @return deep copy of this camera settings block
     */
    public Camera2DComponent copy() {
        Camera2DComponent copy = new Camera2DComponent();
        copy.orthographicSize = orthographicSize;
        copy.depth = depth;
        copy.mainCamera = mainCamera;
        copy.backgroundR = backgroundR;
        copy.backgroundG = backgroundG;
        copy.backgroundB = backgroundB;
        copy.backgroundA = backgroundA;
        return copy;
    }

    @Override
    public Camera2DComponent clone() {
        return copy();
    }
}
