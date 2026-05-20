package org.llw.util.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogLevelTest {

    @Test
    void priorityOrdering() {
        assertTrue(LogLevel.FATAL.priority() > LogLevel.ERROR.priority());
        assertTrue(LogLevel.DEBUG.isEnabledAt(LogLevel.DEBUG));
        assertFalse(LogLevel.TRACE.isEnabledAt(LogLevel.INFO));
    }

    @Test
    void parseLevel() {
        assertEquals(LogLevel.DEBUG, LogLevel.parse("debug"));
    }

    private static void assertEquals(LogLevel expected, LogLevel actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
