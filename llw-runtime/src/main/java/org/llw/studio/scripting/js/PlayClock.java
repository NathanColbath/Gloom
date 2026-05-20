package org.llw.studio.scripting.js;

/**
 * Frame timing state shared with script {@code Time} bindings during play mode.
 */
public final class PlayClock {
    private static float deltaTime;
    private static float time;
    private static int frameCount;

    private PlayClock() {
    }

    /**
     * @param dt elapsed seconds for the current frame
     */
    public static void beginFrame(float dt) {
        deltaTime = dt;
        time += dt;
        frameCount++;
    }

    /**
     * @return elapsed seconds for the current frame
     */
    public static float deltaTime() {
        return deltaTime;
    }

    /**
     * @return accumulated play time in seconds
     */
    public static float time() {
        return time;
    }

    /**
     * @return number of frames since the last reset
     */
    public static int frameCount() {
        return frameCount;
    }

    /** Resets delta time, accumulated time, and frame count to zero. */
    public static void reset() {
        deltaTime = 0f;
        time = 0f;
        frameCount = 0;
    }
}
