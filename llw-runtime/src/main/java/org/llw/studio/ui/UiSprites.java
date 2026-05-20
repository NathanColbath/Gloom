package org.llw.studio.ui;

import org.llw.render.core.Color;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;

/** Helpers for drawing solid-color UI quads. */
public final class UiSprites {
    private static Texture2d whitePixel;

    private UiSprites() {
    }

    public static Sprite solidRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        Sprite sprite = new Sprite(whiteTexture());
        sprite.setPosition(x, y);
        sprite.setScale(Math.max(1f, width), Math.max(1f, height));
        sprite.setTint(new Color(
                Math.round(r * 255f),
                Math.round(g * 255f),
                Math.round(b * 255f),
                Math.round(a * 255f)
        ));
        return sprite;
    }

    private static Texture2d whiteTexture() {
        if (whitePixel == null) {
            whitePixel = Texture2d.whitePixel();
        }
        return whitePixel;
    }
}
