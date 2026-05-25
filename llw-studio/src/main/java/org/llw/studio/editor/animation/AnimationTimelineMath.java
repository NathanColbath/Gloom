package org.llw.studio.editor.animation;

/**
 * Shared timeline coordinate helpers.
 */
public final class AnimationTimelineMath {
    private AnimationTimelineMath() {
    }

    public static float timeToX(float time, float pixelsPerSecond, float scrollX) {
        return time * pixelsPerSecond - scrollX; // Scroll subtracts so panning moves content left.
    }

    public static float xToTime(float x, float pixelsPerSecond, float scrollX, float maxTime) {
        if (pixelsPerSecond <= 0f) {
            return 0f;
        }
        float t = (x + scrollX) / pixelsPerSecond; // Inverse of timeToX; clamp to clip duration for scrubbing.
        return Math.max(0f, Math.min(maxTime, t));
    }
}
