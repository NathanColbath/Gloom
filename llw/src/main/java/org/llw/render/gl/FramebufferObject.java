package org.llw.render.gl;

import org.llw.render.core.IntSize;
import org.llw.render.graphics.Texture2d;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL framebuffer with a single RGBA color attachment backed by a {@link Texture2d}.
 *
 * <p>Use {@link #bind()} before rendering and {@link #unbind()} to restore the default framebuffer.
 * The attachment is validated at construction time.
 */
public final class FramebufferObject {
    private static final Logger log = Log.get(Loggers.GL);

    private final int fbo;
    private final Texture2d colorTexture;
    private final IntSize size;

    /**
     * Creates a framebuffer of the given size with one color texture attachment.
     *
     * @param size width and height of the color attachment in pixels
     * @throws IllegalStateException if the framebuffer is incomplete after attachment
     */
    public FramebufferObject(IntSize size) {
        this.size = size;
        fbo = GL30.glGenFramebuffers();
        colorTexture = Texture2d.createEmpty(size);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, org.lwjgl.opengl.GL11.GL_TEXTURE_2D, colorTexture.id(), 0);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            log.error("Framebuffer incomplete: fbo={} size={}x{} status=0x{}", fbo, size.width(), size.height(), Integer.toHexString(status));
            throw new IllegalStateException("Framebuffer incomplete: " + status);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        log.debug("Created framebuffer fbo={} size={}x{} textureId={}", fbo, size.width(), size.height(), colorTexture.id());
    }

    /**
     * Binds this framebuffer as the active render target.
     */
    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
    }

    /**
     * Binds the default framebuffer (screen).
     */
    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Returns the color texture attached to this framebuffer.
     *
     * @return RGBA color attachment
     */
    public Texture2d colorTexture() {
        return colorTexture;
    }

    /**
     * Returns the dimensions of the color attachment.
     *
     * @return framebuffer width and height in pixels
     */
    public IntSize size() {
        return size;
    }

    /**
     * Destroys the color texture and deletes the framebuffer object.
     */
    public void dispose() {
        colorTexture.dispose();
        GL30.glDeleteFramebuffers(fbo);
    }
}
