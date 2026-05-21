package org.llw.studio.particles.runtime;

/**
 * Fixed-capacity pool of {@link Particle} instances for one emitter.
 */
public final class ParticlePool {
    private final Particle[] particles;
    private int aliveCount;

    public ParticlePool(int capacity) {
        int cap = Math.max(1, capacity);
        particles = new Particle[cap];
        for (int i = 0; i < cap; i++) {
            particles[i] = new Particle();
        }
    }

    public int capacity() {
        return particles.length;
    }

    public int aliveCount() {
        return aliveCount;
    }

    public Particle particleAt(int index) {
        return particles[index];
    }

    public Particle acquire() {
        for (Particle particle : particles) {
            if (!particle.alive) {
                particle.alive = true;
                aliveCount++;
                return particle;
            }
        }
        return null;
    }

    public void compact() {
        aliveCount = 0;
        for (Particle particle : particles) {
            if (particle.alive) {
                aliveCount++;
            }
        }
    }

    public void clear() {
        for (Particle particle : particles) {
            particle.reset();
        }
        aliveCount = 0;
    }
}
