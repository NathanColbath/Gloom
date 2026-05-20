package org.llw.audio;

import org.llw.audio.core.Time;
import org.llw.audio.resources.AudioLoader;
import org.llw.audio.resources.PcmData;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

import java.nio.file.Path;

/**
 * In-memory audio samples uploaded to OpenAL.
 * Corresponds to {@code sf::SoundBuffer} in SFML.
 * <p>
 * A buffer must outlive every {@link Sound} that references it. Call {@link #dispose()}
 * only after all sounds using this buffer have been stopped.
 */
public final class SoundBuffer {
    private static final Logger log = Log.get(Loggers.AUDIO);

    private final OpenAlBackend backend;
    private final int bufferId;
    private final int sampleRate;
    private final int channelCount;
    private final float durationSeconds;
    private boolean disposed;

    private SoundBuffer(OpenAlBackend backend, PcmData pcm) {
        this.backend = backend;
        this.sampleRate = pcm.sampleRate();
        this.channelCount = pcm.channels();
        this.durationSeconds = pcm.durationSeconds();
        this.bufferId = backend.createBuffer(pcm.preparedSamples(), channelCount, sampleRate);
        log.debug("Loaded sound buffer id={} channels={} sampleRate={} durationSec={}",
                bufferId, channelCount, sampleRate, durationSeconds);
    }

    /**
     * Loads a sound buffer from a classpath resource.
     *
     * @param backend       initialized OpenAL backend owned by {@link AudioContext}
     * @param classpathPath resource path relative to the classpath root
     * @return loaded buffer
     */
    public static SoundBuffer fromClasspath(OpenAlBackend backend, String classpathPath) {
        return new SoundBuffer(backend, AudioLoader.loadFromClasspath(classpathPath));
    }

    /**
     * Loads a sound buffer from a filesystem path.
     *
     * @param backend initialized OpenAL backend owned by {@link AudioContext}
     * @param path    audio file path
     * @return loaded buffer
     */
    public static SoundBuffer fromFile(OpenAlBackend backend, Path path) {
        return new SoundBuffer(backend, AudioLoader.loadFromFile(path));
    }

    /**
     * Loads a sound buffer from encoded bytes in memory.
     *
     * @param backend  initialized OpenAL backend owned by {@link AudioContext}
     * @param fileName format hint (file extension)
     * @param bytes    encoded audio bytes
     * @return loaded buffer
     */
    public static SoundBuffer fromMemory(OpenAlBackend backend, String fileName, byte[] bytes) {
        return new SoundBuffer(backend, AudioLoader.loadFromMemory(fileName, bytes));
    }

    /**
     * Returns the duration of the buffer.
     *
     * @return duration in seconds
     */
    public Time getDuration() {
        return new Time(durationSeconds);
    }

    /**
     * Returns the sample rate in Hz.
     *
     * @return sample rate
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Returns the number of channels.
     *
     * @return channel count
     */
    public int getChannelCount() {
        return channelCount;
    }

    int bufferId() {
        return bufferId;
    }

    /**
     * Deletes the OpenAL buffer. The instance must not be used afterward.
     */
    public void dispose() {
        if (!disposed) {
            backend.deleteBuffer(bufferId);
            disposed = true;
        }
    }
}
