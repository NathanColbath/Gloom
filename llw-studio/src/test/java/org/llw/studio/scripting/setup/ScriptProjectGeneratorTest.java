package org.llw.studio.scripting.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptProjectGeneratorTest {
    @Test
    void upgradesSdkWhenVersionIsStale(@TempDir Path projectRoot) throws Exception {
        Path sdkDir = projectRoot.resolve(".llw/sdk");
        Files.createDirectories(sdkDir);
        Files.writeString(sdkDir.resolve("version.txt"), "8");

        ScriptProjectGenerator.ensureProject(projectRoot);

        assertEquals("17", Files.readString(sdkDir.resolve("version.txt")).trim());
        assertTrue(Files.exists(sdkDir.resolve("physics2d.d.ts")));
        assertTrue(Files.exists(sdkDir.resolve("ui.d.ts")));
        assertTrue(Files.exists(sdkDir.resolve("keys.d.ts")));
        assertTrue(Files.readString(sdkDir.resolve("index.d.ts")).contains("Physics2D"));
        assertTrue(Files.readString(sdkDir.resolve("keys.d.ts")).contains("VK_SPACE"));
    }
}
