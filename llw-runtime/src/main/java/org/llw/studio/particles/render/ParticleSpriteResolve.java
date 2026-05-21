package org.llw.studio.particles.render;

import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.particles.model.ParticleSystemDocument;

/**
 * Resolves the sprite used to draw particles for a renderer module.
 */
public final class ParticleSpriteResolve {
    private ParticleSpriteResolve() {
    }

    /**
     * @param assets   asset database
     * @param renderer particle renderer module
     * @return slice to draw, or {@code null} when no texture is available
     */
    public static SpriteDefinition resolve(AssetDatabase assets, ParticleSystemDocument.RendererModule renderer) {
        if (assets == null || renderer == null) {
            return null;
        }
        if (renderer.spriteGuid != null && !renderer.spriteGuid.isBlank()) {
            SpriteDefinition sprite = assets.sprite(renderer.spriteGuid);
            if (sprite != null) {
                return sprite;
            }
        }
        for (StudioAsset asset : assets.allAssets()) {
            if (asset.type() == AssetType.SPRITE && !asset.isFolder()) {
                SpriteDefinition sprite = assets.sprite(asset.guid());
                if (sprite != null) {
                    return sprite;
                }
            }
        }
        for (StudioAsset asset : assets.allAssets()) {
            if (asset.type() != AssetType.TEXTURE || asset.isFolder()) {
                continue;
            }
            String defaultSprite = assets.defaultSpriteGuid(asset.guid());
            if (!defaultSprite.isBlank()) {
                SpriteDefinition sprite = assets.sprite(defaultSprite);
                if (sprite != null) {
                    return sprite;
                }
            }
            Texture2d texture = assets.texture(asset.guid());
            if (texture != null) {
                int w = texture.size().width();
                int h = texture.size().height();
                return new SpriteDefinition(
                        "",
                        "preview-fallback",
                        asset.guid(),
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
}
