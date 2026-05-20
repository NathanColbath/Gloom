package org.llw.studio.ecs;

/**
 * Per-frame logic executed by a {@link SystemScheduler} against a {@link World}.
 * <p>
 * Implementations run in both editor and play mode; use {@link CommandBuffer} for deferred
 * structural changes when mutating entities during iteration.
 */
public interface EcsSystem {
    /**
     * Called once per frame for this system.
     *
     * @param world      ECS world to query and mutate
     * @param deltaTime  elapsed seconds since the previous update
     */
    void onUpdate(World world, float deltaTime);
}
