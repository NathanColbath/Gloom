package org.llw.studio.assets;

import org.llw.math.geometry.RectF;

/**
 * A rectangular slice of a texture atlas, referenced by {@link org.llw.studio.ecs.components.SpriteRendererComponent}.
 */
public record SpriteDefinition(
        String guid,
        String name,
        String textureGuid,
        int x,
        int y,
        int width,
        int height,
        float pivotX,
        float pivotY,
        int atlasWidth,
        int atlasHeight
) {
    /**
     * @return normalized UV rectangle using stored atlas dimensions from import time
     */
    public RectF uvRect() {
        return uvRect(atlasWidth, atlasHeight);
    }

    /**
     * @return normalized UV rectangle for {@link org.llw.render.renderables.Sprite#setTextureRect}
     *         using the active GPU texture size (preferred at draw time)
     */
    public RectF uvRect(int textureWidth, int textureHeight) {
        if (textureWidth <= 0 || textureHeight <= 0) {
            return new RectF(0f, 0f, 1f, 1f);
        }
        float u0 = (x + 0.5f) / textureWidth;
        float u1 = (x + width - 0.5f) / textureWidth;
        // Pixel Y is top-down in atlas space; GPU atlas is STB-flipped (larger V = image top).
        float vAtlasTop = 1f - (y + 0.5f) / textureHeight;
        float vAtlasBottom = 1f - (y + height - 0.5f) / textureHeight;
        return new RectF(u0, vAtlasBottom, Math.max(0f, u1 - u0), Math.max(0f, vAtlasTop - vAtlasBottom));
    }

    /**
     * @return unique key for matching slices across re-imports
     */
    public String rectKey() {
        return name + ":" + x + "," + y + "," + width + "," + height;
    }
}
