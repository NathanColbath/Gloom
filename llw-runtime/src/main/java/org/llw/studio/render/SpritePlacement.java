package org.llw.studio.render;

import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;
import org.llw.studio.assets.SpriteDefinition;

/**
 * Applies studio sprite conventions: {@code Transform2D} position is the world-space center
 * of the texture, matching {@link EntityBounds}.
 *
 * <p><b>Coordinates (Y-down):</b> {@code centerX}/{@code centerY} are the world pivot;
 * the underlying {@link Sprite} position is offset so the texture center lands on that point.
 */
public final class SpritePlacement {
    private SpritePlacement() {
    }

    /**
     * Centers the sprite on {@code centerX}/{@code centerY} with the given rotation and scale.
     *
     * @param sprite   drawable to configure
     * @param texture  source texture (defines origin from pixel size)
     * @param centerX  world X of the visual center
     * @param centerY  world Y of the visual center
     * @param rotationDegrees studio rotation in degrees (0 = +X, positive = clockwise in Y-down space)
     * @param scaleX          horizontal scale factor
     * @param scaleY          vertical scale factor
     */
    public static void applyCentered(
            Sprite sprite,
            Texture2d texture,
            float centerX,
            float centerY,
            float rotationDegrees,
            float scaleX,
            float scaleY
    ) {
        float originX = texture.size().width() * 0.5f;
        float originY = texture.size().height() * 0.5f;
        sprite.setOrigin(originX, originY);
        // llw transform maps pivot local origin to position + origin; offset position so pivot lands on center.
        sprite.setPosition(centerX - originX, centerY - originY);
        sprite.setRotation(rotationDegreesToRadians(rotationDegrees));
        sprite.setScale(scaleX, scaleY);
    }

    /**
     * Centers the sprite on {@code centerX}/{@code centerY} using slice pixel dimensions for the pivot.
     *
     * @param rotationDegrees studio rotation in degrees (includes texture art-facing offset when applied upstream)
     */
    public static void applyCentered(
            Sprite sprite,
            SpriteDefinition slice,
            float centerX,
            float centerY,
            float rotationDegrees,
            float scaleX,
            float scaleY
    ) {
        float originX = slice.width() * 0.5f;
        float originY = slice.height() * 0.5f;
        sprite.setOrigin(originX, originY);
        sprite.setPosition(centerX - originX, centerY - originY);
        sprite.setRotation(rotationDegreesToRadians(rotationDegrees));
        sprite.setScale(scaleX, scaleY);
    }

    /**
     * @param rotationDegrees studio / gameplay rotation in degrees
     * @return radians for {@link org.llw.math.transform.Transform2f#setRotation(float)}
     */
    public static float rotationDegreesToRadians(float rotationDegrees) {
        return (float) Math.toRadians(rotationDegrees);
    }
}
