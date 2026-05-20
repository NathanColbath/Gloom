package org.llw.resources;

import org.llw.resources.pack.AssetPackManifest;

import java.nio.file.Path;

/**
 * Describes where asset bytes are loaded from.
 */
public sealed interface AssetSource permits AssetSource.Classpath, AssetSource.File, AssetSource.PackSlice {

    /**
     * Classpath resource path.
     *
     * @param path path relative to the class loader root
     */
    record Classpath(String path) implements AssetSource {}

    /**
     * Filesystem path.
     *
     * @param path absolute or relative file path
     */
    record File(Path path) implements AssetSource {}

    /**
     * Slice inside a loaded pack payload.
     *
     * @param packReader shared reader holding pack bytes
     * @param entry      manifest entry with offset and length
     */
    record PackSlice(org.llw.resources.pack.AssetPackReader packReader, AssetPackManifest.Entry entry) implements AssetSource {}
}
