package org.llw.studio.particles.runtime;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.particles.ParticleSystemSerializer;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.scene.ActiveUtility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns all active particle emitter simulation state for a scene.
 */
public final class ParticleWorld {
    private final Map<EntityId, EmitterState> byEntity = new HashMap<>();
    private final Map<String, EmitterState> detachedByGuid = new HashMap<>();
    private final ParticleSimulator simulator = new ParticleSimulator();

    public ParticleSimulator simulator() {
        return simulator;
    }

    public void clear() {
        byEntity.clear();
        detachedByGuid.clear();
    }

    public EmitterState emitter(EntityId entity) {
        return byEntity.get(entity);
    }

    public Iterable<EmitterState> detachedEmitters() {
        return detachedByGuid.values();
    }

    public List<EmitterState> allEmitters() {
        List<EmitterState> all = new ArrayList<>(byEntity.size() + detachedByGuid.size());
        all.addAll(byEntity.values());
        all.addAll(detachedByGuid.values());
        return all;
    }

    public void syncScene(World world, AssetDatabase assets) {
        if (world == null || assets == null) {
            return;
        }
        ComponentStore<ParticleEmitterComponent> emitters = world.store(ParticleEmitterComponent.class);
        Map<EntityId, Boolean> seen = new HashMap<>();
        for (int i = 0; i < emitters.size(); i++) {
            EntityId entity = emitters.entityAt(i);
            seen.put(entity, Boolean.TRUE);
            ParticleEmitterComponent component = emitters.componentAt(i);
            int capacity = 64;
            ParticleSystemDocument document = loadDocument(assets, component.particleSystemGuid);
            if (document != null) {
                capacity = Math.max(1, document.maxParticles);
            }
            final int poolCapacity = capacity;
            EmitterState state = byEntity.computeIfAbsent(entity, id -> new EmitterState(id, poolCapacity));
            state.document = document;
            updateTransform(world, entity, state, component);
        }
        byEntity.entrySet().removeIf(entry -> !seen.containsKey(entry.getKey()));
    }

    public EmitterState ensurePreview(String systemGuid, Path assetPath, AssetDatabase assets) {
        ParticleSystemDocument document = assets == null ? null : assets.loadParticleSystem(assetPath);
        int capacity = document == null ? 256 : Math.max(1, document.maxParticles);
        EmitterState state = detachedByGuid.computeIfAbsent(systemGuid, guid -> {
            EmitterState created = new EmitterState(EntityId.none(), capacity);
            created.localSpace = false;
            created.playing = true;
            return created;
        });
        state.document = document;
        state.playing = true;
        return state;
    }

    public void removePreview(String systemGuid) {
        detachedByGuid.remove(systemGuid);
    }

    public EmitterState previewEmitter(String systemGuid) {
        return detachedByGuid.get(systemGuid);
    }

    public void stepEmitter(
            EmitterState state,
            ParticleEmitterComponent component,
            float deltaTime,
            AssetDatabase assets,
            PhysicsWorld physicsWorld
    ) {
        if (state == null || state.document == null || deltaTime <= 0f) {
            return;
        }
        if (component != null) {
            if (component.playOnAwake && !state.playOnAwakeHandled) {
                state.playing = true;
                state.playOnAwakeHandled = true;
            }
            state.playing = component.emitting;
        }
        boolean emitting = state.playing;
        simulator.step(state, deltaTime, emitting, physicsWorld);
        simulator.flushSubEmitters(state, this, assets, ParticleSimulator.MAX_SUB_SPAWNS_PER_FRAME);
    }

    public EmitterState resolveSubEmitter(String systemGuid, AssetDatabase assets) {
        if (systemGuid == null || systemGuid.isBlank()) {
            return null;
        }
        EmitterState existing = detachedByGuid.get(systemGuid);
        if (existing != null) {
            return existing;
        }
        ParticleSystemDocument document = assets == null ? null : loadDocument(assets, systemGuid);
        int capacity = document == null ? 64 : Math.max(1, document.maxParticles);
        EmitterState state = new EmitterState(EntityId.none(), capacity);
        state.document = document;
        detachedByGuid.put(systemGuid, state);
        return state;
    }

    private static ParticleSystemDocument loadDocument(AssetDatabase assets, String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        StudioAsset asset = assets.get(guid);
        if (asset == null) {
            return null;
        }
        return assets.loadParticleSystem(asset.path());
    }

    private static void updateTransform(
            World world,
            EntityId entity,
            EmitterState state,
            ParticleEmitterComponent component
    ) {
        if (!ActiveUtility.isEffectivelyActive(world, entity)) {
            state.playing = false;
            return;
        }
        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        if (local == null) {
            return;
        }
        state.worldX = worldTransform != null ? worldTransform.worldX : local.x;
        state.worldY = worldTransform != null ? worldTransform.worldY : local.y;
        state.worldRotation = worldTransform != null ? worldTransform.worldRotation : local.rotation;
        state.worldScaleX = worldTransform != null ? worldTransform.worldScaleX : local.scaleX;
        state.worldScaleY = worldTransform != null ? worldTransform.worldScaleY : local.scaleY;
        state.localSpace = state.document != null
                && "local".equalsIgnoreCase(state.document.simulationSpace);
    }
}
