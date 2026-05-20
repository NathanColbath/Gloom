package org.llw.audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;

import org.llw.util.log.EnvironmentLog;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Manages the OpenAL device, context, buffer uploads, and listener state.
 */
final class OpenAlBackend {
    private static final Logger log = Log.get(Loggers.AUDIO);

    private long device;
    private long context;
    private boolean initialized;

    /**
     * Opens the default playback device and makes an OpenAL context current.
     */
    void initialize() {
        if (initialized) {
            return;
        }
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == 0L) {
            log.error("Failed to open the default OpenAL playback device");
            throw new IllegalStateException("Failed to open the default OpenAL playback device");
        }

        ALCCapabilities deviceCapabilities = ALC.createCapabilities(device);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer attributes = stack.callocInt(1);
            context = ALC10.alcCreateContext(device, attributes);
        }
        if (context == 0L) {
            ALC10.alcCloseDevice(device);
            device = 0L;
            log.error("Failed to create the OpenAL context");
            throw new IllegalStateException("Failed to create the OpenAL context");
        }

        if (!ALC10.alcMakeContextCurrent(context)) {
            ALC10.alcDestroyContext(context);
            ALC10.alcCloseDevice(device);
            context = 0L;
            device = 0L;
            log.error("Failed to make the OpenAL context current");
            throw new IllegalStateException("Failed to make the OpenAL context current");
        }

        AL.createCapabilities(deviceCapabilities);
        AL10.alDistanceModel(AL10.AL_NONE);
        initialized = true;
        String deviceName = ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER);
        EnvironmentLog.logOpenAl(deviceName, AL10.alGetString(AL10.AL_VERSION), AL10.alGetString(AL10.AL_RENDERER));
        log.info("OpenAL initialized device={}", deviceName);
    }

    /**
     * Uploads 16-bit PCM samples to a new OpenAL buffer.
     *
     * @param pcmSamples interleaved 16-bit little-endian PCM samples
     * @param channels   channel count (1 or 2)
     * @param sampleRate sample rate in Hz
     * @return OpenAL buffer name
     */
    int createBuffer(ByteBuffer pcmSamples, int channels, int sampleRate) {
        ensureInitialized();
        int format = toAlFormat(channels);
        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, format, pcmSamples, sampleRate);
        return buffer;
    }

    /**
     * Deletes an OpenAL buffer.
     *
     * @param buffer OpenAL buffer name
     */
    void deleteBuffer(int buffer) {
        if (buffer != 0) {
            AL10.alDeleteBuffers(buffer);
        }
    }

    /**
     * Returns whether the OpenAL context is active.
     *
     * @return {@code true} when initialized and not disposed
     */
    boolean isInitialized() {
        return initialized;
    }

    /**
     * Destroys the OpenAL context and closes the device.
     */
    void dispose() {
        if (!initialized) {
            return;
        }
        ALC10.alcMakeContextCurrent(0L);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
        context = 0L;
        device = 0L;
        initialized = false;
        log.info("OpenAL disposed");
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("OpenAL backend is not initialized");
        }
    }

    private static int toAlFormat(int channels) {
        return switch (channels) {
            case 1 -> AL10.AL_FORMAT_MONO16;
            case 2 -> AL10.AL_FORMAT_STEREO16;
            default -> throw new IllegalArgumentException("Unsupported channel count: " + channels);
        };
    }
}
