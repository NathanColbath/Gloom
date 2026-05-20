package org.llw.studio.render;

import org.llw.render.renderables.Sprite;
import org.llw.studio.assets.SpriteDefinition;

/**
 * Positions tile sprites with top-left cell anchor (Y-down world).
 */
public final class TilePlacement {
    private TilePlacement() {
    }

    /**
     * Places a tile quad with its top-left at {@code topLeftX}/{@code topLeftY}.
     */
    public static void applyTopLeft(
            Sprite sprite,
            SpriteDefinition slice,
            float topLeftX,
            float topLeftY,
            float scaleX,
            float scaleY,
            byte flags
    ) {
        float w = slice.width() * Math.abs(scaleX);
        float h = slice.height() * Math.abs(scaleY);
        sprite.setOrigin(0f, 0f);
        sprite.setPosition(topLeftX, topLeftY);
        sprite.setScale(scaleX, scaleY);
        applyFlipRotation(sprite, flags);
    }

    private static void applyFlipRotation(Sprite sprite, byte flags) {
        boolean flipX = (flags & 1) != 0;
        boolean flipY = (flags & 2) != 0;
        boolean rotate90 = (flags & 4) != 0;
        float sx = sprite.getScale().x;
        float sy = sprite.getScale().y;
        if (flipX) {
            sx = -Math.abs(sx);
        }
        if (flipY) {
            sy = -Math.abs(sy);
        }
        sprite.setScale(sx, sy);
        if (rotate90) {
            sprite.setRotation(sprite.getRotation() + (float) (Math.PI * 0.5));
        }
    }
}
