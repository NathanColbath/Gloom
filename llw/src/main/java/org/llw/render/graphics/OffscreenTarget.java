package org.llw.render.graphics;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.gl.FramebufferObject;
import org.llw.render.gl.OpenGlBackend;

/**
 * Off-screen {@link RenderTarget} that renders into an OpenGL framebuffer.
 *
 * <p>Useful for render-to-texture workflows (post-processing, UI atlases, screenshots).
 * The camera is initialized to {@code size} with its center at the framebuffer midpoint,
 * using Y-down coordinates.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #clear(Color)}</li>
 *   <li>{@link #draw(Renderable)} / {@link #draw(Renderable, DrawState)}</li>
 *   <li>{@link #flush()} — binds the FBO, submits queued draws, then unbinds</li>
 *   <li>Sample {@link #colorTexture()} or blit the result to another target</li>
 *   <li>{@link #dispose()} when the framebuffer is no longer needed</li>
 * </ol>
 */
public final class OffscreenTarget extends AbstractRenderTarget {
    private final FramebufferObject framebuffer;

    /**
     * Creates an off-screen target of {@code size} using the shared {@code backend}.
     *
     * @param backend OpenGL backend that must already be initialized
     * @param size pixel width and height of the color attachment
     */
    public OffscreenTarget(OpenGlBackend backend, IntSize size) {
        super(backend);
        this.framebuffer = new FramebufferObject(size);
        camera.setSize(size.width(), size.height());
        camera.setCenter(size.width() / 2f, size.height() / 2f);
    }

    /**
     * Returns the RGBA color texture populated by the most recent {@link #flush()}.
     *
     * @return GPU texture holding rendered pixels
     */
    public Texture2d colorTexture() {
        return framebuffer.colorTexture();
    }

    /**
     * Binds the framebuffer, clears its color attachment, then unbinds.
     *
     * @param color fill color in RGBA byte components
     */
    @Override
    public void clear(Color color) {
        framebuffer.bind();
        backend.beginFrame(framebuffer.size());
        backend.setClearColor(color);
        backend.clear();
        framebuffer.unbind();
    }

    /**
     * Returns the pixel dimensions of the framebuffer.
     *
     * @return fixed width and height set at construction
     */
    @Override
    public IntSize getSize() {
        return framebuffer.size();
    }

    /**
     * Binds the framebuffer, flushes all queued draws into it, then unbinds.
     *
     * <p>Must be called before reading {@link #colorTexture()} to ensure draws are
     * committed to the attachment.
     */
    @Override
    public void flush() {
        framebuffer.bind();
        flushQueuedDraws(framebuffer.size());
        framebuffer.unbind();
    }

    /**
     * Releases the framebuffer and its color attachment.
     *
     * <p>The target must not be used after disposal.
     */
    public void dispose() {
        framebuffer.dispose();
    }
}
