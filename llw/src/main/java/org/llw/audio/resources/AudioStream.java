package org.llw.audio.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

/**
 * Incrementally decodes WAV or OGG audio for streaming playback.
 */
public final class AudioStream implements AutoCloseable {
    private final String fileName;
    private final int channels;
    private final int sampleRate;
    private final AudioInputStream wavStream;
    private final long vorbisHandle;
    private boolean closed;

    private AudioStream(String fileName, int channels, int sampleRate, AudioInputStream wavStream, long vorbisHandle) {
        this.fileName = fileName;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.wavStream = wavStream;
        this.vorbisHandle = vorbisHandle;
    }

    static AudioStream from(String fileName, InputStream stream) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".ogg")) {
            return openOgg(fileName, stream);
        }
        return openWav(fileName, stream);
    }

    /**
     * Returns the channel count of the decoded stream.
     *
     * @return number of channels
     */
    public int channels() {
        return channels;
    }

    /**
     * Returns the sample rate in Hz.
     *
     * @return sample rate
     */
    public int sampleRate() {
        return sampleRate;
    }

    /**
     * Reads up to {@code frameCount} PCM frames into a direct 16-bit buffer.
     *
     * @param frameCount maximum number of frames to read
     * @return PCM bytes read, or {@code null} when the stream has ended
     */
    public ByteBuffer readFrames(int frameCount) {
        if (closed) {
            return null;
        }
        if (vorbisHandle != 0L) {
            return readOggFrames(frameCount);
        }
        return readWavFrames(frameCount);
    }

    /**
     * Releases native and JDK stream resources.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (vorbisHandle != 0L) {
            STBVorbis.stb_vorbis_close(vorbisHandle);
        }
        if (wavStream != null) {
            try {
                wavStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static AudioStream openWav(String fileName, InputStream stream) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream);
            AudioFormat format = audioStream.getFormat();
            AudioFormat target = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
            );
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(target, audioStream);
            return new AudioStream(fileName, target.getChannels(), (int) target.getSampleRate(), pcmStream, 0L);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported or corrupt WAV stream: " + fileName, ex);
        }
    }

    private static AudioStream openOgg(String fileName, InputStream stream) {
        try {
            byte[] bytes = stream.readAllBytes();
            stream.close();
            ByteBuffer encoded = ByteBuffer.allocateDirect(bytes.length);
            encoded.put(bytes).flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                var error = stack.mallocInt(1);
                long handle = STBVorbis.stb_vorbis_open_memory(encoded, error, null);
                if (handle == 0L) {
                    throw new IllegalArgumentException("Failed to open OGG stream: " + fileName);
                }
                STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                STBVorbis.stb_vorbis_get_info(handle, info);
                return new AudioStream(fileName, info.channels(), info.sample_rate(), null, handle);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open OGG stream: " + fileName, ex);
        }
    }

    private ByteBuffer readWavFrames(int frameCount) {
        try {
            int bytesToRead = frameCount * channels * 2;
            byte[] bytes = wavStream.readNBytes(bytesToRead);
            if (bytes.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes).flip();
            return buffer;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read WAV stream: " + fileName, ex);
        }
    }

    private ByteBuffer readOggFrames(int frameCount) {
        int maxSamples = frameCount * channels;
        ShortBuffer sampleBuffer = BufferUtils.createShortBuffer(maxSamples);
        int decoded = STBVorbis.stb_vorbis_get_samples_short_interleaved(
                vorbisHandle, channels, sampleBuffer);
        if (decoded <= 0) {
            return null;
        }
        sampleBuffer.position(0);
        sampleBuffer.limit(decoded);
        ByteBuffer pcm = BufferUtils.createByteBuffer(decoded * 2);
        while (sampleBuffer.hasRemaining()) {
            pcm.putShort(sampleBuffer.get());
        }
        pcm.flip();
        return pcm;
    }
}
