package org.llw.util.log;

import java.nio.file.Path;

/**
 * Logging helpers shared across engine layers.
 */
public final class LogHelper {
    private LogHelper() {
    }

    public static RuntimeException logAndThrow(Logger log, String message, Throwable cause) {
        log.error(message, cause);
        if (cause instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new IllegalStateException(message, cause);
    }

    public static String describePath(Path path) {
        return path == null ? "null" : path.toAbsolutePath().toString();
    }

    public static String formatBytes(long bytes) {
        return bytes + " bytes";
    }
}
