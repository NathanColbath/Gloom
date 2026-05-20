package org.llw.studio.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentProjectsStoreTest {
    @Test
    void dedupesAndCapsEntries(@TempDir Path parent) throws Exception {
        RecentProjectsStore store = new RecentProjectsStore();
        for (int i = 0; i < 12; i++) {
            ProjectDescriptor descriptor = ProjectScaffolder.create(parent, "Game" + i);
            store.add(descriptor);
        }
        assertTrue(store.entries().size() <= 10);
        ProjectDescriptor latest = ProjectScaffolder.create(parent, "GameRepeat");
        store.add(latest);
        store.add(latest);
        assertEquals(latest.root().toAbsolutePath().normalize(), store.entries().get(0).path());
    }
}
