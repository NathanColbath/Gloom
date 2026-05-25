package org.llw.studio.editor.assets;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetFileOperations;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.animation.AnimationSetActions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy, cut, paste, duplicate, and delete operations for project assets.
 */
public final class AssetEditorActions {
    private final AssetDatabase assets;
    private final AssetClipboard clipboard;

    /**
     * @param assets    project asset database
     * @param clipboard shared clipboard for paste
     */
    public AssetEditorActions(AssetDatabase assets, AssetClipboard clipboard) {
        this.assets = assets;
        this.clipboard = clipboard;
    }

    /** @return clipboard used by paste operations */
    public AssetClipboard clipboard() {
        return clipboard;
    }

    /**
     * @param asset asset to test
     * @return true if the asset is the synthetic project root
     */
    public boolean isRootAsset(StudioAsset asset) {
        if (asset == null) {
            return true;
        }
        return asset.guid().equals(assets.rootGuid());
    }

    /** @param asset asset to copy */
    public void copy(StudioAsset asset) {
        if (asset != null && !isRootAsset(asset)) {
            clipboard.copy(asset);
        }
    }

    /** @param asset asset to cut */
    public void cut(StudioAsset asset) {
        if (asset != null && !isRootAsset(asset)) {
            clipboard.cut(asset);
        }
    }

    /** Copies {@link AssetDatabase#selected()}. */
    public void copySelected() {
        copy(assets.selected());
    }

    /** Cuts {@link AssetDatabase#selected()}. */
    public void cutSelected() {
        cut(assets.selected());
    }

    /**
     * Pastes clipboard entries into a folder asset.
     *
     * @param folderGuid GUID of the destination folder
     * @return GUIDs of created or moved assets
     */
    public List<String> pasteIntoFolder(String folderGuid) {
        StudioAsset folder = assets.get(folderGuid);
        if (folder == null || !folder.isFolder() || !clipboard.canPaste()) {
            return List.of();
        }
        Path targetFolder = folder.path();
        List<String> resultGuids = new ArrayList<>();
        AssetClipboard.Mode mode = clipboard.mode();
        for (AssetClipboard.Entry entry : clipboard.entries()) {
            AssetFileOperations.OperationResult result = switch (mode) {
                case COPY -> AssetFileOperations.copyInto(entry.path(), targetFolder, assets.assetsRoot());
                case CUT -> AssetFileOperations.moveInto(entry.path(), targetFolder, assets.assetsRoot());
                default -> AssetFileOperations.OperationResult.fail("Clipboard is empty");
            };
            if (!result.success()) {
                break;
            }
            resultGuids.addAll(result.resultGuids());
        }
        if (mode == AssetClipboard.Mode.CUT) {
            clipboard.clear(); // Cut is single-use; second paste must not re-move the same files.
        }
        if (!resultGuids.isEmpty()) {
            assets.refresh();
            assets.select(resultGuids.get(resultGuids.size() - 1));
            assets.showInfo(resultGuids.get(resultGuids.size() - 1));
        }
        return resultGuids;
    }

    /** @return true if {@link AssetDatabase#selected()} was deleted */
    public boolean deleteSelected() {
        return delete(assets.selected());
    }

    /**
     * Duplicates an asset alongside its source file.
     *
     * @param asset asset to duplicate
     * @return GUIDs of new assets
     */
    public List<String> duplicate(StudioAsset asset) {
        if (asset == null || isRootAsset(asset)) {
            return List.of();
        }
        Path parent = asset.path().getParent();
        if (parent == null) {
            return List.of();
        }
        AssetFileOperations.OperationResult result =
                AssetFileOperations.copyInto(asset.path(), parent, assets.assetsRoot());
        if (!result.success()) {
            return List.of();
        }
        assets.refresh();
        if (!result.resultGuids().isEmpty()) {
            String guid = result.resultGuids().get(result.resultGuids().size() - 1);
            assets.select(guid);
            assets.showInfo(guid);
        }
        return result.resultGuids();
    }

    /** @return GUIDs from duplicating {@link AssetDatabase#selected()} */
    public List<String> duplicateSelected() {
        return duplicate(assets.selected());
    }

    /**
     * Deletes an asset from disk and refreshes the database.
     *
     * @param asset asset to delete
     * @return true on success
     */
    public boolean delete(StudioAsset asset) {
        if (asset == null || isRootAsset(asset)) {
            return false;
        }
        AssetFileOperations.OperationResult result =
                AssetFileOperations.delete(asset.path(), assets.assetsRoot());
        if (!result.success()) {
            return false;
        }
        assets.refresh();
        assets.clearSelection();
        assets.clearInfo();
        return true;
    }

    /**
     * @param assetGuid   asset to move
     * @param targetFolder destination directory
     * @return true on success
     */
    public boolean moveToFolder(String assetGuid, Path targetFolder) {
        return assets.moveAsset(assetGuid, targetFolder);
    }

    /**
     * Creates a folder under {@code parentFolderGuid}.
     *
     * @return GUID of the new folder, or {@code null} on failure
     */
    public String createFolder(String parentFolderGuid, String folderName) {
        StudioAsset parent = assets.get(parentFolderGuid);
        if (parent == null || !parent.isFolder() || folderName == null || folderName.isBlank()) {
            return null;
        }
        AssetFileOperations.OperationResult result = AssetFileOperations.createFolder(
                parent.path(),
                folderName,
                assets.projectRoot(),
                assets.assetsRoot()
        );
        if (!result.success() || result.resultGuids().isEmpty()) {
            return null;
        }
        String guid = result.resultGuids().get(0);
        assets.refresh();
        assets.select(guid);
        assets.showInfo(guid);
        return guid;
    }

    /**
     * Renames an asset file and updates animation state names when applicable.
     *
     * @param asset   asset to rename
     * @param newStem new base name without type-specific extensions
     * @return {@code true} on success
     */
    public boolean rename(StudioAsset asset, String newStem) {
        if (asset == null || isRootAsset(asset) || newStem == null || newStem.isBlank()) {
            return false;
        }
        String fileName = fileNameForRename(asset, newStem);
        if (fileName == null) {
            return false;
        }
        if (asset.type() == AssetType.ANIMATION_CLIP && asset.parentAnimationGuid() != null) {
            try {
                // Rename clip file and animation-set state entry together so the controller stays wired.
                AnimationSetActions.renameState(
                        assets,
                        asset.parentAnimationGuid(),
                        asset.guid(),
                        newStem,
                        fileName
                );
            } catch (Exception ignored) {
            }
        }
        AssetFileOperations.OperationResult result = AssetFileOperations.rename(
                asset.path(),
                fileName,
                assets.projectRoot(),
                assets.assetsRoot()
        );
        if (!result.success()) {
            return false;
        }
        assets.refresh();
        if (!result.resultGuids().isEmpty()) {
            assets.select(result.resultGuids().get(0));
        }
        return true;
    }

    private static String fileNameForRename(StudioAsset asset, String newStem) {
        String stem = sanitizeStem(newStem);
        if (stem.isBlank()) {
            return null;
        }
        return switch (asset.type()) {
            case ANIMATION -> stem + ".animation.json";
            case ANIMATION_CLIP -> stem + ".anim.json";
            case PARTICLE_SYSTEM -> stem + ".particle.json";
            case SCENE -> stem.endsWith(".scene") ? stem : stem + ".scene.json";
            case PREFAB -> {
                if (stem.endsWith(".prefab.json")) {
                    yield stem;
                }
                if (stem.endsWith(".prefab")) {
                    yield stem + ".json";
                }
                yield stem + ".prefab.json";
            }
            case SCRIPT -> stem.endsWith(".js") ? stem : stem + ".js";
            default -> {
                String current = asset.displayName();
                int dot = current.lastIndexOf('.');
                yield dot > 0 ? stem + current.substring(dot) : stem;
            }
        };
    }

    private static String sanitizeStem(String value) {
        String stem = value.trim().replace('\\', '/');
        int slash = stem.lastIndexOf('/');
        if (slash >= 0) {
            stem = stem.substring(slash + 1);
        }
        return stem.replaceAll("[<>:\"|?*]", "_");
    }

    /**
     * Imports paths dropped from the OS file manager into a project folder.
     *
     * @param sources    absolute paths from {@link org.llw.render.window.Window#takeDroppedPaths()}
     * @param folderGuid destination folder asset GUID
     * @return GUIDs of imported top-level assets
     */
    public List<String> importExternalFiles(List<Path> sources, String folderGuid) {
        StudioAsset folder = assets.get(folderGuid);
        if (folder == null || !folder.isFolder() || sources == null || sources.isEmpty()) {
            return List.of();
        }
        Path targetFolder = folder.path();
        Path assetsRoot = assets.assetsRoot();
        Path projectRoot = assets.projectRoot();
        List<String> resultGuids = new ArrayList<>();
        for (Path source : sources) {
            if (source == null) {
                continue;
            }
            Path normalized = source.toAbsolutePath().normalize();
            if (normalized.startsWith(assetsRoot.normalize())) {
                // In-project drop is a copy; external OS paths use the import pipeline with type detection.
                AssetFileOperations.OperationResult result =
                        AssetFileOperations.copyInto(normalized, targetFolder, projectRoot, assetsRoot);
                if (result.success()) {
                    resultGuids.addAll(result.resultGuids());
                }
                continue;
            }
            AssetFileOperations.OperationResult result =
                    AssetFileOperations.importExternal(normalized, targetFolder, projectRoot, assetsRoot);
            if (result.success()) {
                resultGuids.addAll(result.resultGuids());
            }
        }
        if (!resultGuids.isEmpty()) {
            assets.refresh();
            String last = resultGuids.get(resultGuids.size() - 1);
            assets.select(last);
            assets.showInfo(last);
        }
        return resultGuids;
    }
}
