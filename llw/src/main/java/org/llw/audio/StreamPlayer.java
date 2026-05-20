package org.llw.audio;

import org.llw.audio.resources.AudioStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import org.lwjgl.openal.AL10;

/**
 * Streams PCM chunks into a queue of OpenAL buffers attached to one source.
 */
final class StreamPlayer {
    private static final int QUEUE_DEPTH = 3;
    private static final int FRAMES_PER_CHUNK = 4096;

    private final OpenAlBackend backend;
    private AudioStream stream;
    private final int source;
    private final int channels;
    private final int sampleRate;
    private final Runnable loopRestart;
    private final Queue<Integer> freeBuffers = new ArrayDeque<>();
    private boolean playing;
    private boolean looping;
    private boolean eof;

    StreamPlayer(OpenAlBackend backend, AudioStream stream, int source, Runnable loopRestart) {
        this.backend = backend;
        this.stream = stream;
        this.source = source;
        this.channels = stream.channels();
        this.sampleRate = stream.sampleRate();
        this.loopRestart = loopRestart;
        for (int i = 0; i < QUEUE_DEPTH; i++) {
            freeBuffers.add(AL10.alGenBuffers());
        }
    }

    boolean isPlaying() {
        return playing;
    }

    void setLooping(boolean looping) {
        this.looping = looping;
    }

    void start() {
        playing = true;
        eof = false;
        unqueueProcessed();
        refillQueue();
        AL10.alSourcePlay(source);
    }

    void stop() {
        playing = false;
        AL10.alSourceStop(source);
        unqueueProcessed();
    }

    void pause() {
        playing = false;
        AL10.alSourcePause(source);
    }

    void resume() {
        playing = true;
        if (eof && looping) {
            loopRestart.run();
            return;
        }
        refillQueue();
        AL10.alSourcePlay(source);
    }

    void update() {
        if (!playing) {
            return;
        }
        unqueueProcessed();
        refillQueue();

        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED && eof) {
            playing = false;
            if (looping) {
                loopRestart.run();
            }
        }
    }

    void dispose() {
        stop();
        unqueueProcessed();
        while (!freeBuffers.isEmpty()) {
            backend.deleteBuffer(freeBuffers.poll());
        }
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    private void unqueueProcessed() {
        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buffer = AL10.alSourceUnqueueBuffers(source);
            freeBuffers.add(buffer);
        }
    }

    private void refillQueue() {
        if (stream == null) {
            return;
        }
        int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
        while (queued < QUEUE_DEPTH && !freeBuffers.isEmpty()) {
            int bufferName = freeBuffers.poll();
            ByteBuffer pcm = stream.readFrames(FRAMES_PER_CHUNK);
            if (pcm == null) {
                eof = true;
                freeBuffers.add(bufferName);
                break;
            }
            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            AL10.alBufferData(bufferName, format, pcm, sampleRate);
            AL10.alSourceQueueBuffers(source, bufferName);
            queued++;
        }
    }
}
