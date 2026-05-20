package org.llw.studio.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStagingTest {
    @Test
    void locateEngineRootFindsGloomCheckoutFromWorkingDirectory() {
        Path gloomRoot = Paths.get(".").toAbsolutePath().normalize();
        Path found = PlayerStaging.locateEngineRoot(gloomRoot.resolve("llw-studio/studio-project"));
        assertNotNull(found, "Expected to locate Gloom engine root");
        assertTrue(Files.isRegularFile(found.resolve("llw-player/build.gradle.kts")));
    }

    @Test
    void locateEngineRootFindsCheckoutForExternalProjectPath() {
        Path externalProject = Paths.get(System.getProperty("java.io.tmpdir")).resolve("llw-external-project-test");
        Path found = PlayerStaging.locateEngineRoot(externalProject);
        assertNotNull(found, "Expected engine root via user.dir or code source, not project path");
        assertTrue(Files.isRegularFile(found.resolve("settings.gradle.kts")));
    }

    @Test
    void bundledPlayerJarIsAvailableOnClasspath() throws IOException {
        Path projectRoot = Paths.get(System.getProperty("java.io.tmpdir")).resolve("llw-no-engine-root");
        var stream = PlayerStaging.class.getResourceAsStream("/player/llw-player.jar");
        assertNotNull(stream, "Bundled player jar should be on the test classpath");
        stream.close();
    }
}
