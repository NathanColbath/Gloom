package org.llw.studio.scene;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ActiveComponent;

/**
 * Hierarchy-aware active checks combining {@link ActiveComponent} flags.
 * <p>
 * Used by editor gizmos and play mode systems to skip inactive branches. Walks parents via
 * {@link org.llw.studio.ecs.components.HierarchyComponent}; does not use world coordinates.
 */
public final class ActiveUtility {
    private ActiveUtility() {
    }

    /**
     * Returns whether {@code entity} and every ancestor with an {@link ActiveComponent}
     * have {@link ActiveComponent#selfActive} {@code true}.
     *
     * @param world   scene world containing hierarchy and active components
     * @param entity  entity to test
     * @return {@code false} if any ancestor (including {@code entity}) is explicitly inactive
     */
    public static boolean isEffectivelyActive(World world, EntityId entity) {
        EntityId current = entity;
        while (!current.isNone() && world.isAlive(current)) {
            ActiveComponent active = world.getComponent(current, ActiveComponent.class);
            if (active != null && !active.selfActive) {
                return false;
            }
            var hierarchy = world.getComponent(current, org.llw.studio.ecs.components.HierarchyComponent.class);
            if (hierarchy == null || hierarchy.parentIndex < 0) {
                break;
            }
            current = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
        }
        return true;
    }
}
