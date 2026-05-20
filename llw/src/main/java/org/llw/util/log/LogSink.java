package org.llw.util.log;

/**
 * Destination for formatted log records.
 */
public interface LogSink extends AutoCloseable {
    void log(LogRecord record);

    void flush();

    @Override
    void close();
}
