package org.llw.studio.log;

import org.llw.studio.editor.panels.ConsolePanel;
import org.llw.util.log.LogLevel;
import org.llw.util.log.LogRecord;
import org.llw.util.log.LogSink;

/**
 * Forwards {@linkplain org.llw.util.log.Log LLW log} records and ad-hoc messages to the editor {@link ConsolePanel}.
 */
public final class ConsoleLogSink implements LogSink, StudioLogSink {
    private final ConsolePanel console;

    /**
     * @param console panel that displays log lines in the editor UI
     */
    public ConsoleLogSink(ConsolePanel console) {
        this.console = console;
    }

    /** {@inheritDoc} */
    @Override
    public void log(LogRecord record) {
        console.append(record.level(), record.loggerName() + ": " + record.message());
        if (record.throwable() != null) {
            console.append(record.level(), record.throwable().toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void flush() {
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }

    /**
     * Appends a single line without going through the {@linkplain org.llw.util.log.Log LLW log} pipeline.
     *
     * @param level   severity
     * @param message text to display
     */
    public void append(LogLevel level, String message) {
        console.append(level, message);
    }
}
