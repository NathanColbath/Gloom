package org.llw.studio.assets;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Immutable view of a single indexed project asset (file or folder).
 */
public final class StudioAsset {
    private final String guid;
    private final Path path;
    private final AssetType type;
    private final String displayName;
    private final Instant lastModified;
    private final String parentTextureGuid;
    private final String parentAnimationGuid;

    public StudioAsset(String guid, Path path, AssetType type, String displayName, Instant lastModified) {
        this(guid, path, type, displayName, lastModified, null, null);
    }

    public StudioAsset(
            String guid,
            Path path,
            AssetType type,
            String displayName,
            Instant lastModified,
            String parentTextureGuid
    ) {
        this(guid, path, type, displayName, lastModified, parentTextureGuid, null);
    }

    public StudioAsset(
            String guid,
            Path path,
            AssetType type,
            String displayName,
            Instant lastModified,
            String parentTextureGuid,
            String parentAnimationGuid
    ) {
        this.guid = guid;
        this.path = path;
        this.type = type;
        this.displayName = displayName;
        this.lastModified = lastModified;
        this.parentTextureGuid = parentTextureGuid;
        this.parentAnimationGuid = parentAnimationGuid;
    }

    public String guid() {
        return guid;
    }

    public Path path() {
        return path;
    }

    public AssetType type() {
        return type;
    }

    public String displayName() {
        return displayName;
    }

    public String parentTextureGuid() {
        return parentTextureGuid;
    }

    public String parentAnimationGuid() {
        return parentAnimationGuid;
    }

    public String friendlyDisplayName() {
        return AssetDisplayNames.friendly(this);
    }

    public Instant lastModified() {
        return lastModified;
    }

    public boolean isFolder() {
        return type == AssetType.FOLDER;
    }

    public boolean isSpriteSubAsset() {
        return type == AssetType.SPRITE;
    }

    public boolean isAnimationClipSubAsset() {
        return type == AssetType.ANIMATION_CLIP && parentAnimationGuid != null;
    }
}
