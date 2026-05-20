package org.llw.util.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileLogSinkTest {

    @Test
    void writesFormattedLine(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("app.log");
        try (FileLogSink sink = new FileLogSink(file)) {
            sink.log(new LogRecord(Instant.now(), LogLevel.INFO, "test", "message", null));
            sink.flush();
        }
        String content = Files.readString(file);
        assertTrue(content.contains("[INFO]"));
        assertTrue(content.contains("message"));
    }
}
