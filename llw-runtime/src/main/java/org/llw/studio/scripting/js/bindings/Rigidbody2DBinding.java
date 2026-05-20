package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.PhysicsBodyRefComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.physics.PhysicsBodyType;
import org.llw.studio.physics.PhysicsCoordinates;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.physics.PlayPhysicsBridge;

/**
 * Unity {@code Rigidbody2D} host binding.
 */
public final class Rigidbody2DBinding {
    private final ScriptContext context;
    private final EntityId entity;

    public Rigidbody2DBinding(ScriptContext context, EntityId entity) {
        this.context = context;
        this.entity = entity;
    }

    @HostAccess.Export
    public String getBodyType() {
        Rigidbody2DComponent rb = component();
        return rb == null ? PhysicsBodyType.DYNAMIC.name() : rb.bodyType.name();
    }

    @HostAccess.Export
    public void setBodyType(String type) {
        Rigidbody2DComponent rb = component();
        if (rb != null) {
            rb.bodyType = PhysicsBodyType.valueOf(type);
        }
    }

    @HostAccess.Export
    public double getMass() {
        Rigidbody2DComponent rb = component();
        return rb == null ? 1d : rb.mass;
    }

    @HostAccess.Export
    public void setMass(double mass) {
        Rigidbody2DComponent rb = component();
        if (rb != null) {
            rb.mass = (float) mass;
        }
    }

    @HostAccess.Export
    public double getGravityScale() {
        Rigidbody2DComponent rb = component();
        return rb == null ? 1d : rb.gravityScale;
    }

    @HostAccess.Export
    public void setGravityScale(double scale) {
        Rigidbody2DComponent rb = component();
        if (rb != null) {
            rb.gravityScale = (float) scale;
        }
    }

    @HostAccess.Export
    public double getVelocityX() {
        Rigidbody2DComponent rb = component();
        return rb == null ? 0d : rb.linearVelocityX;
    }

    @HostAccess.Export
    public double getVelocityY() {
        Rigidbody2DComponent rb = component();
        return rb == null ? 0d : rb.linearVelocityY;
    }

    @HostAccess.Export
    public void setVelocity(double x, double y) {
        Body body = body();
        if (body != null) {
            body.setLinearVelocity(PhysicsCoordinates.studioVectorToBox2d((float) x, (float) y));
        }
        Rigidbody2DComponent rb = component();
        if (rb != null) {
            rb.linearVelocityX = (float) x;
            rb.linearVelocityY = (float) y;
        }
    }

    @HostAccess.Export
    public void addForce(double x, double y) {
        Body body = body();
        if (body != null) {
            Vec2 force = PhysicsCoordinates.studioVectorToBox2d((float) x, (float) y);
            body.applyForceToCenter(force);
        }
    }

    @HostAccess.Export
    public void movePosition(double x, double y) {
        Body body = body();
        if (body != null) {
            body.setTransform(PhysicsCoordinates.studioToBox2d((float) x, (float) y), body.getAngle());
        }
    }

    @HostAccess.Export
    public boolean getFreezeRotation() {
        Rigidbody2DComponent rb = component();
        return rb != null && rb.freezeRotation;
    }

    @HostAccess.Export
    public void setFreezeRotation(boolean freeze) {
        Rigidbody2DComponent rb = component();
        if (rb != null) {
            rb.freezeRotation = freeze;
        }
        Body body = body();
        if (body != null) {
            body.setFixedRotation(freeze);
        }
    }

    private Rigidbody2DComponent component() {
        return context.world().getComponent(entity, Rigidbody2DComponent.class);
    }

    private Body body() {
        PhysicsWorld world = PlayPhysicsBridge.active();
        if (world != null) {
            return world.bodyFor(entity);
        }
        PhysicsBodyRefComponent ref = context.world().getComponent(entity, PhysicsBodyRefComponent.class);
        return ref == null ? null : ref.body;
    }
}
