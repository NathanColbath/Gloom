package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.SpriteDefinition;

/**
 * Draws a cropped preview of one spritesheet slice using atlas UVs.
 */
public final class SpriteSlicePreview {
    private static final float DEFAULT_MAX_SIZE = 128f;

    private SpriteSlicePreview() {
    }

    /**
     * @param texture parent atlas texture
     * @param slice   sprite definition within the atlas
     * @param maxSize longest edge of the preview quad in pixels
     * @return {@code true} when a preview was drawn
     */
    public static boolean draw(Texture2d texture, SpriteDefinition slice, float maxSize) {
        if (texture == null || slice == null || slice.width() <= 0 || slice.height() <= 0) {
            ImGui.textDisabled("Preview unavailable");
            return false;
        }
        int tw = texture.size().width();
        int th = texture.size().height();
        var uv = slice.uvRect(tw, th);
        float scale = Math.min(maxSize / slice.width(), maxSize / slice.height());
        float displayW = slice.width() * scale;
        float displayH = slice.height() * scale;
        float u0 = uv.left;
        float u1 = uv.left + uv.width;
        float vTop = uv.top + uv.height;
        float vBottom = uv.top;
        ImGui.image(texture.id(), displayW, displayH, u0, vTop, u1, vBottom);
        return true;
    }

    /**
     * @param texture parent atlas texture
     * @param slice   sprite definition within the atlas
     * @return {@code true} when a preview was drawn
     */
    public static boolean draw(Texture2d texture, SpriteDefinition slice) {
        return draw(texture, slice, DEFAULT_MAX_SIZE);
    }

    /**
     * Draws a square sprite slice thumbnail for asset browser cells.
     *
     * @return {@code true} when drawn
     */
    public static boolean drawThumb(AssetDatabase assets, AssetPreviewCache previews, String spriteGuid, float size) {
        if (assets == null || previews == null || spriteGuid == null || spriteGuid.isBlank()) {
            return false;
        }
        Texture2d texture = previews.textureForSprite(spriteGuid);
        SpriteDefinition slice = assets.sprite(spriteGuid);
        if (texture == null || slice == null) {
            return false;
        }
        int tw = texture.size().width();
        int th = texture.size().height();
        var uv = slice.uvRect(tw, th);
        float u0 = uv.left;
        float u1 = uv.left + uv.width;
        float vTop = uv.top + uv.height;
        float vBottom = uv.top;
        ImGui.image(texture.id(), size, size, u0, vTop, u1, vBottom);
        return true;
    }

    /**
     * Draws a square sprite slice thumbnail without creating an ImGui hit target.
     */
    public static boolean drawThumbDrawList(
            imgui.ImDrawList drawList,
            float x,
            float y,
            AssetDatabase assets,
            AssetPreviewCache previews,
            String spriteGuid,
            float size
    ) {
        if (drawList == null || assets == null || previews == null || spriteGuid == null || spriteGuid.isBlank()) {
            return false;
        }
        Texture2d texture = previews.textureForSprite(spriteGuid);
        SpriteDefinition slice = assets.sprite(spriteGuid);
        if (texture == null || slice == null) {
            return false;
        }
        int tw = texture.size().width();
        int th = texture.size().height();
        var uv = slice.uvRect(tw, th);
        float u0 = uv.left;
        float u1 = uv.left + uv.width;
        float vTop = uv.top + uv.height;
        float vBottom = uv.top;
        drawList.addImage(texture.id(), x, y, x + size, y + size, u0, vTop, u1, vBottom);
        return true;
    }
}
