package org.llw.studio.scripting.js;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.particles.systems.ParticleSimulationSystem;

/**
 * Play-mode access to the active {@link ParticleSimulationSystem}.
 */
public final class PlayParticleBridge {
    private static ParticleSimulationSystem particleSystem;

    private PlayParticleBridge() {
    }

    public static void setActive(ParticleSimulationSystem system) {
        particleSystem = system;
    }

    public static void clear() {
        particleSystem = null;
    }

    public static void play(EntityId entity) {
        if (particleSystem != null) {
            particleSystem.play(entity);
        }
    }

    public static void stop(EntityId entity) {
        if (particleSystem != null) {
            particleSystem.stop(entity);
        }
    }

    public static void burst(EntityId entity, int count) {
        if (particleSystem != null) {
            particleSystem.burst(entity, count);
        }
    }
}
