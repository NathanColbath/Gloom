package org.llw.studio.ecs.components;

/**
 * Stable integer id for a scene object within a {@link org.llw.studio.scene.Scene}.
 * <p>
 * Assigned by {@link org.llw.studio.scene.SceneObjectIds} when objects are created in the
 * editor; used to correlate selection, serialization, and play mode instances.
 * {@link #sceneId} {@code -1} means unassigned.
 */
public final class SceneObjectIdComponent implements Cloneable {
    /** Per-scene object id, or {@code -1} if not yet assigned. */
    public int sceneId = -1;

    /**
     * @return deep copy of this scene object id
     */
    public SceneObjectIdComponent copy() {
        SceneObjectIdComponent copy = new SceneObjectIdComponent();
        copy.sceneId = sceneId;
        return copy;
    }

    @Override
    public SceneObjectIdComponent clone() {
        return copy();
    }
}
