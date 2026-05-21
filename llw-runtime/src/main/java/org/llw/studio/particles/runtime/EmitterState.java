package org.llw.studio.particles.runtime;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.particles.model.ParticleSystemDocument;

/**
 * Per-entity particle simulation state.
 */
public final class EmitterState {
    public final EntityId entity;
    public final ParticlePool pool;
    public ParticleSystemDocument document;
    public float emitterTime;
    public float spawnAccumulator;
    public boolean playing = true;
    public boolean playOnAwakeHandled;
    public float worldX;
    public float worldY;
    public float worldRotation;
    public float worldScaleX = 1f;
    public float worldScaleY = 1f;
    public boolean localSpace;
    public int subEmitterDepth;

    public EmitterState(EntityId entity, int maxParticles) {
        this.entity = entity;
        this.pool = new ParticlePool(maxParticles);
    }
}
