package org.llw.audio;

import org.llw.audio.core.PlaybackStatus;
import org.lwjgl.openal.AL10;

/**
 * Short sound played from a {@link SoundBuffer}.
 * Corresponds to {@code sf::Sound} in SFML.
 */
public final class Sound extends AbstractPlaybackSource {
    private SoundBuffer buffer;

    /**
     * Creates a sound source using the shared source pool from an {@link AudioContext}.
     *
     * @param context audio context that owns the OpenAL source pool
     */
    public Sound(AudioContext context) {
        super(context.sourcePool());
    }

    /**
     * Sets the buffer played by this sound.
     *
     * @param buffer sound buffer; must remain valid for the lifetime of playback
     */
    public void setBuffer(SoundBuffer buffer) {
        this.buffer = buffer;
        if (source != 0 && buffer != null) {
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer.bufferId());
        }
    }

    /**
     * Returns the buffer attached to this sound.
     *
     * @return current buffer, or {@code null} if none
     */
    public SoundBuffer getBuffer() {
        return buffer;
    }

    /**
     * Starts or resumes playback from the current offset.
     */
    public void play() {
        if (buffer == null) {
            return;
        }
        if (source == 0) {
            source = sourcePool.acquire();
            if (source == 0) {
                return;
            }
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer.bufferId());
            applySourceProperties();
        }
        AL10.alSourcePlay(source);
    }

    /**
     * Pauses playback at the current offset.
     */
    public void pause() {
        if (source != 0) {
            AL10.alSourcePause(source);
        }
    }

    /**
     * Stops playback and resets the offset to the beginning.
     */
    public void stop() {
        releaseSource();
    }

    /**
     * Returns whether this sound is currently playing.
     *
     * @return {@code true} when status is {@link PlaybackStatus#PLAYING}
     */
    public boolean isPlaying() {
        return status() == PlaybackStatus.PLAYING;
    }
}
