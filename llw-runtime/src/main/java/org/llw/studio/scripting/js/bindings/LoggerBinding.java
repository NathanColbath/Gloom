package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.log.StudioLogSink;
import org.llw.util.log.LogLevel;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: forwards script log lines to the editor console.
 */
public final class LoggerBinding {
    private static final ThreadLocal<String> PREFIX = new ThreadLocal<>();
    private final StudioLogSink console;

    /**
     * @param console editor console sink, or {@code null} to discard output
     */
    public LoggerBinding(StudioLogSink console) {
        this.console = console;
    }

    /**
     * @param prefix optional log prefix for the current thread
     */
    public static void setPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            PREFIX.remove();
        } else {
            PREFIX.set(prefix);
        }
    }

    /** Clears the thread-local log prefix. */
    public static void clearPrefix() {
        PREFIX.remove();
    }

    /**
     * @param message log text
     */
    @HostAccess.Export
    public void log(String message) {
        append(LogLevel.INFO, message);
    }

    /**
     * @param message warning text
     */
    @HostAccess.Export
    public void warn(String message) {
        append(LogLevel.WARN, message);
    }

    /**
     * @param message error text
     */
    @HostAccess.Export
    public void error(String message) {
        append(LogLevel.ERROR, message);
    }

    private void append(LogLevel level, String message) {
        if (console != null) {
            String prefix = PREFIX.get();
            console.append(level, prefix == null ? message : prefix + message);
        }
    }
}
