package org.llw.studio.ecs.components;

import org.llw.studio.physics.PhysicsBodyType;

/**
 * Unity {@code Rigidbody2D} equivalent.
 */
public final class Rigidbody2DComponent implements Cloneable {
    public PhysicsBodyType bodyType = PhysicsBodyType.DYNAMIC;
    public float mass = 1f;
    public float gravityScale = 1f;
    public float linearDrag;
    public float angularDrag;
    public boolean freezeRotation;
    public boolean simulated = true;
  /** Runtime-synced linear velocity (studio space, Y-down). */
    public float linearVelocityX;
    public float linearVelocityY;
  /** Runtime-synced angular velocity in degrees per second. */
    public float angularVelocity;

    public Rigidbody2DComponent copy() {
        Rigidbody2DComponent copy = new Rigidbody2DComponent();
        copy.bodyType = bodyType;
        copy.mass = mass;
        copy.gravityScale = gravityScale;
        copy.linearDrag = linearDrag;
        copy.angularDrag = angularDrag;
        copy.freezeRotation = freezeRotation;
        copy.simulated = simulated;
        copy.linearVelocityX = linearVelocityX;
        copy.linearVelocityY = linearVelocityY;
        copy.angularVelocity = angularVelocity;
        return copy;
    }

    @Override
    public Rigidbody2DComponent clone() {
        return copy();
    }
}
