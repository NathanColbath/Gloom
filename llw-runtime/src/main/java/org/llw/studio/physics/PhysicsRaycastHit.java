package org.llw.studio.physics;

import org.llw.studio.ecs.EntityId;

public record PhysicsRaycastHit(
        EntityId entity,
        float pointX,
        float pointY,
        float normalX,
        float normalY,
        float fraction
) {
}
