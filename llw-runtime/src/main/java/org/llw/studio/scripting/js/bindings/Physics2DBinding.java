package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.physics.PhysicsRaycastHit;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.physics.PlayPhysicsBridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Global {@code Physics2D} script API.
 */
public final class Physics2DBinding {
    @HostAccess.Export
    public double getGravityX() {
        PhysicsWorld world = PlayPhysicsBridge.active();
        return world == null ? 0d : world.gravityX();
    }

    @HostAccess.Export
    public double getGravityY() {
        PhysicsWorld world = PlayPhysicsBridge.active();
        return world == null ? PhysicsWorld.DEFAULT_GRAVITY_Y : world.gravityY();
    }

    @HostAccess.Export
    public void setGravity(double x, double y) {
        PhysicsWorld world = PlayPhysicsBridge.active();
        if (world != null) {
            world.setGravity((float) x, (float) y);
        }
    }

    @HostAccess.Export
    public Object raycast(
            double originX,
            double originY,
            double directionX,
            double directionY,
            double distance,
            int layerMask
    ) {
        PhysicsWorld world = PlayPhysicsBridge.active();
        if (world == null) {
            return null;
        }
        PhysicsRaycastHit hit = world.raycast(
                (float) originX,
                (float) originY,
                (float) directionX,
                (float) directionY,
                (float) distance,
                layerMask
        );
        if (hit == null || hit.entity().isNone()) {
            return null;
        }
        return new RaycastHitBinding(hit);
    }

    @HostAccess.Export
    public Object[] overlapCircle(double x, double y, double radius, int layerMask) {
        PhysicsWorld world = PlayPhysicsBridge.active();
        if (world == null) {
            return new Object[0];
        }
        List<EntityId> entities = world.overlapCircle((float) x, (float) y, (float) radius, layerMask);
        var hostApi = PlayPhysicsBridge.hostApi();
        if (hostApi == null) {
            return new Object[0];
        }
        List<Object> wrapped = new ArrayList<>();
        for (EntityId entity : entities) {
            wrapped.add(hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), entity)));
        }
        return wrapped.toArray();
    }

    public static final class RaycastHitBinding {
        private final PhysicsRaycastHit hit;

        public RaycastHitBinding(PhysicsRaycastHit hit) {
            this.hit = hit;
        }

        @HostAccess.Export
        public double getPointX() {
            return hit.pointX();
        }

        @HostAccess.Export
        public double getPointY() {
            return hit.pointY();
        }

        @HostAccess.Export
        public double getFraction() {
            return hit.fraction();
        }
    }
}
