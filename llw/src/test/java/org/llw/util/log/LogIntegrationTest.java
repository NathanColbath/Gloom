package org.llw.util.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LogIntegrationTest {

    @AfterEach
    void tearDown() {
        Log.shutdown();
    }

    @Test
    void initWritesInfoAndErrorFiles(@TempDir Path temp) throws Exception {
        Log.init(LogConfig.builder()
                .logDir(temp)
                .minLevel(LogLevel.DEBUG)
                .consoleEnabled(false)
                .exitOnFatal(false)
                .build());

        Log.get("integration").info("started");
        Log.get("integration").warn("careful");
        Log.get("integration").error("broken");
        Log.flush();

        String app = Files.readString(temp.resolve("app.log"));
        String error = Files.readString(temp.resolve("error.log"));
        assertTrue(app.contains("started"));
        assertTrue(app.contains("broken"));
        assertTrue(error.contains("careful"));
        assertTrue(error.contains("broken"));
    }
}
