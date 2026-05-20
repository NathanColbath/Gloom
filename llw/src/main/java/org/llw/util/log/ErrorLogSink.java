package org.llw.util.log;

/**
 * Forwards WARN and above to a delegate sink.
 */
public final class ErrorLogSink implements LogSink {
    private final LogSink delegate;

    public ErrorLogSink(LogSink delegate) {
        this.delegate = delegate;
    }

    @Override
    public void log(LogRecord record) {
        if (record.level().priority() >= LogLevel.WARN.priority()) {
            delegate.log(record);
        }
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
