package org.llw.util.log;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global logging entry point for LLW.
 */
public final class Log {
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private static volatile LogConfig config = LogConfig.defaults();
    private static final List<LogSink> sinks = new ArrayList<>();
    private static LogSink appSink;
    private static LogSink errorSink;
    private static boolean initialized;

    private Log() {
    }

    public static synchronized void init(LogConfig newConfig) {
        if (initialized) {
            get("llw.util.log").warn("Log.init called more than once; ignoring");
            return;
        }
        config = newConfig;
        FrameDiagnostics.configure(newConfig.frameDiagnosticsIntervalSec());
        try {
            appSink = new FileLogSink(newConfig.logDir().resolve(newConfig.appFileName()));
            errorSink = new ErrorLogSink(new FileLogSink(newConfig.logDir().resolve(newConfig.errorFileName())));
            sinks.add(appSink);
            sinks.add(errorSink);
            if (newConfig.consoleEnabled()) {
                sinks.add(new ConsoleLogSink());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize log files in " + newConfig.logDir(), e);
        }
        initialized = true;
        EnvironmentLog.logSessionStart();
    }

    public static synchronized void initForTests(LogConfig newConfig) {
        shutdown();
        initialized = false;
        sinks.clear();
        appSink = null;
        errorSink = null;
        if (newConfig != null) {
            init(newConfig);
        } else {
            config = LogConfig.builder().consoleEnabled(false).build();
            initialized = true;
        }
    }

    public static Logger get(String name) {
        return LOGGERS.computeIfAbsent(name, Logger::new);
    }

    public static boolean isEnabled(LogLevel level) {
        return level.isEnabledAt(config.minLevel());
    }

    static void log(LogLevel level, String loggerName, String message, Throwable throwable) {
        if (!isEnabled(level)) {
            return;
        }
        LogRecord record = new LogRecord(Instant.now(), level, loggerName, message, throwable);
        List<LogSink> active;
        synchronized (Log.class) {
            active = List.copyOf(sinks);
        }
        for (LogSink sink : active) {
            sink.log(record);
        }
        if (level == LogLevel.FATAL) {
            handleFatal(loggerName, message, throwable);
        }
    }

    public static void fatal(String message) {
        fatal("llw", message, null);
    }

    public static void fatal(String message, Throwable throwable) {
        fatal("llw", message, throwable);
    }

    static void fatal(String loggerName, String message, Throwable throwable) {
        log(LogLevel.FATAL, loggerName, message, throwable);
    }

    private static void handleFatal(String loggerName, String message, Throwable throwable) {
        flush();
        String body = message;
        if (throwable != null) {
            body = message + System.lineSeparator() + LogFormatter.format(
                    new LogRecord(Instant.now(), LogLevel.FATAL, loggerName, "", throwable)
            );
        }
        NativeDialogs.showError("Fatal Error", body);
        shutdown();
        if (config.exitOnFatal()) {
            System.exit(1);
        }
    }

    public static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                fatal("Uncaught exception in thread " + thread.getName(), throwable));
    }

    public static synchronized void flush() {
        for (LogSink sink : sinks) {
            sink.flush();
        }
    }

    public static synchronized void shutdown() {
        for (LogSink sink : sinks) {
            sink.close();
        }
        sinks.clear();
        appSink = null;
        errorSink = null;
        initialized = false;
    }
}
