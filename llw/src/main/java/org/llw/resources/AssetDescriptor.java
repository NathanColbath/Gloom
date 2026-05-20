package org.llw.resources;

/**
 * Immutable registration record for a managed asset.
 *
 * @param id         logical asset id
 * @param type       asset kind
 * @param source     byte source
 * @param fontSize   raster height when {@code type} is {@link AssetType#FONT}
 */
public record AssetDescriptor(String id, AssetType type, AssetSource source, int fontSize) {
    public AssetDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must be non-empty");
        }
        if (type == AssetType.FONT && fontSize <= 0) {
            throw new IllegalArgumentException("fontSize required for fonts");
        }
    }
}
