package org.llw.studio.particles.systems;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.physics.PhysicsWorld;

/**
 * Advances particle simulation for all {@link ParticleEmitterComponent} entities.
 */
public final class ParticleSimulationSystem implements EcsSystem {
    private final AssetDatabase assets;
    private final ParticleWorld particleWorld;
    private PhysicsWorld physicsWorld;

    public ParticleSimulationSystem(AssetDatabase assets, ParticleWorld particleWorld) {
        this.assets = assets;
        this.particleWorld = particleWorld;
    }

    public void setPhysicsWorld(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    public ParticleWorld particleWorld() {
        return particleWorld;
    }

    @Override
    public void onUpdate(World world, float deltaTime) {
        if (assets == null || particleWorld == null) {
            return;
        }
        particleWorld.syncScene(world, assets);
        ComponentStore<ParticleEmitterComponent> emitters = world.store(ParticleEmitterComponent.class);
        for (int i = 0; i < emitters.size(); i++) {
            EntityId entity = emitters.entityAt(i);
            ParticleEmitterComponent component = emitters.componentAt(i);
            EmitterState state = particleWorld.emitter(entity);
            if (state == null || state.document == null) {
                continue;
            }
            if (component.playOnAwake && !state.playOnAwakeHandled) {
                state.playing = true;
                state.playOnAwakeHandled = true;
            }
            state.playing = component.emitting;
            particleWorld.stepEmitter(state, component, deltaTime, assets, physicsWorld);
        }
        for (EmitterState detached : particleWorld.detachedEmitters()) {
            particleWorld.stepEmitter(detached, null, deltaTime, assets, physicsWorld);
        }
    }

    public void play(EntityId entity) {
        EmitterState state = particleWorld.emitter(entity);
        if (state != null) {
            state.playing = true;
            state.emitterTime = 0f;
            state.spawnAccumulator = 0f;
            state.pool.clear();
        }
    }

    public void stop(EntityId entity) {
        EmitterState state = particleWorld.emitter(entity);
        if (state != null) {
            state.playing = false;
        }
    }

    public void burst(EntityId entity, int count) {
        EmitterState state = particleWorld.emitter(entity);
        if (state == null || count <= 0) {
            return;
        }
        particleWorld.simulator().spawnBurst(state, count);
    }
}
