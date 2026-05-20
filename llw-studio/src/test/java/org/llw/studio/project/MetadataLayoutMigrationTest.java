package org.llw.studio.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataLayoutMigrationTest {
    @Test
    void migratesAssetSidecarsAndLegacyScriptCache(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Path scriptsDir = assetsRoot.resolve("Scripts");
        Files.createDirectories(scriptsDir);
        Path script = scriptsDir.resolve("Player.ts");
        Files.writeString(script, "export default class Player {}");

        Path legacyMeta = StudioProjectLayout.legacyAssetsMetaPath(script);
        Files.writeString(legacyMeta, """
                {"guid":"abc-123","type":"SCRIPT","importer":{}}
                """);

        Path legacyCacheDir = StudioProjectLayout.legacyScriptCacheDir(projectRoot);
        Files.createDirectories(legacyCacheDir);
        Files.writeString(legacyCacheDir.resolve("abc-123.js"), "bundled");

        int moved = MetadataLayoutMigration.migrateIfNeeded(projectRoot);

        assertTrue(moved >= 2);
        assertFalse(Files.exists(legacyMeta));
        assertTrue(Files.exists(StudioProjectLayout.assetsMetaPath(projectRoot, assetsRoot, script)));
        assertTrue(Files.exists(StudioProjectLayout.scriptCachePath(projectRoot, "abc-123")));
        assertFalse(Files.exists(legacyCacheDir.resolve("abc-123.js")));
    }

    @Test
    void isIdempotent(@TempDir Path projectRoot) throws Exception {
        StudioProjectLayout.ensureMetadataDirs(projectRoot);
        assertEquals(0, MetadataLayoutMigration.migrateIfNeeded(projectRoot));
        assertEquals(0, MetadataLayoutMigration.migrateIfNeeded(projectRoot));
    }
}
