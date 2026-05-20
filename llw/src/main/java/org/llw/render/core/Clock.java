package org.llw.render.core;

/**
 * High-resolution timer based on {@link System#nanoTime()} for frame timing.
 *
 * <p>All returned durations are in seconds. The clock records a start time on
 * construction and updates its last-tick timestamp on each {@link #tick()} or
 * {@link #restart()}.
 */
public final class Clock {
    private long startNanos = System.nanoTime();
    private long lastNanos = startNanos;

    /**
     * Returns the elapsed time since the previous {@link #tick()} or {@link #restart()},
     * then resets both the start and last-tick timestamps to now.
     *
     * @return delta time in seconds since the last tick or restart
     */
    public float restart() {
        long now = System.nanoTime();
        float delta = (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        startNanos = now;
        return delta;
    }

    /**
     * Returns the elapsed time since the previous {@link #tick()} or {@link #restart()},
     * then updates the last-tick timestamp to now.
     *
     * @return delta time in seconds since the last tick or restart
     */
    public float tick() {
        long now = System.nanoTime();
        float delta = (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        return delta;
    }

    /**
     * Returns the total time since the last {@link #restart()} (or since construction
     * if {@link #restart()} has not been called).
     *
     * @return elapsed time in seconds since the last restart
     */
    public float elapsedSeconds() {
        return (System.nanoTime() - startNanos) / 1_000_000_000f;
    }
}
