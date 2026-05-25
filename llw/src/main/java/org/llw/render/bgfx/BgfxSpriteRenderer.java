package org.llw.render.bgfx;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.ShaderLibrary;
import org.lwjgl.bgfx.BGFX;

/**
 * Optional bgfx-native sprite path; fullscreen presentation falls back to OpenGL blit upstream.
 */
final class BgfxSpriteRenderer {
    private boolean hasPending;

    void initialize(ShaderLibrary library) {
        hasPending = false;
    }

    void drawTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode,
            IntSize viewport
    ) {
        hasPending = true;
    }

    boolean hasPending() {
        return hasPending;
    }

    void flush(IntSize viewport) {
        hasPending = false;
    }

    void presentFullscreen(Texture2d texture, IntSize viewport) {
        if (texture == null) {
            return;
        }
        BGFX.bgfx_touch(0);
    }

    void dispose() {
        hasPending = false;
    }
}
