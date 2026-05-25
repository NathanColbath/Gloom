package org.llw.render.graphics;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.backend.BackendInitOptions;
import org.llw.render.backend.RenderBackend;
import org.llw.render.backend.RenderBackendFactory;
import org.llw.render.window.Window;
import org.llw.util.log.FrameDiagnostics;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * On-screen {@link RenderTarget} backed by a {@link Window} and {@link OpenGlBackend}.
 *
 * <p>Created once at application startup. The camera is initialized to the window's pixel
 * size with its center at the midpoint, using Y-down coordinates.
 *
 * <p>Typical per-frame lifecycle:
 * <ol>
 *   <li>{@link #pollEvents()}</li>
 *   <li>{@link #clear(Color)}</li>
 *   <li>{@link #draw(Renderable)} / {@link #draw(Renderable, DrawState)}</li>
 *   <li>{@link #present()} — flushes the draw queue and swaps front/back buffers</li>
 * </ol>
 *
 * <p>Call {@link #dispose()} once at shutdown to release GL resources and destroy the window.
 * After disposal, {@link #isActive()} returns {@code false}.
 */
public final class GraphicsContext extends AbstractRenderTarget {
    private static final Logger log = Log.get(Loggers.GRAPHICS);

    private final Window window;
    private boolean active = true;
    private float frameDelta;

    /**
     * Creates a graphics context for {@code window}, initializes the OpenGL backend, and
     * configures the default camera to match the window's initial size.
     *
     * @param window native window that owns the default framebuffer
     */
    public GraphicsContext(Window window) {
        super(RenderBackendFactory.create(window, BackendInitOptions.opengl(window.settings().vsync())));
        this.window = window;
        camera.setSize(window.settings().width(), window.settings().height());
        camera.setCenter(window.settings().width() / 2f, window.settings().height() / 2f);
        log.info("GraphicsContext created for window {}x{}", window.settings().width(), window.settings().height());
    }

    /**
     * Supplies the latest frame delta in seconds for throttled diagnostics during {@link #present()}.
     */
    public void setFrameDelta(float dt) {
        frameDelta = dt;
    }

    /**
     * Returns the window associated with this context.
     *
     * @return the underlying {@link Window}
     */
    public Window window() {
        return window;
    }

    /**
     * Returns the OpenGL backend used for rendering and resource management.
     *
     * @return the initialized {@link OpenGlBackend}
     */
    public RenderBackend backend() {
        return backend;
    }

    /**
     * Sets the GL clear color and clears the default framebuffer.
     *
     * @param color fill color in RGBA byte components
     */
    @Override
    public void clear(Color color) {
        backend.setClearColor(color);
        backend.clear();
    }

    /**
     * Returns the current window size in pixels (Y-down coordinate extent).
     *
     * @return live window dimensions
     */
    @Override
    public IntSize getSize() {
        return window.size();
    }

    /**
     * Flushes all queued draws to the default framebuffer.
     *
     * <p>Does not swap buffers; use {@link #present()} for a complete frame submission.
     */
    @Override
    public void flush() {
        flushQueuedDraws(getSize());
    }

    /**
     * Flushes queued draws and presents the frame by swapping window buffers.
     *
     * <p>Equivalent to {@link #flush()} followed by a buffer swap. This is the usual
     * end-of-frame call for on-screen rendering.
     */
    public void present() {
        flush();
        FrameDiagnostics.tick(frameDelta, getSize());
        window.swapBuffers();
    }

    /**
     * Returns whether this context is still usable for rendering.
     *
     * @return {@code true} if not disposed and the window is still open
     */
    public boolean isActive() {
        return active && window.isOpen();
    }

    /**
     * Polls pending window events (input, resize, close, etc.).
     *
     * <p>Should be called once per frame before processing input or drawing.
     */
    public void pollEvents() {
        window.pollEvents();
    }

    /**
     * Shuts down this context: marks it inactive, disposes the GL backend, and destroys
     * the window.
     *
     * <p>After this call, no further drawing or {@link #present()} should be attempted.
     */
    public void dispose() {
        active = false;
        log.info("Disposing GraphicsContext");
        backend.dispose();
        window.destroy();
    }
}
