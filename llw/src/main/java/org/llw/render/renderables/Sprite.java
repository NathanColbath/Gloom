package org.llw.render.renderables;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.math.geometry.RectF;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.OpenGlBackend;

/**
 * Textured quad {@link Renderable} with optional tint and normalized texture coordinates.
 *
 * <p>The quad is drawn in local space from {@code (0, 0)} to
 * {@code (textureRect.width * textureWidth, textureRect.height * textureHeight)}. If
 * {@link DrawState#texture()} is set, that texture and its dimensions are used instead of
 * this sprite's own texture.
 */
public final class Sprite extends AbstractTransformable implements Renderable {
    private Texture2d texture;
    private final RectF textureRect = new RectF(0f, 0f, 1f, 1f);
    private Color tint = Color.WHITE;

    /**
     * Creates a sprite that draws the given texture with a full-atlas UV rectangle and white tint.
     *
     * @param texture source texture; may be {@code null}, in which case {@link #render} is a no-op
     */
    public Sprite(Texture2d texture) {
        this.texture = texture;
    }

    /**
     * Returns the texture drawn when no texture override is present in {@link DrawState}.
     *
     * @return the sprite's texture, or {@code null} if none is set
     */
    public Texture2d getTexture() {
        return texture;
    }

    /**
     * Assigns the texture drawn when no texture override is present in {@link DrawState}.
     *
     * @param texture new source texture; may be {@code null}
     */
    public void setTexture(Texture2d texture) {
        this.texture = texture;
    }

    /**
     * Returns a copy of the normalized UV rectangle within the active texture.
     *
     * @return texture coordinates as left, top, width, height in the range {@code [0, 1]}
     */
    public RectF getTextureRect() {
        return textureRect.copy();
    }

    /**
     * Sets the normalized UV rectangle used when sampling the active texture.
     *
     * @param rect UV bounds as left, top, width, height in the range {@code [0, 1]}; must not be {@code null}
     */
    public void setTextureRect(RectF rect) {
        textureRect.left = rect.left;
        textureRect.top = rect.top;
        textureRect.width = rect.width;
        textureRect.height = rect.height;
    }

    /**
     * Returns the per-vertex color multiplier applied when drawing the quad.
     *
     * @return tint color in RGBA byte components
     */
    public Color getTint() {
        return tint;
    }

    /**
     * Sets the per-vertex color multiplier applied when drawing the quad.
     *
     * @param tint tint color in RGBA byte components; must not be {@code null}
     */
    public void setTint(Color tint) {
        this.tint = tint;
    }

    /**
     * Submits a textured quad to the backend, combining this sprite's transform with any
     * transform in {@code state}.
     *
     * <p>Does nothing if no texture is available (both this sprite's texture and
     * {@link DrawState#texture()} are {@code null}).
     *
     * @param backend OpenGL backend that batches and flushes sprite geometry
     * @param state per-draw blend mode, shader, optional texture override, and parent transform
     */
    @Override
    public void render(OpenGlBackend backend, DrawState state) {
        if (texture == null) {
            return;
        }

        Matrix3x2 model = combineModel(state);
        Texture2d activeTexture = state.texture() != null ? state.texture() : texture;
        float width = textureRect.width * activeTexture.size().width();
        float height = textureRect.height * activeTexture.size().height();

        float u0 = textureRect.left;
        float u1 = textureRect.left + textureRect.width;
        // After STB vertical flip on load, atlas top is at larger V; map screen-top (y0) there.
        float vAtlasTop = textureRect.top + textureRect.height;
        float vAtlasBottom = textureRect.top;
        backend.drawTexturedQuad(
                model,
                0f, 0f, width, height,
                u0, vAtlasTop,
                u1, vAtlasBottom,
                tint,
                activeTexture,
                state.shader(),
                state.blendMode()
        );
    }

    private Matrix3x2 combineModel(DrawState state) {
        Matrix3x2 model = getTransform();
        if (state.transform() != null) {
            Matrix3x2 combined = state.transform().copy();
            combined.multiply(model);
            return combined;
        }
        return model;
    }
}
