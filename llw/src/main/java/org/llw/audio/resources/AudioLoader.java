package org.llw.audio.resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Loads WAV and OGG audio into 16-bit PCM suitable for OpenAL upload.
 */
public final class AudioLoader {
    private static final Logger log = Log.get(Loggers.AUDIO_RESOURCES);

    private AudioLoader() {}

    /**
     * Loads audio from a classpath resource.
     *
     * @param classpathPath path relative to the classpath root
     * @return decoded PCM data
     * @throws IllegalArgumentException if the resource is missing or the format is unsupported
     */
    public static PcmData loadFromClasspath(String classpathPath) {
        try (InputStream stream = AudioLoader.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Audio resource not found: " + classpathPath);
            }
            return decode(classpathPath, stream);
        } catch (IOException ex) {
            throw LogHelper.logAndThrow(log, "Failed to load audio resource: " + classpathPath, ex);
        }
    }

    /**
     * Loads audio from a filesystem path.
     *
     * @param path file path
     * @return decoded PCM data
     * @throws IllegalArgumentException if the file is missing or the format is unsupported
     */
    public static PcmData loadFromFile(Path path) {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            return decode(path.getFileName().toString(), stream);
        } catch (IOException ex) {
            throw LogHelper.logAndThrow(log, "Failed to load audio file: " + path, ex);
        }
    }

    /**
     * Loads audio from memory bytes.
     *
     * @param fileName hint used to detect the container format (extension)
     * @param bytes    encoded audio bytes
     * @return decoded PCM data
     */
    public static PcmData loadFromMemory(String fileName, byte[] bytes) {
        try (InputStream stream = new BufferedInputStream(new java.io.ByteArrayInputStream(bytes))) {
            return decode(fileName, stream);
        } catch (IOException ex) {
            throw LogHelper.logAndThrow(log, "Failed to load audio from memory: " + fileName, ex);
        }
    }

    /**
     * Opens a streaming audio source from a classpath resource.
     *
     * @param classpathPath path relative to the classpath root
     * @return stream handle for incremental decoding
     */
    public static AudioStream openStreamFromClasspath(String classpathPath) {
        InputStream stream = AudioLoader.class.getClassLoader().getResourceAsStream(classpathPath);
        if (stream == null) {
            throw new IllegalArgumentException("Audio resource not found: " + classpathPath);
        }
        return AudioStream.from(classpathPath, stream);
    }

    /**
     * Opens a streaming audio source from a filesystem path.
     *
     * @param path file path
     * @return stream handle for incremental decoding
     */
    public static AudioStream openStreamFromFile(Path path) {
        try {
            return AudioStream.from(path.getFileName().toString(), Files.newInputStream(path));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open audio stream: " + path, ex);
        }
    }

    /**
     * Opens a streaming audio source from encoded bytes in memory.
     *
     * @param fileName format hint (extension)
     * @param bytes    encoded audio bytes
     * @return stream handle
     */
    public static AudioStream openStreamFromMemory(String fileName, byte[] bytes) {
        return AudioStream.from(fileName, new java.io.ByteArrayInputStream(bytes));
    }

    private static PcmData decode(String fileName, InputStream stream) throws IOException {
        String lower = fileName.toLowerCase();
        PcmData pcm;
        if (lower.endsWith(".ogg")) {
            pcm = decodeOgg(stream.readAllBytes());
        } else {
            pcm = decodeWav(stream, fileName);
        }
        log.debug("Decoded audio file={} channels={} sampleRate={} durationSec={}",
                fileName, pcm.channels(), pcm.sampleRate(), pcm.durationSeconds());
        return pcm;
    }

    private static PcmData decodeWav(InputStream stream, String fileName) throws IOException {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream)) {
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
            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(target, audioStream)) {
                byte[] bytes = pcmStream.readAllBytes();
                ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
                buffer.put(bytes).flip();
                return new PcmData(buffer, target.getChannels(), (int) target.getSampleRate());
            }
        } catch (Exception ex) {
            log.error("Unsupported or corrupt WAV data file={}", fileName);
            throw new IllegalArgumentException("Unsupported or corrupt WAV data", ex);
        }
    }

    private static PcmData decodeOgg(byte[] bytes) {
        ByteBuffer encoded = ByteBuffer.allocateDirect(bytes.length);
        encoded.put(bytes).flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);
            ShortBuffer samples = STBVorbis.stb_vorbis_decode_memory(encoded, channels, sampleRate);
            if (samples == null) {
                log.error("Failed to decode OGG audio");
                throw new IllegalArgumentException("Failed to decode OGG audio");
            }
            ByteBuffer pcm = ByteBuffer.allocateDirect(samples.remaining() * 2).order(ByteOrder.nativeOrder());
            while (samples.hasRemaining()) {
                pcm.putShort(samples.get());
            }
            pcm.flip();
            return new PcmData(pcm, channels.get(0), sampleRate.get(0));
        }
    }
}
