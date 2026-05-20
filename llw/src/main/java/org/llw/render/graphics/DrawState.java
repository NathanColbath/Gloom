package org.llw.render.graphics;

import org.llw.math.matrix.Matrix3x2;

/**
 * Immutable per-draw rendering state applied when a {@link Renderable} is flushed from a
 * {@link RenderTarget} queue.
 *
 * <p>Use the {@code with*} methods to derive modified copies. {@link #DEFAULT} provides
 * alpha blending, no texture or shader override, identity transform, and layer {@code 0}.
 *
 * @param blendMode blending equation for this draw
 * @param texture   optional texture override; {@code null} uses the renderable's texture
 * @param shader    optional shader override; {@code null} uses the backend default
 * @param transform optional world transform multiplied with vertex positions
 * @param layer     draw order layer; lower layers are drawn first
 */
public record DrawState(
        BlendMode blendMode,
        Texture2d texture,
        ShaderProgram shader,
        Matrix3x2 transform,
        int layer
) {
    /**
     * Default draw state: {@link BlendMode#ALPHA}, no texture or shader override, identity
     * transform, layer {@code 0}.
     */
    public static final DrawState DEFAULT = new DrawState(BlendMode.ALPHA, null, null, new Matrix3x2().identity(), 0);

    /**
     * Returns a copy of this state with the given blend mode.
     *
     * @param blendMode new blending mode
     * @return updated {@link DrawState}
     */
    public DrawState withBlendMode(BlendMode blendMode) {
        return new DrawState(blendMode, texture, shader, transform, layer);
    }

    /**
     * Returns a copy of this state with the given texture override.
     *
     * @param texture texture bound for this draw, or {@code null} for default
     * @return updated {@link DrawState}
     */
    public DrawState withTexture(Texture2d texture) {
        return new DrawState(blendMode, texture, shader, transform, layer);
    }

    /**
     * Returns a copy of this state with the given shader override.
     *
     * @param shader program used for this draw, or {@code null} for backend default
     * @return updated {@link DrawState}
     */
    public DrawState withShader(ShaderProgram shader) {
        return new DrawState(blendMode, texture, shader, transform, layer);
    }

    /**
     * Returns a copy of this state with the given transform matrix.
     *
     * @param transform model matrix applied to vertices (Y-down world space)
     * @return updated {@link DrawState}
     */
    public DrawState withTransform(Matrix3x2 transform) {
        return new DrawState(blendMode, texture, shader, transform, layer);
    }

    /**
     * Returns a copy of this state with the given draw layer.
     *
     * @param layer sort layer; lower values are drawn before higher values
     * @return updated {@link DrawState}
     */
    public DrawState withLayer(int layer) {
        return new DrawState(blendMode, texture, shader, transform, layer);
    }

    /**
     * Returns a copy whose transform is this state's transform multiplied by {@code local}
     * on the right (local applied first, then parent).
     *
     * @param local additional local transform to prepend
     * @return updated {@link DrawState} with combined transform
     */
    public DrawState combineTransform(Matrix3x2 local) {
        Matrix3x2 combined = new Matrix3x2();
        if (transform != null) {
            combined.set(transform);
        }
        combined.multiply(local);
        return withTransform(combined);
    }

    /**
     * Builds a 64-bit key used to sort queued draws before {@link RenderTarget#flush()}.
     *
     * <p>Ordering prioritizes layer, then submission order, shader, texture, and blend mode.
     *
     * @param submissionOrder zero-based enqueue index within the current frame
     * @return composite sort key for stable batched rendering
     */
    public long sortKey(int submissionOrder) {
        int shaderId = shader == null ? 0 : shader.programId();
        int textureId = texture == null ? 0 : texture.id();
        return ((long) layer << 56)
                | ((long) submissionOrder << 40)
                | ((long) shaderId << 24)
                | ((long) textureId << 8)
                | blendMode.ordinal();
    }
}
