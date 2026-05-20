package org.llw.studio.ecs.components;

/**
 * Audio playback settings referencing a project clip by GUID.
 * <p>
 * Authored in the editor and triggered during play mode; does not affect 2D layout.
 */
public final class AudioSourceComponent implements Cloneable {
    /** Asset GUID of the audio clip to play. */
    public String clipGuid = "";
    /** Playback volume multiplier, typically {@code [0, 1]}. */
    public float volume = 1f;
    /** When {@code true}, playback starts automatically when play mode begins. */
    public boolean playOnStart;

    /**
     * @return deep copy of this audio source settings
     */
    public AudioSourceComponent copy() {
        AudioSourceComponent copy = new AudioSourceComponent();
        copy.clipGuid = clipGuid;
        copy.volume = volume;
        copy.playOnStart = playOnStart;
        return copy;
    }

    @Override
    public AudioSourceComponent clone() {
        return copy();
    }
}
