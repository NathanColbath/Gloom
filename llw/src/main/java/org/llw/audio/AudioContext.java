package org.llw.audio;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.llw.audio.resources.AudioLoader;

import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Entry point for the Gloom audio backend.
 * <p>
 * Owns the OpenAL device/context and exposes helpers for loading {@link SoundBuffer},
 * {@link Sound}, and {@link Music}. Call {@link #update()} once per frame while
 * streaming music is active.
 */
public final class AudioContext {
    private static final Logger log = Log.get(Loggers.AUDIO);

    private final OpenAlBackend backend = new OpenAlBackend();
    private final AlSourcePool sourcePool;
    private final List<Music> activeMusic = new ArrayList<>();
    private boolean disposed;

    /**
     * Creates an audio context and initializes the default OpenAL device.
     */
    public AudioContext() {
        backend.initialize();
        sourcePool = new AlSourcePool();
        AudioListener.setGlobalVolume(100f);
        log.info("AudioContext created");
    }

    /**
     * Loads a {@link SoundBuffer} from the classpath.
     *
     * @param classpathPath resource path relative to the classpath root
     * @return decoded and uploaded buffer
     */
    public SoundBuffer loadSoundBuffer(String classpathPath) {
        ensureActive();
        return SoundBuffer.fromClasspath(backend, classpathPath);
    }

    /**
     * Loads a {@link SoundBuffer} from the filesystem.
     *
     * @param path audio file path
     * @return decoded and uploaded buffer
     */
    public SoundBuffer loadSoundBuffer(Path path) {
        ensureActive();
        return SoundBuffer.fromFile(backend, path);
    }

    /**
     * Loads a {@link SoundBuffer} from encoded bytes in memory.
     *
     * @param fileName format hint (extension)
     * @param bytes    encoded audio bytes
     * @return decoded and uploaded buffer
     */
    public SoundBuffer loadSoundBufferFromMemory(String fileName, byte[] bytes) {
        ensureActive();
        return SoundBuffer.fromMemory(backend, fileName, bytes);
    }

    /**
     * Creates a {@link Sound} instance bound to this context's source pool.
     *
     * @return new sound source
     */
    public Sound createSound() {
        ensureActive();
        return new Sound(this);
    }

    /**
     * Opens streaming music from a classpath resource.
     *
     * @param classpathPath resource path relative to the classpath root
     * @return streaming music source tracked by {@link #update()}
     */
    public Music openMusic(String classpathPath) {
        ensureActive();
        Music music = new Music(backend, sourcePool, () -> AudioLoader.openStreamFromClasspath(classpathPath));
        activeMusic.add(music);
        return music;
    }

    /**
     * Opens streaming music from the filesystem.
     *
     * @param path audio file path
     * @return streaming music source tracked by {@link #update()}
     */
    public Music openMusic(Path path) {
        ensureActive();
        return openMusicSupplier(() -> AudioLoader.openStreamFromFile(path));
    }

    /**
     * Opens streaming music from a custom stream supplier.
     *
     * @param streamSupplier factory that opens a new {@link org.llw.audio.resources.AudioStream}
     * @return streaming music tracked by {@link #update()}
     */
    public Music openMusicSupplier(java.util.function.Supplier<org.llw.audio.resources.AudioStream> streamSupplier) {
        ensureActive();
        Music music = new Music(backend, sourcePool, streamSupplier);
        activeMusic.add(music);
        return music;
    }

    /**
     * Updates all active {@link Music} streams. Call once per frame from the main loop.
     */
    public void update() {
        if (disposed) {
            return;
        }
        activeMusic.removeIf(music -> {
            music.update();
            return false;
        });
    }

    /**
     * Stops all sounds, releases OpenAL resources, and closes the device.
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        for (Music music : activeMusic) {
            music.dispose();
        }
        activeMusic.clear();
        sourcePool.dispose();
        backend.dispose();
        disposed = true;
        log.info("AudioContext disposed");
    }

    AlSourcePool sourcePool() {
        return sourcePool;
    }

    private void ensureActive() {
        if (disposed) {
            throw new IllegalStateException("AudioContext has been disposed");
        }
    }
}
