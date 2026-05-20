package org.llw.studio.log;

import org.llw.util.log.Log;
import org.llw.util.log.LogSink;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Registers additional {@link LogSink} instances with the global {@link Log} facility.
 */
public final class LogSinkRegistry {
    private LogSinkRegistry() {
    }

    /**
     * Appends {@code sink} to the internal {@link Log} sink list via reflection.
     *
     * @param sink sink to receive log records
     * @throws IllegalStateException when the {@link Log} implementation cannot be accessed
     */
    @SuppressWarnings("unchecked")
    public static void attach(LogSink sink) {
        try {
            Field field = Log.class.getDeclaredField("sinks");
            field.setAccessible(true);
            List<LogSink> sinks = (List<LogSink>) field.get(null);
            sinks.add(sink);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to attach studio log sink", ex);
        }
    }
}
