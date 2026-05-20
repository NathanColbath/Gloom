package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Filesystem copy, move, and delete for project assets and centralized metadata.
 */
public final class AssetFileOperations {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AssetFileOperations() {
    }

    /**
     * Outcome of a copy, move, or delete operation.
     *
     * @param resultGuids GUIDs of assets created or moved; empty when deleting or on failure
     * @param errorMessage failure description, or {@code null} on success
     */
    public record OperationResult(List<String> resultGuids, String errorMessage) {
        public static OperationResult ok(List<String> guids) {
            return new OperationResult(List.copyOf(guids), null);
        }

        public static OperationResult fail(String message) {
            return new OperationResult(List.of(), message);
        }

        public boolean success() {
            return errorMessage == null;
        }
    }

    public static OperationResult copyInto(Path source, Path targetFolder, Path assetsRoot) {
        return copyInto(source, targetFolder, projectRootFromAssets(assetsRoot), assetsRoot);
    }

    public static OperationResult rename(Path source, String newFileName, Path projectRoot, Path assetsRoot) {
        try {
            if (source == null || newFileName == null || newFileName.isBlank()) {
                return OperationResult.fail("Invalid rename");
            }
            if (isAssetsRoot(source, assetsRoot)) {
                return OperationResult.fail("Cannot rename the Assets root folder");
            }
            Path destination = source.getParent().resolve(newFileName);
            if (Files.exists(destination)) {
                return OperationResult.fail("An asset with that name already exists");
            }
            String guid = movePath(source, destination, projectRoot, assetsRoot);
            return OperationResult.ok(List.of(guid));
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    public static OperationResult moveInto(Path source, Path targetFolder, Path assetsRoot) {
        return moveInto(source, targetFolder, projectRootFromAssets(assetsRoot), assetsRoot);
    }

    public static OperationResult delete(Path source, Path assetsRoot) {
        return delete(source, projectRootFromAssets(assetsRoot), assetsRoot);
    }

    /**
     * Creates an empty folder with metadata under {@code parentFolder}.
     *
     * @param parentFolder destination parent directory under {@code Assets}
     * @param folderName   new folder name (not a path)
     */
    public static OperationResult createFolder(
            Path parentFolder,
            String folderName,
            Path projectRoot,
            Path assetsRoot
    ) {
        try {
            validateTargetFolder(parentFolder, assetsRoot);
            String sanitized = sanitizeFolderName(folderName);
            if (sanitized.isBlank()) {
                return OperationResult.fail("Invalid folder name");
            }
            Path destination = uniqueDestination(parentFolder, sanitized);
            Files.createDirectories(destination);
            writeFreshMeta(destination, projectRoot, assetsRoot);
            return OperationResult.ok(List.of(MetaFile.read(projectRoot, assetsRoot, destination).guid));
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    /**
     * Copies files or folders from outside the project (for example OS drag-and-drop) into {@code targetFolder}.
     * Always assigns new GUIDs; never reads metadata from {@code source} paths outside {@code assetsRoot}.
     */
    public static OperationResult importExternal(
            Path source,
            Path targetFolder,
            Path projectRoot,
            Path assetsRoot
    ) {
        try {
            validateTargetFolder(targetFolder, assetsRoot);
            if (!Files.exists(source)) {
                return OperationResult.fail("Source does not exist: " + source);
            }
            if (shouldSkipExternalEntry(source)) {
                return OperationResult.ok(List.of());
            }
            List<String> guids = new ArrayList<>();
            if (Files.isDirectory(source)) {
                guids.add(importExternalFolder(source, targetFolder, projectRoot, assetsRoot));
            } else {
                guids.add(importExternalFile(source, targetFolder, projectRoot, assetsRoot));
            }
            return OperationResult.ok(guids);
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    public static OperationResult copyInto(Path source, Path targetFolder, Path projectRoot, Path assetsRoot) {
        try {
            validateTargetFolder(targetFolder, assetsRoot);
            if (isAssetsRoot(source, assetsRoot)) {
                return OperationResult.fail("Cannot copy the Assets root folder");
            }
            List<String> guids = new ArrayList<>();
            if (Files.isDirectory(source)) {
                String folderName = source.getFileName().toString();
                Path destFolder = uniqueDestination(targetFolder, folderName);
                guids.add(copyFolderRecursive(source, destFolder, projectRoot, assetsRoot));
            } else {
                guids.add(copyFile(source, targetFolder, projectRoot, assetsRoot));
            }
            return OperationResult.ok(guids);
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    public static OperationResult moveInto(Path source, Path targetFolder, Path projectRoot, Path assetsRoot) {
        try {
            validateMove(source, targetFolder, assetsRoot);
            Path destination = targetFolder.resolve(source.getFileName());
            if (Files.exists(destination)) {
                return OperationResult.fail("Asset already exists in target folder: " + destination.getFileName());
            }
            String guid = movePath(source, destination, projectRoot, assetsRoot);
            return OperationResult.ok(List.of(guid));
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    public static OperationResult delete(Path source, Path projectRoot, Path assetsRoot) {
        try {
            if (isAssetsRoot(source, assetsRoot)) {
                return OperationResult.fail("Cannot delete the Assets root folder");
            }
            if (!source.normalize().startsWith(assetsRoot.normalize())) {
                return OperationResult.fail("Path must stay within Assets/");
            }
            if (Files.isDirectory(source)) {
                deleteFolderRecursive(source, projectRoot, assetsRoot);
            } else {
                deleteFileWithMeta(source, projectRoot, assetsRoot);
            }
            return OperationResult.ok(List.of());
        } catch (IOException ex) {
            return OperationResult.fail(ex.getMessage());
        }
    }

    private static String importExternalFile(
            Path sourceFile,
            Path targetFolder,
            Path projectRoot,
            Path assetsRoot
    ) throws IOException {
        Path destination = uniqueDestination(targetFolder, sourceFile.getFileName().toString());
        Files.copy(sourceFile, destination);
        writeFreshMeta(destination, projectRoot, assetsRoot);
        return MetaFile.read(projectRoot, assetsRoot, destination).guid;
    }

    private static String importExternalFolder(
            Path sourceFolder,
            Path targetFolder,
            Path projectRoot,
            Path assetsRoot
    ) throws IOException {
        Path destFolder = uniqueDestination(targetFolder, sourceFolder.getFileName().toString());
        Files.createDirectories(destFolder);
        writeFreshMeta(destFolder, projectRoot, assetsRoot);
        try (Stream<Path> stream = Files.list(sourceFolder)) {
            List<Path> entries = stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();
            for (Path entry : entries) {
                if (shouldSkipExternalEntry(entry)) {
                    continue;
                }
                if (Files.isDirectory(entry)) {
                    importExternalFolder(entry, destFolder, projectRoot, assetsRoot);
                } else {
                    importExternalFile(entry, destFolder, projectRoot, assetsRoot);
                }
            }
        }
        return MetaFile.read(projectRoot, assetsRoot, destFolder).guid;
    }

    private static void writeFreshMeta(Path destination, Path projectRoot, Path assetsRoot) throws IOException {
        MetaFile.MetaData meta = new MetaFile.MetaData();
        meta.guid = Guid.newGuid();
        meta.type = MetaFile.inferType(destination).name();
        meta.importer = MAPPER.createObjectNode();
        MetaFile.write(projectRoot, assetsRoot, destination, meta);
    }

    private static boolean shouldSkipExternalEntry(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return true;
        }
        String name = fileName.toString();
        if (name.isEmpty() || name.startsWith(".")) {
            return true;
        }
        if (name.endsWith(".meta")) {
            return true;
        }
        return name.equalsIgnoreCase("Thumbs.db") || name.equalsIgnoreCase("desktop.ini");
    }

    private static String copyFile(Path sourceFile, Path targetFolder, Path projectRoot, Path assetsRoot)
            throws IOException {
        Path destination = uniqueDestination(targetFolder, sourceFile.getFileName().toString());
        Files.copy(sourceFile, destination);
        writeCopiedMeta(sourceFile, destination, projectRoot, assetsRoot);
        return MetaFile.read(projectRoot, assetsRoot, destination).guid;
    }

    private static String copyFolderRecursive(
            Path sourceFolder, Path destFolder, Path projectRoot, Path assetsRoot) throws IOException {
        Files.createDirectories(destFolder);
        MetaFile.MetaData sourceMeta = MetaFile.read(projectRoot, assetsRoot, sourceFolder);
        MetaFile.MetaData destMeta = new MetaFile.MetaData();
        destMeta.guid = Guid.newGuid();
        destMeta.type = sourceMeta.type;
        destMeta.importer = copyImporter(sourceMeta.importer);
        MetaFile.write(projectRoot, assetsRoot, destFolder, destMeta);

        try (Stream<Path> stream = Files.list(sourceFolder)) {
            List<Path> entries = stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();
            for (Path entry : entries) {
                if (entry.getFileName().toString().endsWith(".meta")) {
                    continue;
                }
                if (Files.isDirectory(entry)) {
                    Path childDest = destFolder.resolve(entry.getFileName());
                    copyFolderRecursive(entry, childDest, projectRoot, assetsRoot);
                } else {
                    copyFile(entry, destFolder, projectRoot, assetsRoot);
                }
            }
        }
        return destMeta.guid;
    }

    private static void writeCopiedMeta(
            Path sourceFile, Path destination, Path projectRoot, Path assetsRoot) throws IOException {
        MetaFile.MetaData sourceMeta = MetaFile.read(projectRoot, assetsRoot, sourceFile);
        MetaFile.MetaData destMeta = new MetaFile.MetaData();
        destMeta.guid = Guid.newGuid();
        destMeta.type = sourceMeta.type;
        destMeta.importer = copyImporter(sourceMeta.importer);
        MetaFile.write(projectRoot, assetsRoot, destination, destMeta);
    }

    private static ObjectNode copyImporter(ObjectNode importer) {
        if (importer == null) {
            return MAPPER.createObjectNode();
        }
        return importer.deepCopy();
    }

    private static String movePath(Path source, Path destination, Path projectRoot, Path assetsRoot)
            throws IOException {
        Files.createDirectories(destination.getParent());
        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, source);
        Files.move(source, destination);
        relocateMetadataMirror(projectRoot, assetsRoot, source, destination);
        Path legacySource = StudioProjectLayout.legacyAssetsMetaPath(source);
        if (Files.isRegularFile(legacySource)) {
            Files.deleteIfExists(legacySource);
        }
        if (Files.isDirectory(destination)) {
            deleteLegacyMetaUnder(source);
        }
        return meta.guid;
    }

    private static void relocateMetadataMirror(
            Path projectRoot, Path assetsRoot, Path oldAssetPath, Path newAssetPath) throws IOException {
        Path metaRoot = StudioProjectLayout.assetsMetaRoot(projectRoot);
        if (!Files.isDirectory(metaRoot)) {
            return;
        }
        String oldPrefix = assetsRoot.relativize(oldAssetPath.normalize()).toString().replace('\\', '/');
        String newPrefix = assetsRoot.relativize(newAssetPath.normalize()).toString().replace('\\', '/');
        try (Stream<Path> walk = Files.walk(metaRoot)) {
            for (Path metaFile : walk.filter(Files::isRegularFile).toList()) {
                String key = metaRoot.relativize(metaFile).toString().replace('\\', '/');
                if (!key.equals(oldPrefix + ".meta") && !key.startsWith(oldPrefix + "/")) {
                    continue;
                }
                String suffix = key.substring(oldPrefix.length());
                Path dest = metaRoot;
                for (String segment : (newPrefix + suffix).split("/")) {
                    if (!segment.isEmpty()) {
                        dest = dest.resolve(segment);
                    }
                }
                Files.createDirectories(dest.getParent());
                Files.move(metaFile, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void deleteLegacyMetaUnder(Path assetFolder) throws IOException {
        if (!Files.isDirectory(assetFolder)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(assetFolder)) {
            for (Path file : walk.filter(Files::isRegularFile).toList()) {
                if (file.getFileName().toString().endsWith(".meta")) {
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    private static void deleteFileWithMeta(Path file, Path projectRoot, Path assetsRoot) throws IOException {
        Files.deleteIfExists(file);
        MetaFile.deleteMeta(projectRoot, assetsRoot, file);
    }

    private static void deleteFolderRecursive(Path folder, Path projectRoot, Path assetsRoot) throws IOException {
        try (Stream<Path> stream = Files.list(folder)) {
            List<Path> entries = stream.toList();
            for (Path entry : entries) {
                if (entry.getFileName().toString().endsWith(".meta")) {
                    continue;
                }
                if (Files.isDirectory(entry)) {
                    deleteFolderRecursive(entry, projectRoot, assetsRoot);
                } else {
                    deleteFileWithMeta(entry, projectRoot, assetsRoot);
                }
            }
        }
        MetaFile.deleteMeta(projectRoot, assetsRoot, folder);
        Files.deleteIfExists(folder);
    }

    private static Path uniqueDestination(Path targetFolder, String fileName) throws IOException {
        Path candidate = targetFolder.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String base;
        String extension;
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            base = fileName.substring(0, dot);
            extension = fileName.substring(dot);
        } else {
            base = fileName;
            extension = "";
        }
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = targetFolder.resolve(base + " " + suffix + extension);
            suffix++;
        }
        return candidate;
    }

    private static Path projectRootFromAssets(Path assetsRoot) {
        Path parent = assetsRoot.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Assets root has no parent project directory: " + assetsRoot);
        }
        return parent;
    }

    private static void validateTargetFolder(Path targetFolder, Path assetsRoot) throws IOException {
        Path normalizedTarget = targetFolder.normalize();
        Path normalizedRoot = assetsRoot.normalize();
        if (!normalizedTarget.startsWith(normalizedRoot)) {
            throw new IOException("Target folder must stay within Assets/");
        }
        if (!Files.isDirectory(normalizedTarget)) {
            throw new IOException("Target is not a folder: " + targetFolder);
        }
    }

    private static void validateMove(Path source, Path targetFolder, Path assetsRoot) throws IOException {
        validateTargetFolder(targetFolder, assetsRoot);
        Path normSource = source.normalize();
        Path normTarget = targetFolder.normalize();
        Path normRoot = assetsRoot.normalize();
        if (!normSource.startsWith(normRoot)) {
            throw new IOException("Source must stay within Assets/");
        }
        if (normSource.equals(normRoot)) {
            throw new IOException("Cannot move the Assets root folder");
        }
        if (normTarget.startsWith(normSource) && Files.isDirectory(normSource)) {
            throw new IOException("Cannot move a folder into itself or its descendants");
        }
        Path currentParent = normSource.getParent();
        if (currentParent != null && currentParent.equals(normTarget)) {
            throw new IOException("Asset is already in the target folder");
        }
    }

    private static boolean isAssetsRoot(Path path, Path assetsRoot) {
        return path.normalize().equals(assetsRoot.normalize());
    }

    private static String sanitizeFolderName(String value) {
        String name = value.trim().replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        return name.replaceAll("[<>:\"|?*]", "_");
    }
}
