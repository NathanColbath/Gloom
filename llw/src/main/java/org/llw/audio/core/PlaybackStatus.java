package org.llw.audio.core;

/**
 * Playback state of a {@link org.llw.audio.Sound} or {@link org.llw.audio.Music} source.
 * Corresponds to {@code sf::SoundSource::Status} in SFML.
 */
public enum PlaybackStatus {
    /** The source is not playing and is at the beginning (or was stopped). */
    STOPPED,
    /** The source is paused at the current offset. */
    PAUSED,
    /** The source is actively playing. */
    PLAYING
}
