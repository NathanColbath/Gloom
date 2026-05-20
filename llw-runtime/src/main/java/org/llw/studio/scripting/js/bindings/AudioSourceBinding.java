package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.scripting.js.PlayAudioBridge;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: {@code AudioSource} component host binding.
 */
public final class AudioSourceBinding {
    private final World world;
    private final EntityId entity;

    /**
     * @param context play-mode script context
     * @param entity  owning entity
     */
    public AudioSourceBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    /**
     * @return audio clip asset GUID
     */
    @HostAccess.Export
    public String getClipGuid() {
        AudioSourceComponent audio = component();
        return audio == null ? "" : audio.clipGuid;
    }

    /**
     * @param guid audio clip asset GUID
     */
    @HostAccess.Export
    public void setClipGuid(String guid) {
        AudioSourceComponent audio = component();
        if (audio != null) {
            audio.clipGuid = guid == null ? "" : guid;
        }
    }

    /**
     * @return volume in {@code [0, 1]}
     */
    @HostAccess.Export
    public double getVolume() {
        AudioSourceComponent audio = component();
        return audio == null ? 1d : audio.volume;
    }

    /**
     * @param value volume in {@code [0, 1]}
     */
    @HostAccess.Export
    public void setVolume(double value) {
        AudioSourceComponent audio = component();
        if (audio != null) {
            audio.volume = (float) value;
        }
    }

    /**
     * @return {@code true} when the clip should play on start
     */
    @HostAccess.Export
    public boolean getPlayOnStart() {
        AudioSourceComponent audio = component();
        return audio != null && audio.playOnStart;
    }

    /**
     * @param value {@code true} to play on start
     */
    @HostAccess.Export
    public void setPlayOnStart(boolean value) {
        AudioSourceComponent audio = component();
        if (audio != null) {
            audio.playOnStart = value;
        }
    }

    /**
     * @return {@code true} when {@link #play()} was called and not stopped
     */
    @HostAccess.Export
    public boolean getPlaying() {
        return PlayAudioBridge.isPlaying(entity);
    }

    /** Starts playback when a clip GUID is assigned. */
    @HostAccess.Export
    public void play() {
        AudioSourceComponent audio = component();
        if (audio != null) {
            PlayAudioBridge.play(entity, audio);
        }
    }

    /** Stops playback for this entity. */
    @HostAccess.Export
    public void stop() {
        PlayAudioBridge.stop(entity);
    }

    private AudioSourceComponent component() {
        return world.getComponent(entity, AudioSourceComponent.class);
    }
}
