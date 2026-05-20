package org.llw.audio;

import org.llw.audio.core.PlaybackStatus;
import org.llw.audio.core.Time;
import org.llw.audio.core.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * Shared playback controls for {@link Sound} and {@link Music}.
 * Corresponds to {@code sf::SoundSource} in SFML.
 */
abstract class AbstractPlaybackSource {
    private static final float MIN_PITCH = 0.1f;
    private static final float MAX_PITCH = 10f;

    final AlSourcePool sourcePool;
    int source;
    float volume = 100f;
    float pitch = 1f;
    float pan;
    boolean looping;
    boolean relative = true;
    final Vector3f position = new Vector3f();
    float minDistance = 1f;
    float maxDistance = 100f;

    AbstractPlaybackSource(AlSourcePool sourcePool) {
        this.sourcePool = sourcePool;
    }

    /**
     * Returns the current playback status.
     *
     * @return status derived from the OpenAL source state
     */
    public PlaybackStatus status() {
        if (source == 0) {
            return PlaybackStatus.STOPPED;
        }
        return switch (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE)) {
            case AL10.AL_PLAYING -> PlaybackStatus.PLAYING;
            case AL10.AL_PAUSED -> PlaybackStatus.PAUSED;
            default -> PlaybackStatus.STOPPED;
        };
    }

    /**
     * Sets the playback volume on a 0–100 scale.
     *
     * @param volume linear volume (clamped to 0–100)
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(100f, volume));
        applySourceProperties();
    }

    /**
     * Returns the playback volume on a 0–100 scale.
     *
     * @return volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Sets the pitch multiplier (1 = normal).
     *
     * @param pitch pitch factor, clamped to 0.1–10
     */
    public void setPitch(float pitch) {
        this.pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, pitch));
        applySourceProperties();
    }

    /**
     * Returns the pitch multiplier.
     *
     * @return pitch factor
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the stereo pan from -1 (left) to 1 (right).
     *
     * @param pan pan value, clamped to -1–1
     */
    public void setPan(float pan) {
        this.pan = Math.max(-1f, Math.min(1f, pan));
        applySourceProperties();
    }

    /**
     * Returns the stereo pan.
     *
     * @return pan in the range -1–1
     */
    public float getPan() {
        return pan;
    }

    /**
     * Enables or disables looping.
     *
     * @param looping {@code true} to loop
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
        applySourceProperties();
    }

    /**
     * Returns whether looping is enabled.
     *
     * @return {@code true} when looping
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     * Sets whether the position is relative to the listener.
     *
     * @param relative {@code true} for listener-relative coordinates
     */
    public void setRelativeToListener(boolean relative) {
        this.relative = relative;
        applySourceProperties();
    }

    /**
     * Returns whether the source position is listener-relative.
     *
     * @return relative flag
     */
    public boolean isRelativeToListener() {
        return relative;
    }

    /**
     * Sets the 3D position used for spatialization.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        applySourceProperties();
    }

    /**
     * Returns the 3D position.
     *
     * @return mutable position vector
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Sets the minimum distance for attenuation.
     *
     * @param minDistance distance in world units
     */
    public void setMinDistance(float minDistance) {
        this.minDistance = Math.max(0f, minDistance);
        applySourceProperties();
    }

    /**
     * Returns the minimum attenuation distance.
     *
     * @return distance in world units
     */
    public float getMinDistance() {
        return minDistance;
    }

    /**
     * Sets the maximum distance for attenuation.
     *
     * @param maxDistance distance in world units
     */
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = Math.max(minDistance, maxDistance);
        applySourceProperties();
    }

    /**
     * Returns the maximum attenuation distance.
     *
     * @return distance in world units
     */
    public float getMaxDistance() {
        return maxDistance;
    }

    /**
     * Returns the current playback offset.
     *
     * @return offset in seconds
     */
    public Time getPlayingOffset() {
        if (source == 0) {
            return new Time(0f);
        }
        float seconds = AL11.alGetSourcef(source, AL11.AL_SEC_OFFSET);
        return new Time(seconds);
    }

    /**
     * Seeks to the given playback offset.
     *
     * @param offset offset from the beginning in seconds
     */
    public void setPlayingOffset(Time offset) {
        if (source != 0) {
            AL11.alSourcef(source, AL11.AL_SEC_OFFSET, Math.max(0f, offset.asSeconds()));
        }
    }

    void applySourceProperties() {
        if (source == 0) {
            return;
        }
        AL10.alSourcef(source, AL10.AL_GAIN, volume / 100f);
        AL10.alSourcef(source, AL10.AL_PITCH, pitch);
        AL10.alSourcei(source, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSource3f(source, AL10.AL_POSITION, pan, position.y, position.z);
        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, minDistance);
        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, maxDistance);
    }

    void releaseSource() {
        if (source != 0) {
            sourcePool.release(source);
            source = 0;
        }
    }
}
