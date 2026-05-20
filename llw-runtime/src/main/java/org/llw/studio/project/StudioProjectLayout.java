package org.llw.studio.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Canonical paths for project metadata under {@code .studio/metadata/}.
 */
public final class StudioProjectLayout {
    private static final String STUDIO_DIR = ".studio";
    private static final String METADATA_DIR = "metadata";

    private StudioProjectLayout() {
    }

    /** @return {@code {projectRoot}/.studio/metadata} */
    public static Path metadataRoot(Path projectRoot) {
        return projectRoot.resolve(STUDIO_DIR).resolve(METADATA_DIR);
    }

    /** @return {@code {projectRoot}/Assets} */
    public static Path assetsRoot(Path projectRoot) {
        return projectRoot.resolve("Assets");
    }

    /** @return {@code .studio/metadata/assets} */
    public static Path assetsMetaRoot(Path projectRoot) {
        return metadataRoot(projectRoot).resolve("assets");
    }

    /** @return {@code .studio/metadata/script-schemas} */
    public static Path scriptSchemasDir(Path projectRoot) {
        return metadataRoot(projectRoot).resolve("script-schemas");
    }

    /** @return {@code .studio/metadata/script-cache} */
    public static Path scriptCacheDir(Path projectRoot) {
        return metadataRoot(projectRoot).resolve("script-cache");
    }

    /** @return {@code .studio/metadata/logs} */
    public static Path logsDir(Path projectRoot) {
        return metadataRoot(projectRoot).resolve("logs");
    }

    /**
     * @param projectRoot project root
     * @param scriptGuid script asset GUID
     * @return schema JSON path in the metadata tree
     */
    public static Path scriptSchemaPath(Path projectRoot, String scriptGuid) {
        return scriptSchemasDir(projectRoot).resolve(scriptGuid + ".json");
    }

    /**
     * @param projectRoot project root
     * @param scriptGuid script asset GUID
     * @return bundled script path in the metadata tree
     */
    public static Path scriptCachePath(Path projectRoot, String scriptGuid) {
        return scriptCacheDir(projectRoot).resolve(scriptGuid + ".js");
    }

    /**
     * Resolves centralized meta for an asset under {@code Assets/}.
     *
     * @param projectRoot project root
     * @param assetsRoot  {@code Assets} directory (usually {@link #assetsRoot(Path)})
     * @param assetPath   file or folder inside {@code assetsRoot}
     * @return path under {@code .studio/metadata/assets/}
     */
    public static Path assetsMetaPath(Path projectRoot, Path assetsRoot, Path assetPath) {
        Path normalizedAsset = assetPath.normalize();
        Path relative = assetsRoot.normalize().relativize(normalizedAsset);
        String relativeKey = relative.toString().replace('\\', '/');
        if (relativeKey.isEmpty()) {
            relativeKey = assetsRoot.getFileName().toString();
        }
        return assetsMetaRoot(projectRoot).resolve(relativeKey + ".meta");
    }

    /** Legacy sidecar next to the asset (pre-migration). */
    public static Path legacyAssetsMetaPath(Path assetPath) {
        return Path.of(assetPath.toString() + ".meta");
    }

    /** @return legacy {@code .studio/script-schemas} directory */
    public static Path legacyScriptSchemasDir(Path projectRoot) {
        return projectRoot.resolve(STUDIO_DIR).resolve("script-schemas");
    }

    /** @return legacy {@code .studio/script-cache} directory */
    public static Path legacyScriptCacheDir(Path projectRoot) {
        return projectRoot.resolve(STUDIO_DIR).resolve("script-cache");
    }

    /** @return legacy {@code .llw/script-cache} directory */
    public static Path legacyLlwScriptCacheDir(Path projectRoot) {
        return projectRoot.resolve(".llw").resolve("script-cache");
    }

    /** @return legacy {@code .studio/logs} directory */
    public static Path legacyLogsDir(Path projectRoot) {
        return projectRoot.resolve(STUDIO_DIR).resolve("logs");
    }

    /**
     * Creates metadata subfolders if missing.
     *
     * @param projectRoot project root
     * @throws IOException if directories cannot be created
     */
    public static void ensureMetadataDirs(Path projectRoot) throws IOException {
        Files.createDirectories(assetsMetaRoot(projectRoot));
        Files.createDirectories(scriptSchemasDir(projectRoot));
        Files.createDirectories(scriptCacheDir(projectRoot));
        Files.createDirectories(logsDir(projectRoot));
    }

    /**
     * @param pathInProject any path inside a project
     * @return the nearest ancestor named {@code Assets}, or {@code null} if none
     */
    public static Path tryFindAssetsRoot(Path pathInProject) {
        Path current = pathInProject.toAbsolutePath().normalize();
        while (current != null) {
            Path name = current.getFileName();
            if (name != null && "Assets".equals(name.toString())) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Resolves a script cache file, preferring the metadata tree then legacy locations.
     */
    public static Path resolveScriptCachePath(Path projectRoot, String scriptGuid) {
        Path primary = scriptCachePath(projectRoot, scriptGuid);
        if (Files.isRegularFile(primary)) {
            return primary;
        }
        Path legacyStudio = legacyScriptCacheDir(projectRoot).resolve(scriptGuid + ".js");
        if (Files.isRegularFile(legacyStudio)) {
            return legacyStudio;
        }
        Path legacyLlw = legacyLlwScriptCacheDir(projectRoot).resolve(scriptGuid + ".js");
        if (Files.isRegularFile(legacyLlw)) {
            return legacyLlw;
        }
        return primary;
    }

    /**
     * Resolves a script schema file, preferring the metadata tree then legacy locations.
     */
    public static Path resolveScriptSchemaPath(Path projectRoot, String scriptGuid) {
        Path primary = scriptSchemaPath(projectRoot, scriptGuid);
        if (Files.isRegularFile(primary)) {
            return primary;
        }
        Path legacy = legacyScriptSchemasDir(projectRoot).resolve(scriptGuid + ".json");
        if (Files.isRegularFile(legacy)) {
            return legacy;
        }
        return primary;
    }

    /**
     * Resolves a logs directory, preferring the metadata tree then legacy {@code .studio/logs}.
     */
    public static Path resolveLogsDir(Path projectRoot) {
        Path primary = logsDir(projectRoot);
        if (Files.isDirectory(primary)) {
            return primary;
        }
        Path legacy = legacyLogsDir(projectRoot);
        if (Files.isDirectory(legacy)) {
            return legacy;
        }
        return primary;
    }

    /** @return {@code .studio/extract-schema.mjs} (tool, not under metadata) */
    public static Path schemaExtractorScript(Path projectRoot) {
        return projectRoot.resolve(STUDIO_DIR).resolve("extract-schema.mjs");
    }

    /**
     * Deletes empty legacy metadata directories after migration.
     */
    public static void deleteEmptyLegacyDirs(Path projectRoot) throws IOException {
        deleteIfEmpty(legacyScriptSchemasDir(projectRoot));
        deleteIfEmpty(legacyScriptCacheDir(projectRoot));
        deleteIfEmpty(legacyLlwScriptCacheDir(projectRoot));
        deleteIfEmpty(legacyLogsDir(projectRoot));
    }

    private static void deleteIfEmpty(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            if (stream.findAny().isEmpty()) {
                Files.deleteIfExists(dir);
            }
        }
    }
}
