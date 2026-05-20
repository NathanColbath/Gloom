package org.llw.studio.project;

import org.llw.util.log.Log;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Moves legacy co-located metadata into {@code .studio/metadata/}.
 */
public final class MetadataLayoutMigration {
    private static final String LOG = "llw.studio.metadata";

    private MetadataLayoutMigration() {
    }

    /**
     * Ensures metadata directories exist and migrates legacy layouts when present.
     *
     * @param projectRoot project root directory
     * @return number of files moved
     * @throws IOException if migration cannot complete
     */
    public static int migrateIfNeeded(Path projectRoot) throws IOException {
        StudioProjectLayout.ensureMetadataDirs(projectRoot);
        AtomicInteger moved = new AtomicInteger();
        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
        if (Files.isDirectory(assetsRoot)) {
            Files.walkFileTree(assetsRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".meta")) {
                        Path assetPath = Path.of(file.toString().substring(0, file.toString().length() - 5));
                        Path destination = StudioProjectLayout.assetsMetaPath(projectRoot, assetsRoot, assetPath);
                        moveFileIfNeeded(file, destination, moved);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        moveDirectoryContents(
                StudioProjectLayout.legacyScriptSchemasDir(projectRoot),
                StudioProjectLayout.scriptSchemasDir(projectRoot),
                moved);
        moveDirectoryContents(
                StudioProjectLayout.legacyScriptCacheDir(projectRoot),
                StudioProjectLayout.scriptCacheDir(projectRoot),
                moved);
        moveDirectoryContents(
                StudioProjectLayout.legacyLlwScriptCacheDir(projectRoot),
                StudioProjectLayout.scriptCacheDir(projectRoot),
                moved);
        moveDirectoryContents(
                StudioProjectLayout.legacyLogsDir(projectRoot),
                StudioProjectLayout.logsDir(projectRoot),
                moved);
        StudioProjectLayout.deleteEmptyLegacyDirs(projectRoot);
        int count = moved.get();
        if (count > 0) {
            Log.get(LOG).info("Migrated " + count + " metadata file(s) to .studio/metadata/");
        }
        return count;
    }

    private static void moveDirectoryContents(Path sourceDir, Path targetDir, AtomicInteger moved) throws IOException {
        if (!Files.isDirectory(sourceDir)) {
            return;
        }
        Files.createDirectories(targetDir);
        try (Stream<Path> stream = Files.list(sourceDir)) {
            for (Path entry : stream.toList()) {
                if (Files.isDirectory(entry)) {
                    moveDirectoryContents(entry, targetDir.resolve(entry.getFileName()), moved);
                } else {
                    moveFileIfNeeded(entry, targetDir.resolve(entry.getFileName()), moved);
                }
            }
        }
    }

    private static void moveFileIfNeeded(Path source, Path destination, AtomicInteger moved) throws IOException {
        if (!Files.isRegularFile(source)) {
            return;
        }
        if (Files.exists(destination)) {
            Files.deleteIfExists(source);
            return;
        }
        Files.createDirectories(destination.getParent());
        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        moved.incrementAndGet();
    }

}
