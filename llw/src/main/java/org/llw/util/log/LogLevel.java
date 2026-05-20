package org.llw.util.log;

/**
 * Log severity levels ordered from most to least verbose.
 */
public enum LogLevel {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public boolean isEnabledAt(LogLevel minLevel) {
        return priority >= minLevel.priority;
    }

    public static LogLevel parse(String value) {
        if (value == null || value.isBlank()) {
            return INFO;
        }
        return LogLevel.valueOf(value.trim().toUpperCase());
    }
}
