package org.llw.studio.assets;

import org.llw.render.graphics.Texture2d;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-session cache of texture previews resolved through an {@link AssetDatabase}.
 */
public final class AssetPreviewCache {
    private final AssetDatabase database;
    private final Map<String, Texture2d> previews = new HashMap<>();

    /**
     * @param database asset index used to resolve texture GUIDs
     */
    public AssetPreviewCache(AssetDatabase database) {
        this.database = database;
    }

    /**
     * @param guid texture asset GUID
     * @return preview texture, or {@code null} if the GUID is not a texture asset
     */
    public Texture2d preview(String guid) {
        if (guid == null) {
            return null;
        }
        Texture2d cached = previews.get(guid);
        if (cached != null) {
            return cached;
        }
        StudioAsset asset = database.get(guid);
        if (asset == null || asset.type() != AssetType.TEXTURE) {
            return null;
        }
        Texture2d full = database.texture(guid);
        previews.put(guid, full);
        return full;
    }

    /**
     * @param spriteGuid virtual sprite sub-asset GUID
     * @return parent atlas texture for UV cropping, or {@code null}
     */
    public Texture2d textureForSprite(String spriteGuid) {
        if (spriteGuid == null || spriteGuid.isBlank()) {
            return null;
        }
        StudioAsset sprite = database.get(spriteGuid);
        if (sprite == null || sprite.parentTextureGuid() == null) {
            return null;
        }
        return preview(sprite.parentTextureGuid());
    }

    /**
     * @param guid texture GUID to drop from the cache
     */
    public void invalidate(String guid) {
        previews.remove(guid);
    }

    /** Clears all cached previews. */
    public void clear() {
        previews.clear();
    }
}
