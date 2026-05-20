package org.llw.util.log;

/**
 * Mirrors log records to stdout/stderr.
 */
public final class ConsoleLogSink implements LogSink {
    @Override
    public void log(LogRecord record) {
        String line = LogFormatter.format(record);
        if (record.level().priority() >= LogLevel.WARN.priority()) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
    }

    @Override
    public void flush() {
        System.out.flush();
        System.err.flush();
    }

    @Override
    public void close() {
        flush();
    }
}
