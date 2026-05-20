package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.World;
import org.llw.studio.physics.PhysicsContactBridge;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.physics.PlayPhysicsBridge;
import org.llw.studio.systems.TransformSystem;

/**
 * Fixed-timestep Box2D simulation (Unity {@code FixedUpdate}).
 */
public final class PhysicsSystem implements EcsSystem {
    public static final float FIXED_DT = 1f / 50f;

    private final PhysicsWorld physicsWorld;
    private final PhysicsContactBridge contactBridge;
    private final TransformSystem transformSystem = new TransformSystem();
    private float accumulator;

    public PhysicsSystem(PhysicsWorld physicsWorld, PhysicsContactBridge contactBridge) {
        this.physicsWorld = physicsWorld;
        this.contactBridge = contactBridge;
    }

    /** Clears the fixed-timestep accumulator when entering play mode. */
    public void resetFixedStep() {
        accumulator = 0f;
    }

  /**
   * @param world      play scene ECS world
   * @param deltaTime  frame delta seconds
   */
    @Override
    public void onUpdate(World world, float deltaTime) {
        if (physicsWorld == null) {
            return;
        }
        float dt = deltaTime > 0f ? deltaTime : FIXED_DT;
        accumulator += dt;
        int safety = 0;
        while (accumulator >= FIXED_DT && safety < 12) {
            transformSystem.onUpdate(world, 0f);
            physicsWorld.syncTransformsToBodies(world);
            physicsWorld.step(FIXED_DT);
            physicsWorld.syncBodiesToTransforms(world);
            contactBridge.flush(physicsWorld);
            accumulator -= FIXED_DT;
            safety++;
        }
        PlayPhysicsBridge.setActive(physicsWorld);
    }

    public PhysicsWorld physicsWorld() {
        return physicsWorld;
    }
}
