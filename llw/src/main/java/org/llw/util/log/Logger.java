package org.llw.util.log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Named logger that writes through the global {@link Log} sinks.
 */
public final class Logger {
    private final String name;

    Logger(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean isTraceEnabled() {
        return Log.isEnabled(LogLevel.TRACE);
    }

    public boolean isDebugEnabled() {
        return Log.isEnabled(LogLevel.DEBUG);
    }

    public void trace(String message, Object... args) {
        log(LogLevel.TRACE, message, null, args);
    }

    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, null, args);
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, null, args);
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, null, args);
    }

    public void warn(String message, Throwable throwable, Object... args) {
        log(LogLevel.WARN, message, throwable, args);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, null, args);
    }

    public void error(String message, Throwable throwable, Object... args) {
        log(LogLevel.ERROR, message, throwable, args);
    }

    public void fatal(String message, Object... args) {
        Log.fatal(name, format(message, args), null);
    }

    public void fatal(String message, Throwable throwable, Object... args) {
        Log.fatal(name, format(message, args), throwable);
    }

    private void log(LogLevel level, String message, Throwable throwable, Object... args) {
        Log.log(level, name, format(message, args), throwable);
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", MatcherQuote(Objects.toString(arg)));
        }
        return result;
    }

    private static String MatcherQuote(String value) {
        return java.util.regex.Matcher.quoteReplacement(value);
    }
}
