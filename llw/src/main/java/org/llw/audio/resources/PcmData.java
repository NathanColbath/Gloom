package org.llw.audio.resources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Decoded PCM audio in 16-bit signed little-endian interleaved layout.
 *
 * @param samples    sample bytes (16-bit LE)
 * @param channels   channel count
 * @param sampleRate sample rate in Hz
 */
public record PcmData(ByteBuffer samples, int channels, int sampleRate) {
    /**
     * Returns the duration of the audio in seconds.
     *
     * @return duration in seconds
     */
    public float durationSeconds() {
        int frameCount = samples.remaining() / (2 * channels);
        return frameCount / (float) sampleRate;
    }

    /**
     * Returns a duplicate buffer positioned at the start with native byte order.
     *
     * @return prepared PCM buffer
     */
    public ByteBuffer preparedSamples() {
        ByteBuffer copy = samples.duplicate();
        copy.order(ByteOrder.nativeOrder());
        copy.position(0);
        return copy;
    }
}
