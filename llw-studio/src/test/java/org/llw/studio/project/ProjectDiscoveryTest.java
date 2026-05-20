package org.llw.studio.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectDiscoveryTest {
    @Test
    void readsStartupSceneFromScaffoldedProject(@TempDir Path parent) throws Exception {
        ProjectDescriptor created = ProjectScaffolder.create(parent, "DiscoveryGame");
        ProjectDescriptor discovered = ProjectDiscovery.discover(created.root());
        assertEquals("DiscoveryGame", discovered.name());
        assertEquals("Scenes/Main.scene.json", discovered.startupSceneRelative());
    }
}
