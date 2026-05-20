package org.llw.studio.assets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetFileOperationsTest {
    @TempDir
    Path tempDir;

    private Path projectRoot;
    private Path assetsRoot;
    private Path subFolder;

    @BeforeEach
    void setUp() throws Exception {
        projectRoot = tempDir;
        assetsRoot = tempDir.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        subFolder = assetsRoot.resolve("Sprites");
        Files.createDirectories(subFolder);
        MetaFile.read(projectRoot, assetsRoot, subFolder);
    }

    @Test
    void copyFileAssignsNewGuid() throws Exception {
        Path source = subFolder.resolve("hero.png");
        Files.writeString(source, "pixels");
        MetaFile.MetaData sourceMeta = MetaFile.read(projectRoot, assetsRoot, source);

        AssetFileOperations.OperationResult result =
                AssetFileOperations.copyInto(source, assetsRoot, assetsRoot);

        assertTrue(result.success());
        Path copy = assetsRoot.resolve("hero.png");
        assertTrue(Files.exists(copy));
        MetaFile.MetaData copyMeta = MetaFile.read(projectRoot, assetsRoot, copy);
        assertNotEquals(sourceMeta.guid, copyMeta.guid);
        assertEquals("pixels", Files.readString(copy));
    }

    @Test
    void cutAndPastePreservesGuid() throws Exception {
        Path source = subFolder.resolve("clip.wav");
        Files.writeString(source, "audio");
        MetaFile.MetaData sourceMeta = MetaFile.read(projectRoot, assetsRoot, source);

        AssetFileOperations.OperationResult move =
                AssetFileOperations.moveInto(source, assetsRoot, assetsRoot);

        assertTrue(move.success());
        assertFalse(Files.exists(source));
        Path moved = assetsRoot.resolve("clip.wav");
        assertTrue(Files.exists(moved));
        assertEquals(sourceMeta.guid, MetaFile.read(projectRoot, assetsRoot, moved).guid);
    }

    @Test
    void deleteFileAndFolder() throws Exception {
        Path file = subFolder.resolve("temp.png");
        Files.writeString(file, "x");
        MetaFile.read(projectRoot, assetsRoot, file);

        assertTrue(AssetFileOperations.delete(file, assetsRoot).success());
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(MetaFile.metaPath(projectRoot, assetsRoot, file)));

        Path nested = subFolder.resolve("Nested");
        Files.createDirectories(nested);
        MetaFile.read(projectRoot, assetsRoot, nested);
        Path nestedFile = nested.resolve("inner.png");
        Files.writeString(nestedFile, "y");
        MetaFile.read(projectRoot, assetsRoot, nestedFile);

        assertTrue(AssetFileOperations.delete(subFolder, assetsRoot).success());
        assertFalse(Files.exists(subFolder));
    }

    @Test
    void moveFolderPreservesChildGuids() throws Exception {
        Path child = subFolder.resolve("tile.png");
        Files.writeString(child, "tile");
        MetaFile.MetaData childMeta = MetaFile.read(projectRoot, assetsRoot, child);

        Path target = assetsRoot.resolve("Moved");
        Files.createDirectories(target);
        MetaFile.read(projectRoot, assetsRoot, target);

        AssetFileOperations.OperationResult result =
                AssetFileOperations.moveInto(subFolder, target, assetsRoot);

        assertTrue(result.success());
        Path movedChild = target.resolve("Sprites").resolve("tile.png");
        assertTrue(Files.exists(movedChild));
        assertEquals(childMeta.guid, MetaFile.read(projectRoot, assetsRoot, movedChild).guid);
        assertFalse(Files.exists(subFolder));
    }

    @Test
    void rejectsCopyOutsideAssetsRoot() {
        Path outside = tempDir.resolve("Outside");
        AssetFileOperations.OperationResult result =
                AssetFileOperations.copyInto(assetsRoot.resolve("Sprites"), outside, assetsRoot);
        assertFalse(result.success());
    }

    @Test
    void rejectsMoveIntoDescendant() throws Exception {
        Path parent = assetsRoot.resolve("Parent");
        Files.createDirectories(parent);
        MetaFile.read(projectRoot, assetsRoot, parent);
        Path child = parent.resolve("Child");
        Files.createDirectories(child);
        MetaFile.read(projectRoot, assetsRoot, child);

        AssetFileOperations.OperationResult result =
                AssetFileOperations.moveInto(parent, child, assetsRoot);
        assertFalse(result.success());
    }

    @Test
    void createFolderAssignsGuid() throws Exception {
        AssetFileOperations.OperationResult result =
                AssetFileOperations.createFolder(assetsRoot, "Props", projectRoot, assetsRoot);

        assertTrue(result.success());
        Path created = assetsRoot.resolve("Props");
        assertTrue(Files.isDirectory(created));
        assertEquals(AssetType.FOLDER.name(), MetaFile.read(projectRoot, assetsRoot, created).type);
    }

    @Test
    void importExternalFileCreatesMetaUnderMetadataTree() throws Exception {
        Path externalRoot = tempDir.resolve("external");
        Files.createDirectories(externalRoot);
        Path externalFile = externalRoot.resolve("drop.png");
        Files.writeString(externalFile, "pixels");

        AssetFileOperations.OperationResult result =
                AssetFileOperations.importExternal(externalFile, subFolder, projectRoot, assetsRoot);

        assertTrue(result.success());
        Path imported = subFolder.resolve("drop.png");
        assertTrue(Files.exists(imported));
        assertTrue(Files.exists(MetaFile.metaPath(projectRoot, assetsRoot, imported)));
        assertFalse(Files.exists(MetaFile.metaPath(projectRoot, assetsRoot, externalFile)));
    }
}
