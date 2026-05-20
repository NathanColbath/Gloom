package org.llw.resources.pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.llw.render.resources.ResourceLoader;
import org.llw.util.log.Log;
import org.llw.util.log.Logger;
import org.llw.util.log.Loggers;

/**
 * Reads LLWP {@code .pack} files from classpath, filesystem, or raw bytes.
 */
public final class AssetPackReader {
    private static final Logger log = Log.get(Loggers.RESOURCES_PACK);

    private final byte[] fileBytes;
    private final AssetPackManifest manifest;
    private final int payloadOffset;

    private AssetPackReader(byte[] fileBytes, AssetPackManifest manifest, int payloadOffset) {
        this.fileBytes = fileBytes;
        this.manifest = manifest;
        this.payloadOffset = payloadOffset;
    }

    /**
     * Loads a pack from the classpath.
     *
     * @param classpathPath path to {@code .pack} resource
     * @return reader positioned at manifest and payload
     */
    public static AssetPackReader fromClasspath(String classpathPath) {
        return fromBytes(ResourceLoader.loadBytes(classpathPath));
    }

    /**
     * Loads a pack from the filesystem.
     *
     * @param path pack file path
     * @return reader
     * @throws IOException if the file cannot be read
     */
    public static AssetPackReader fromFile(Path path) throws IOException {
        return fromBytes(Files.readAllBytes(path));
    }

    /**
     * Parses an in-memory pack file.
     *
     * @param bytes full pack bytes
     * @return reader
     */
    public static AssetPackReader fromBytes(byte[] bytes) {
        AssetPackFormat.PackHeader header = AssetPackFormat.readHeader(bytes);
        AssetPackManifest manifest = ManifestJson.parse(header.manifestJson());
        log.debug("Parsed pack version={} entries={} payloadOffset={}",
                header.version(), manifest.entries().size(), header.payloadOffset());
        return new AssetPackReader(bytes, manifest, header.payloadOffset());
    }

    /** Returns the parsed manifest. */
    public AssetPackManifest manifest() {
        return manifest;
    }

    /**
     * Copies payload bytes for an entry.
     *
     * @param entry manifest entry
     * @return slice of concatenated payload
     */
    public byte[] slice(AssetPackManifest.Entry entry) {
        int start = payloadOffset + entry.offset();
        int end = start + entry.length();
        if (end > fileBytes.length) {
            throw new IllegalArgumentException("Entry exceeds pack payload: " + entry.hint());
        }
        return Arrays.copyOfRange(fileBytes, start, end);
    }

    /** Returns the full backing bytes (includes header and manifest). */
    public byte[] fileBytes() {
        return fileBytes;
    }

    /** Returns payload start offset within {@link #fileBytes()}. */
    public int payloadOffset() {
        return payloadOffset;
    }
}
