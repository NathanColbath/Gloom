package org.llw.studio.physics;

import org.llw.studio.ecs.EntityId;

public final class PhysicsBodyUserData {
    public final EntityId entityId;

    public PhysicsBodyUserData(EntityId entityId) {
        this.entityId = entityId;
    }
}
