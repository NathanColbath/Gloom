package org.llw.studio.render;

import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.ecs.components.SpriteRendererComponent;

/**
 * Resolves {@link SpriteRendererComponent} references to {@link SpriteDefinition} slices.
 */
public final class SpriteResolve {
    private SpriteResolve() {
    }

    /**
     * @param assets   asset database for sprite and texture lookup
     * @param renderer sprite renderer component
     * @return slice to draw, or {@code null} when unresolved
     */
    public static SpriteDefinition resolve(AssetDatabase assets, SpriteRendererComponent renderer) {
        if (assets == null || renderer == null) {
            return null;
        }
        if (renderer.spriteGuid != null && !renderer.spriteGuid.isBlank()) {
            SpriteDefinition sprite = assets.sprite(renderer.spriteGuid);
            if (sprite != null) {
                return sprite;
            }
        }
        if (renderer.textureGuid != null && !renderer.textureGuid.isBlank()) {
            String defaultSprite = assets.defaultSpriteGuid(renderer.textureGuid);
            if (!defaultSprite.isBlank()) {
                renderer.spriteGuid = defaultSprite;
                return assets.sprite(defaultSprite);
            }
            Texture2d texture = assets.texture(renderer.textureGuid);
            if (texture != null) {
                int w = texture.size().width();
                int h = texture.size().height();
                return new SpriteDefinition(
                        "",
                        "legacy",
                        renderer.textureGuid,
                        0,
                        0,
                        w,
                        h,
                        0.5f,
                        0.5f,
                        w,
                        h
                );
            }
        }
        return null;
    }

    /**
     * @param sprite resolved slice
     * @return draw width in studio pixels
     */
    public static float pixelWidth(SpriteDefinition sprite) {
        return sprite == null ? 0f : sprite.width();
    }

    /**
     * @param sprite resolved slice
     * @return draw height in studio pixels
     */
    public static float pixelHeight(SpriteDefinition sprite) {
        return sprite == null ? 0f : sprite.height();
    }
}
