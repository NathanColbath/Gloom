package org.llw.studio.scripting.js;

import org.llw.audio.AudioContext;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Play-mode playback for {@link AudioSourceComponent} instances via OpenAL.
 */
public final class PlayAudioBridge {
    private static final Logger log = Log.get(Loggers.AUDIO);

    private static AudioContext audioContext;
    private static AssetDatabase assets;
    private static final java.util.Map<EntityId, ActiveSound> activeSounds = new java.util.HashMap<>();

    private PlayAudioBridge() {
    }

    /**
     * @param context studio audio context
     * @param assetDb project assets for clip lookup
     */
    public static void configure(AudioContext context, AssetDatabase assetDb) {
        audioContext = context;
        assets = assetDb;
    }

    /**
     * @param entity audio source entity
     * @return {@code true} while the clip is actively playing
     */
    public static boolean isPlaying(EntityId entity) {
        ActiveSound active = activeSounds.get(entity);
        if (active == null) {
            return false;
        }
        if (active.sound.isPlaying()) {
            return true;
        }
        releaseActive(active);
        activeSounds.remove(entity);
        return false;
    }

    /**
     * @param entity audio source entity
     * @param source component with a non-blank clip GUID
     */
    public static void play(EntityId entity, AudioSourceComponent source) {
        if (audioContext == null || assets == null || source == null) {
            return;
        }
        if (source.clipGuid == null || source.clipGuid.isBlank()) {
            return;
        }
        stop(entity);
        SoundBuffer buffer = assets.soundBuffer(source.clipGuid);
        if (buffer == null) {
            log.warn("Audio clip not found or failed to load: {}", source.clipGuid);
            return;
        }
        try {
            Sound sound = audioContext.createSound();
            sound.setBuffer(buffer);
            sound.setVolume(clampVolume(source.volume) * 100f);
            sound.setLooping(false);
            sound.setRelativeToListener(true);
            sound.play();
            activeSounds.put(entity, new ActiveSound(sound, source.clipGuid));
        } catch (RuntimeException ex) {
            log.warn("Failed to play audio clip {}", ex, source.clipGuid);
        }
    }

    /**
     * @param entity audio source entity
     */
    public static void stop(EntityId entity) {
        ActiveSound active = activeSounds.remove(entity);
        releaseActive(active);
    }

    /** Stops all sounds and clears play-mode audio state. */
    public static void reset() {
        for (ActiveSound active : activeSounds.values()) {
            releaseActive(active);
        }
        activeSounds.clear();
        if (assets != null) {
            assets.clearSoundCache();
        }
        audioContext = null;
        assets = null;
    }

    /** Updates streaming music and drops finished one-shot sources. */
    public static void update() {
        if (audioContext != null) {
            audioContext.update();
        }
        activeSounds.entrySet().removeIf(entry -> {
            if (entry.getValue().sound.isPlaying()) {
                return false;
            }
            releaseActive(entry.getValue());
            return true;
        });
    }

    private static void releaseActive(ActiveSound active) {
        if (active != null) {
            active.sound.stop();
        }
    }

    private static float clampVolume(float volume) {
        if (volume < 0f) {
            return 0f;
        }
        if (volume > 1f) {
            return 1f;
        }
        return volume;
    }

    private record ActiveSound(Sound sound, String clipGuid) {
    }
}
