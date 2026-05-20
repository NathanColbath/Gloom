package org.llw.studio.ecs.components;

/**
 * Cached world-space 2D transform computed from the hierarchy.
 * <p>
 * Populated by systems during play mode and editor preview. Positions use Y-down
 * coordinates ({@link #worldY} increases downward).
 */
public final class WorldTransformComponent implements Cloneable {
    /** World X position (Y-down space). */
    public float worldX;
    /** World Y position (Y-down space). */
    public float worldY;
    /** World rotation in degrees. */
    public float worldRotation;
    /** Accumulated world horizontal scale. */
    public float worldScaleX = 1f;
    /** Accumulated world vertical scale. */
    public float worldScaleY = 1f;

    /**
     * @return deep copy of this cached world transform
     */
    public WorldTransformComponent copy() {
        WorldTransformComponent copy = new WorldTransformComponent();
        copy.worldX = worldX;
        copy.worldY = worldY;
        copy.worldRotation = worldRotation;
        copy.worldScaleX = worldScaleX;
        copy.worldScaleY = worldScaleY;
        return copy;
    }

    @Override
    public WorldTransformComponent clone() {
        return copy();
    }
}
