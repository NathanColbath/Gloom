package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.World;
import org.llw.studio.scripting.js.PlayInputBridge;

/**
 * Resets per-frame play-mode input state before script logic runs.
 *
 * @see PlayInputBridge#beginFrame()
 */
public final class PlayInputSystem implements EcsSystem {
    /**
     * @param world      scene ECS world
     * @param deltaTime  unused
     */
    @Override
    public void onUpdate(World world, float deltaTime) {
        PlayInputBridge.beginFrame();
    }
}
