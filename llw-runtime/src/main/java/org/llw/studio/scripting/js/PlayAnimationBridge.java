package org.llw.studio.scripting.js;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.systems.AnimationSystem;

/**
 * Play-mode access to the active {@link AnimationSystem}.
 */
public final class PlayAnimationBridge {
    private static AnimationSystem animationSystem;

    private PlayAnimationBridge() {
    }

    public static void setActive(AnimationSystem system) {
        animationSystem = system;
    }

    public static void clear() {
        animationSystem = null;
    }

    public static void play(EntityId entity) {
        if (animationSystem != null) {
            animationSystem.setPlaying(entity, true);
        }
    }

    public static void playState(EntityId entity, String stateName) {
        if (animationSystem != null) {
            animationSystem.playState(entity, stateName);
        }
    }

    public static void stop(EntityId entity) {
        if (animationSystem != null) {
            animationSystem.stop(entity);
        }
    }

    public static float normalizedTime(EntityId entity) {
        return animationSystem == null ? 0f : animationSystem.normalizedTime(entity);
    }

    public static void setNormalizedTime(EntityId entity, float value) {
        if (animationSystem != null) {
            animationSystem.setNormalizedTime(entity, value);
        }
    }
}
