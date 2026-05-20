package org.llw.studio.editor.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists Iconify SVG responses on disk so icons load without re-downloading every session.
 */
public final class EditorIconDiskCache {
    private final Path cacheRoot;

    public EditorIconDiskCache(Path cacheRoot) {
        this.cacheRoot = cacheRoot;
    }

    public static EditorIconDiskCache defaultCache() {
        String home = System.getProperty("user.home", ".");
        return new EditorIconDiskCache(Path.of(home, ".llw-studio", "icon-cache"));
    }

    /**
     * @param spec   icon descriptor
     * @param pixels requested raster size used in the API query
     * @return cached SVG bytes, or {@code null} if not on disk
     */
    public byte[] read(OpenSourceIconSpec spec, int pixels) {
        Path file = cacheFile(spec, pixels);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param spec   icon descriptor
     * @param pixels requested raster size
     * @param svg    SVG bytes from Iconify
     */
    public void write(OpenSourceIconSpec spec, int pixels, byte[] svg) {
        if (svg == null || svg.length == 0) {
            return;
        }
        Path file = cacheFile(spec, pixels);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, svg);
        } catch (IOException ignored) {
        }
    }

    private Path cacheFile(OpenSourceIconSpec spec, int pixels) {
        String safeName = spec.prefix() + "-" + spec.name() + "-" + pixels + ".svg";
        return cacheRoot.resolve(safeName);
    }
}
