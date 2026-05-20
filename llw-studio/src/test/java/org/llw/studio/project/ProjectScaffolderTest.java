package org.llw.studio.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectScaffolderTest {
    @Test
    void createsExpectedProjectLayout(@TempDir Path parent) throws Exception {
        ProjectDescriptor descriptor = ProjectScaffolder.create(parent, "TestGame");
        assertEquals("TestGame", descriptor.name());
        assertTrue(Files.isDirectory(descriptor.root().resolve("Assets/Scripts")));
        assertTrue(Files.isDirectory(descriptor.root().resolve("Scenes")));
        assertTrue(Files.exists(descriptor.root().resolve("Scenes/Main.scene.json")));
        assertTrue(Files.exists(descriptor.root().resolve("TestGame.llwproj")));
    }
}
