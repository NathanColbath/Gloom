package org.llw.resources.pack;

import org.llw.resources.AssetType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parsed manifest describing entries inside an asset pack payload.
 */
public final class AssetPackManifest {
    private final int version;
    private final Map<String, Entry> entries;

    public AssetPackManifest(int version, Map<String, Entry> entries) {
        this.version = version;
        this.entries = Map.copyOf(entries);
    }

    /** Returns manifest format version. */
    public int version() {
        return version;
    }

    /** Returns an unmodifiable view of pack entries keyed by asset id. */
    public Map<String, Entry> entries() {
        return entries;
    }

    /**
     * Single asset entry within a pack.
     *
     * @param type     asset kind
     * @param offset   byte offset relative to payload start
     * @param length   byte length in payload
     * @param hint     original filename hint (extension for audio decode)
     * @param fontSize required when {@code type} is {@link AssetType#FONT}
     */
    public record Entry(AssetType type, int offset, int length, String hint, int fontSize) {
        public Entry {
            if (length < 0 || offset < 0) {
                throw new IllegalArgumentException("offset and length must be non-negative");
            }
        }
    }

    /**
     * Builder entry used when writing packs.
     */
    public record PackEntry(AssetType type, java.nio.file.Path source, String hint, int fontSize) {
        public PackEntry(AssetType type, java.nio.file.Path source) {
            this(type, source, source.getFileName().toString(), 0);
        }

        public PackEntry(AssetType type, java.nio.file.Path source, int fontSize) {
            this(type, source, source.getFileName().toString(), fontSize);
        }
    }
}
