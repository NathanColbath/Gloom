package org.llw.render.bgfx;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.OpenGlBackend;
import org.lwjgl.bgfx.BGFX;
import org.lwjgl.opengl.GL30;

/**
 * Fullscreen OpenGL blit of an offscreen texture, then bgfx frame presentation.
 */
public final class BgfxPresentPass {
    private static final Camera2d BLIT_CAMERA = new Camera2d();
    private static final Matrix3x2 BLIT_IDENTITY = new Matrix3x2().identity();

    private BgfxPresentPass() {
    }

    public static void present(OpenGlBackend openGl, Texture2d texture, IntSize viewport, boolean bgfxInitialized) {
        if (texture == null) {
            return;
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        openGl.beginFrame(viewport);
        BLIT_CAMERA.setCenter(viewport.width() * 0.5f, viewport.height() * 0.5f);
        BLIT_CAMERA.setSize(viewport.width(), viewport.height());
        openGl.setViewProjection(BLIT_CAMERA.getViewProjection(viewport));
        openGl.setClearColor(Color.BLACK);
        openGl.clear();
        openGl.drawTexturedQuad(
                BLIT_IDENTITY,
                0f,
                0f,
                viewport.width(),
                viewport.height(),
                0f,
                1f,
                1f,
                0f,
                Color.WHITE,
                texture,
                null,
                BlendMode.ALPHA
        );
        openGl.flushSprites();
        if (bgfxInitialized) {
            BGFX.bgfx_frame(false);
        }
    }
}
