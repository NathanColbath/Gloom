package org.llw.studio.physics;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.PhysicsBodyRefComponent;

/**
 * Marks ECS transforms as needing a push into Box2D before the next physics step.
 */
public final class PhysicsTransformSync {
    private PhysicsTransformSync() {
    }

    public static void markDirty(World world, EntityId entity) {
        if (world == null || entity == null || entity.isNone()) {
            return;
        }
        PhysicsBodyRefComponent ref = world.getComponent(entity, PhysicsBodyRefComponent.class);
        if (ref != null) {
            ref.transformDirty = true;
        }
    }
}
