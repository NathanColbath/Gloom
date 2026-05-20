package org.llw.studio.assets;

/**
 * Registry entry linking a clip file to its GUID within a parent animation asset.
 */
public record AnimationClipEntry(String guid, String name, String path) {
    public AnimationClipEntry {
        if (guid == null) {
            guid = "";
        }
        if (name == null) {
            name = "";
        }
        if (path == null) {
            path = "";
        }
    }
}
