package org.llw.util.log;

import java.nio.file.Path;

/**
 * Logging runtime configuration.
 */
public final class LogConfig {
    private final LogLevel minLevel;
    private final Path logDir;
    private final String appFileName;
    private final String errorFileName;
    private final boolean consoleEnabled;
    private final float frameDiagnosticsIntervalSec;
    private final boolean exitOnFatal;

    private LogConfig(Builder builder) {
        this.minLevel = builder.minLevel;
        this.logDir = builder.logDir;
        this.appFileName = builder.appFileName;
        this.errorFileName = builder.errorFileName;
        this.consoleEnabled = builder.consoleEnabled;
        this.frameDiagnosticsIntervalSec = builder.frameDiagnosticsIntervalSec;
        this.exitOnFatal = builder.exitOnFatal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LogConfig defaults() {
        return builder().build();
    }

    public LogLevel minLevel() {
        return minLevel;
    }

    public Path logDir() {
        return logDir;
    }

    public String appFileName() {
        return appFileName;
    }

    public String errorFileName() {
        return errorFileName;
    }

    public boolean consoleEnabled() {
        return consoleEnabled;
    }

    public float frameDiagnosticsIntervalSec() {
        return frameDiagnosticsIntervalSec;
    }

    public boolean exitOnFatal() {
        return exitOnFatal;
    }

    public static final class Builder {
        private LogLevel minLevel = resolveLevel();
        private Path logDir = resolveLogDir();
        private String appFileName = "app.log";
        private String errorFileName = "error.log";
        private boolean consoleEnabled = true;
        private float frameDiagnosticsIntervalSec = 1.0f;
        private boolean exitOnFatal = true;

        public Builder minLevel(LogLevel minLevel) {
            this.minLevel = minLevel;
            return this;
        }

        public Builder logDir(Path logDir) {
            this.logDir = logDir;
            return this;
        }

        public Builder appFileName(String appFileName) {
            this.appFileName = appFileName;
            return this;
        }

        public Builder errorFileName(String errorFileName) {
            this.errorFileName = errorFileName;
            return this;
        }

        public Builder consoleEnabled(boolean consoleEnabled) {
            this.consoleEnabled = consoleEnabled;
            return this;
        }

        public Builder frameDiagnosticsIntervalSec(float frameDiagnosticsIntervalSec) {
            this.frameDiagnosticsIntervalSec = frameDiagnosticsIntervalSec;
            return this;
        }

        public Builder exitOnFatal(boolean exitOnFatal) {
            this.exitOnFatal = exitOnFatal;
            return this;
        }

        public LogConfig build() {
            return new LogConfig(this);
        }

        private static LogLevel resolveLevel() {
            String prop = System.getProperty("llw.log.level");
            if (prop == null || prop.isBlank()) {
                prop = System.getenv("LLW_LOG_LEVEL");
            }
            if (prop == null || prop.isBlank()) {
                return LogLevel.INFO;
            }
            try {
                return LogLevel.parse(prop);
            } catch (IllegalArgumentException ex) {
                return LogLevel.INFO;
            }
        }

        private static Path resolveLogDir() {
            String prop = System.getProperty("llw.log.dir");
            if (prop == null || prop.isBlank()) {
                prop = System.getenv("LLW_LOG_DIR");
            }
            if (prop == null || prop.isBlank()) {
                return Path.of("logs");
            }
            return Path.of(prop);
        }
    }
}
