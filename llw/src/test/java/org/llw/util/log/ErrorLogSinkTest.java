package org.llw.util.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorLogSinkTest {

    @Test
    void writesWarnAndAboveOnly(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("error.log");
        try (FileLogSink delegate = new FileLogSink(file);
             ErrorLogSink sink = new ErrorLogSink(delegate)) {
            sink.log(new LogRecord(Instant.now(), LogLevel.INFO, "test", "info", null));
            sink.log(new LogRecord(Instant.now(), LogLevel.WARN, "test", "warn", null));
            sink.flush();
        }
        String content = Files.readString(file);
        assertFalse(content.contains("info"));
        assertTrue(content.contains("warn"));
    }
}
