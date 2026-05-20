package org.llw.audio.core;

/**
 * Immutable duration or playback offset expressed in seconds.
 * Corresponds to {@code sf::Time} in SFML.
 */
public final class Time {
    private final float seconds;

    /**
     * Creates a time value from seconds.
     *
     * @param seconds duration or offset in seconds
     */
    public Time(float seconds) {
        this.seconds = seconds;
    }

    /**
     * Creates a time value from milliseconds.
     *
     * @param milliseconds duration or offset in milliseconds
     * @return the equivalent {@link Time}
     */
    public static Time milliseconds(long milliseconds) {
        return new Time(milliseconds / 1000f);
    }

    /**
     * Returns the value in seconds.
     *
     * @return seconds
     */
    public float asSeconds() {
        return seconds;
    }

    /**
     * Returns the value in milliseconds.
     *
     * @return milliseconds rounded to the nearest whole millisecond
     */
    public long asMilliseconds() {
        return Math.round(seconds * 1000f);
    }
}
