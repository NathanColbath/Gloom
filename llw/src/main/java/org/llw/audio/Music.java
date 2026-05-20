package org.llw.audio;

import org.llw.audio.core.PlaybackStatus;
import org.llw.audio.resources.AudioStream;

import java.util.function.Supplier;

/**
 * Streaming music played from a file or classpath resource.
 * Corresponds to {@code sf::Music} in SFML.
 * <p>
 * Unlike {@link Sound}, the full PCM data is not kept in memory. Call
 * {@link AudioContext#update()} each frame so queued buffers can be refilled.
 */
public final class Music extends AbstractPlaybackSource {
    private final OpenAlBackend backend;
    private final Supplier<AudioStream> streamFactory;
    private StreamPlayer streamPlayer;
    private AudioStream stream;

    Music(OpenAlBackend backend, AlSourcePool sourcePool, Supplier<AudioStream> streamFactory) {
        super(sourcePool);
        this.backend = backend;
        this.streamFactory = streamFactory;
    }

    /**
     * Starts or resumes streaming playback.
     */
    public void play() {
        if (streamPlayer == null || stream == null) {
            openStream();
        }
        if (streamPlayer != null) {
            streamPlayer.setLooping(looping);
            streamPlayer.resume();
            playing = true;
        }
    }

    /**
     * Pauses streaming at the current offset.
     */
    public void pause() {
        if (streamPlayer != null) {
            streamPlayer.pause();
        }
    }

    /**
     * Stops streaming and releases the OpenAL source.
     */
    public void stop() {
        if (streamPlayer != null) {
            streamPlayer.stop();
        }
        closeStream();
        releaseSource();
    }

    /**
     * Returns whether this music stream is playing.
     *
     * @return {@code true} when status is {@link PlaybackStatus#PLAYING}
     */
    public boolean isPlaying() {
        return streamPlayer != null && streamPlayer.isPlaying();
    }

    @Override
    public void setLooping(boolean looping) {
        super.setLooping(looping);
        if (streamPlayer != null) {
            streamPlayer.setLooping(looping);
        }
    }

    void update() {
        if (streamPlayer == null) {
            return;
        }
        streamPlayer.update();
        if (!streamPlayer.isPlaying() && looping && stream != null) {
            reopenForLoop();
        }
        applySourceProperties();
    }

    void dispose() {
        stop();
        if (streamPlayer != null) {
            streamPlayer.dispose();
            streamPlayer = null;
        }
    }

    private void openStream() {
        closeStream();
        stream = streamFactory.get();
        source = sourcePool.acquire();
        if (source == 0) {
            stream.close();
            stream = null;
            return;
        }
        streamPlayer = new StreamPlayer(backend, stream, source, this::reopenForLoop);
        streamPlayer.setLooping(looping);
        applySourceProperties();
        streamPlayer.start();
    }

    private void reopenForLoop() {
        if (streamPlayer != null) {
            streamPlayer.dispose();
            streamPlayer = null;
        }
        stream = null;
        openStream();
    }

    private void closeStream() {
        if (streamPlayer != null) {
            streamPlayer.dispose();
            streamPlayer = null;
        }
        stream = null;
    }

    private boolean playing;
}
