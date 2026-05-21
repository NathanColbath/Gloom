package org.llw.studio.particles.runtime;

/**
 * Queued sub-emitter spawn from a parent particle event.
 */
public record SubEmitterRequest(
        String systemGuid,
        float x,
        float y,
        float vx,
        float vy,
        int depth
) {
}
