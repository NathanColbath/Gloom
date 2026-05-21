package org.llw.studio.ecs.components;

/**
 * Spawns and simulates particles from a {@code .particle.json} asset at this entity's transform.
 */
public final class ParticleEmitterComponent implements Cloneable {
    /** GUID of the particle system asset. */
    public String particleSystemGuid = "";
    /** When true, simulation starts when the entity becomes active in play mode. */
    public boolean playOnAwake = true;
    /** Overrides asset looping when non-null behavior is needed at runtime. */
    public boolean looping = true;
    /** Draw order offset applied with scene base layer. */
    public int sortingOrder;
    /** Runtime flag: when false, no new particles spawn (existing may finish). */
    public boolean emitting = true;

    public ParticleEmitterComponent copy() {
        ParticleEmitterComponent copy = new ParticleEmitterComponent();
        copy.particleSystemGuid = particleSystemGuid;
        copy.playOnAwake = playOnAwake;
        copy.looping = looping;
        copy.sortingOrder = sortingOrder;
        copy.emitting = emitting;
        return copy;
    }

    @Override
    public ParticleEmitterComponent clone() {
        return copy();
    }
}
