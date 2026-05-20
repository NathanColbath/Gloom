package org.llw.render.graphics;

/**
 * Blending modes applied per draw via {@link DrawState#blendMode()}.
 *
 * <p>Corresponds to SFML-style blend equations used when flushing the draw queue.
 */
public enum BlendMode {
    /**
     * Standard alpha blending: {@code src * alpha + dst * (1 - alpha)}.
     */
    ALPHA,

    /**
     * Additive blending: source color is added to the destination.
     */
    ADDITIVE,

    /**
     * Multiplicative blending: source and destination colors are multiplied.
     */
    MULTIPLY,

    /**
     * No blending; source fragments replace the destination.
     */
    NONE
}
