package org.llw.studio.editor.assets;

import org.llw.studio.assets.StudioAsset;

import java.nio.file.Path;
import java.util.List;

/**
 * In-memory copy/cut buffer for project asset file operations.
 */
public final class AssetClipboard {
    /** Clipboard operation mode. */
    public enum Mode {
        /** No entries; paste is disabled. */
        NONE,
        /** Duplicate entries on paste. */
        COPY,
        /** Move entries on paste and clear. */
        CUT
    }

    /**
     * One asset path staged for paste.
     *
     * @param guid   asset GUID at copy/cut time
     * @param path   filesystem path
     * @param folder whether the asset is a folder
     */
    public record Entry(String guid, Path path, boolean folder) {
    }

    private Mode mode = Mode.NONE;
    private List<Entry> entries = List.of();

    /** @return current clipboard mode */
    public Mode mode() {
        return mode;
    }

    /** @return staged entries */
    public List<Entry> entries() {
        return entries;
    }

    /** @return true when paste may run */
    public boolean canPaste() {
        return mode != Mode.NONE && !entries.isEmpty();
    }

    /**
     * @param asset asset to copy
     */
    public void copy(StudioAsset asset) {
        if (asset == null) {
            return;
        }
        mode = Mode.COPY;
        entries = List.of(toEntry(asset));
    }

    /**
     * @param asset asset to cut
     */
    public void cut(StudioAsset asset) {
        if (asset == null) {
            return;
        }
        mode = Mode.CUT;
        entries = List.of(toEntry(asset));
    }

    /**
     * Stages a duplicate (copy) of the asset for paste-into-folder.
     *
     * @param asset asset to duplicate
     */
    public void duplicate(StudioAsset asset) {
        copy(asset);
    }

    /** Clears the clipboard. */
    public void clear() {
        mode = Mode.NONE;
        entries = List.of();
    }

    private static Entry toEntry(StudioAsset asset) {
        return new Entry(asset.guid(), asset.path(), asset.isFolder());
    }
}
