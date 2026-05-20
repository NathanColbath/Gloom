package org.llw.studio.ecs.components;

/**
 * Per-entity self active flag; effective visibility also depends on ancestors.
 * <p>
 * Honored in editor and play mode. Combine with {@link org.llw.studio.scene.ActiveUtility}
 * to test whether an entity is effectively active in the hierarchy.
 */
public final class ActiveComponent implements Cloneable {
    /** When {@code false}, this entity is inactive regardless of parent state. */
    public boolean selfActive = true;

    /**
     * @return deep copy of this active flag
     */
    public ActiveComponent copy() {
        ActiveComponent copy = new ActiveComponent();
        copy.selfActive = selfActive;
        return copy;
    }

    @Override
    public ActiveComponent clone() {
        return copy();
    }
}
