package org.llw.render.graphics;

import org.lwjgl.opengl.GL12;

/**
 * Edge wrapping mode for {@link Texture2d} sampling outside {@code [0, 1]}.
 */
public enum TextureWrap {
    /** Clamp UVs to the edge texel. */
    CLAMP(GL12.GL_CLAMP_TO_EDGE),
    /** Repeat the texture. */
    REPEAT(GL12.GL_REPEAT);

    private final int glConstant;

    TextureWrap(int glConstant) {
        this.glConstant = glConstant;
    }

    /** @return OpenGL {@code GL_TEXTURE_WRAP_*} constant */
    public int glConstant() {
        return glConstant;
    }
}
