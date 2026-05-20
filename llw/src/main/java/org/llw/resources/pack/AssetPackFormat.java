package org.llw.resources.pack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Binary layout constants for LLW asset pack files ({@code .pack}).
 */
public final class AssetPackFormat {
    /** Magic bytes {@code LLWP}. */
    public static final int MAGIC = 0x50574C4C;
    public static final int VERSION = 1;
    public static final int HEADER_SIZE = 12;

    private AssetPackFormat() {}

    /**
     * Writes the pack header and returns the byte offset where the payload begins.
     *
     * @param buffer target buffer positioned at start
     * @param jsonLength manifest UTF-8 length in bytes
     * @return payload start offset (= {@value HEADER_SIZE} + jsonLength)
     */
    public static int writeHeader(ByteBuffer buffer, int jsonLength) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(MAGIC);
        buffer.putInt(VERSION);
        buffer.putInt(jsonLength);
        return HEADER_SIZE + jsonLength;
    }

    /**
     * Reads and validates the header.
     *
     * @param bytes full pack file
     * @return parsed header
     */
    public static PackHeader readHeader(byte[] bytes) {
        if (bytes.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Pack file too small");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int magic = buffer.getInt();
        if (magic != MAGIC) {
            throw new IllegalArgumentException("Invalid pack magic");
        }
        int version = buffer.getInt();
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported pack version: " + version);
        }
        int jsonLength = buffer.getInt();
        if (HEADER_SIZE + jsonLength > bytes.length) {
            throw new IllegalArgumentException("Manifest length exceeds file size");
        }
        String json = new String(bytes, HEADER_SIZE, jsonLength, StandardCharsets.UTF_8);
        int payloadOffset = HEADER_SIZE + jsonLength;
        int payloadLength = bytes.length - payloadOffset;
        return new PackHeader(version, json, payloadOffset, payloadLength);
    }

    /**
     * Parsed pack header.
     *
     * @param version        format version
     * @param manifestJson   raw manifest JSON
     * @param payloadOffset  byte offset where concatenated asset data starts
     * @param payloadLength  total payload bytes
     */
    public record PackHeader(int version, String manifestJson, int payloadOffset, int payloadLength) {}
}
