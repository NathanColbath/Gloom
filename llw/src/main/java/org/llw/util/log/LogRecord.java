package org.llw.util.log;

import java.time.Instant;

/**
 * Immutable log entry passed to sinks.
 */
public record LogRecord(
        Instant timestamp,
        LogLevel level,
        String loggerName,
        String message,
        Throwable throwable
) {}
