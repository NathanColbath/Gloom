package org.llw.render.gl;

import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

/**
 * Tracks bound shader program, texture, and blend mode to avoid redundant GL state changes.
 */
final class GlStateTracker {
    private int boundProgram = -1;
    private int boundTextureUnit = -1;
    private int boundTexture = -1;
    private BlendMode blendMode = BlendMode.ALPHA;

    void useProgram(ShaderProgram program) {
        int id = program.programId();
        if (boundProgram != id) {
            GL20.glUseProgram(id);
            boundProgram = id;
        }
    }

    void bindTexture(int unit, int textureId) {
        if (boundTextureUnit != unit) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            boundTextureUnit = unit;
        }
        if (boundTexture != textureId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            boundTexture = textureId;
        }
    }

    void applyBlendMode(BlendMode mode) {
        if (blendMode == mode) {
            return;
        }
        blendMode = mode;
        switch (mode) {
            case ALPHA -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            case ADDITIVE -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            }
            case MULTIPLY -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ZERO);
            }
            case NONE -> GL11.glDisable(GL11.GL_BLEND);
        }
    }

    void reset() {
        boundProgram = -1;
        boundTextureUnit = -1;
        boundTexture = -1;
        blendMode = null;
    }
}
