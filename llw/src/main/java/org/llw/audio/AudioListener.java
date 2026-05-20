package org.llw.audio;

import org.llw.audio.core.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * Global OpenAL listener used for spatial audio and master gain.
 * Corresponds to {@code sf::Listener} in SFML.
 */
public final class AudioListener {
    private static float globalVolume = 100f;
    private static final Vector3f position = new Vector3f();
    private static final Vector3f direction = new Vector3f(0f, 0f, -1f);
    private static final Vector3f up = new Vector3f(0f, 1f, 0f);

    private AudioListener() {}

    /**
     * Sets the master listener volume on a 0–100 scale.
     *
     * @param volume linear volume (clamped to 0–100)
     */
    public static void setGlobalVolume(float volume) {
        globalVolume = Math.max(0f, Math.min(100f, volume));
        AL10.alListenerf(AL10.AL_GAIN, globalVolume / 100f);
    }

    /**
     * Returns the master listener volume on a 0–100 scale.
     *
     * @return global volume
     */
    public static float getGlobalVolume() {
        return globalVolume;
    }

    /**
     * Sets the listener position in world space.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public static void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    }

    /**
     * Returns the listener position.
     *
     * @return mutable position vector
     */
    public static Vector3f getPosition() {
        return position;
    }

    /**
     * Sets the forward direction of the listener.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     */
    public static void setDirection(float x, float y, float z) {
        direction.set(x, y, z);
        applyOrientation();
    }

    /**
     * Returns the listener forward direction.
     *
     * @return mutable direction vector
     */
    public static Vector3f getDirection() {
        return direction;
    }

    /**
     * Sets the listener up vector.
     *
     * @param x X component
     * @param y Y component
     * @param z Z component
     */
    public static void setUpVector(float x, float y, float z) {
        up.set(x, y, z);
        applyOrientation();
    }

    /**
     * Returns the listener up vector.
     *
     * @return mutable up vector
     */
    public static Vector3f getUpVector() {
        return up;
    }

    private static void applyOrientation() {
        float[] orientation = {
                direction.x, direction.y, direction.z,
                up.x, up.y, up.z
        };
        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
    }
}
