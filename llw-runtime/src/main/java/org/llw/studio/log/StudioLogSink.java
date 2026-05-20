package org.llw.studio.log;

import org.llw.util.log.LogLevel;

/**
 * Receives script compile and build log lines from runtime services.
 */
public interface StudioLogSink {
    /**
     * @param level   severity
     * @param message text to display
     */
    void append(LogLevel level, String message);
}
