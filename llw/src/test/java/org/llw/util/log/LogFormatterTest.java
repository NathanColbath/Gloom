package org.llw.util.log;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LogFormatterTest {

    @Test
    void formatsLevelNameAndLogger() {
        LogRecord record = new LogRecord(Instant.parse("2026-05-13T17:34:12.456Z"), LogLevel.INFO, "test", "hello", null);
        String line = LogFormatter.format(record);
        assertTrue(line.contains("[INFO]"));
        assertTrue(line.contains("[test]"));
        assertTrue(line.contains("hello"));
    }
}
