package org.llw.studio.physics;

import org.llw.studio.ecs.EntityId;

/**
 * Queued physics message for script dispatch.
 */
public record PhysicsContactEvent(
        PhysicsMessageType type,
        EntityId self,
        EntityId other,
        boolean trigger,
        float relativeVelocityX,
        float relativeVelocityY
) {
}
