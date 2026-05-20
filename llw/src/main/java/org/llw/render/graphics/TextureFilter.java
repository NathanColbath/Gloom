package org.llw.render.graphics;

import org.lwjgl.opengl.GL11;

/**
 * Minification and magnification filter for {@link Texture2d} sampling.
 */
public enum TextureFilter {
    /** Nearest-neighbor (crisp pixels, no blur between texels). */
    POINT(GL11.GL_NEAREST),
    /** Linear interpolation (smooth scaling). */
    LINEAR(GL11.GL_LINEAR);

    private final int glConstant;

    TextureFilter(int glConstant) {
        this.glConstant = glConstant;
    }

    /** @return OpenGL {@code GL_TEXTURE_*_FILTER} constant */
    public int glConstant() {
        return glConstant;
    }
}
