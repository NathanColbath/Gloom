package org.llw.render.graphics;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;

/**
 * SFML-style render target that accepts queued draw commands and flushes them on demand.
 *
 * <p>Implementations ({@link GraphicsContext}, {@link OffscreenTarget}) batch
 * {@link #draw(Renderable)} calls internally. Nothing is submitted to the GPU until
 * {@link #flush()} is invoked. For the default window target, {@link GraphicsContext#present()}
 * calls {@code flush()} before swapping buffers.
 *
 * <p>All drawing uses the target's {@link Camera2d} and Y-down pixel coordinates.
 */
public interface RenderTarget {
    /**
     * Clears the target's color buffer to {@code color}.
     *
     * @param color fill color in RGBA byte components
     */
    void clear(Color color);

    /**
     * Enqueues a draw using {@link DrawState#DEFAULT} (alpha blending, identity transform,
     * layer 0, no texture override).
     *
     * <p>The draw is not executed until {@link #flush()} is called.
     *
     * @param renderable object that emits GPU geometry when flushed
     */
    void draw(Renderable renderable);

    /**
     * Enqueues a draw with explicit blend mode, texture, shader, transform, and layer.
     *
     * <p>The draw is not executed until {@link #flush()} is called.
     *
     * @param renderable object that emits GPU geometry when flushed
     * @param state per-draw overrides applied when the queue is flushed
     */
    void draw(Renderable renderable, DrawState state);

    /**
     * Replaces this target's camera with a copy of {@code camera}'s center, size, and viewport.
     *
     * @param camera source camera whose settings are copied into this target
     */
    void setCamera(Camera2d camera);

    /**
     * Returns the camera owned by this target. Mutations affect subsequent draws.
     *
     * @return this target's active {@link Camera2d}
     */
    Camera2d getCamera();

    /**
     * Returns the pixel dimensions of this target in Y-down screen space.
     *
     * @return width and height in pixels
     */
    IntSize getSize();

    /**
     * Submits all queued draws to the GPU for this target.
     *
     * <p>After flushing, the queue is empty and ready for the next frame's draw calls.
     * On-screen targets typically flush via {@link GraphicsContext#present()}; off-screen
     * targets should call this explicitly before sampling {@link OffscreenTarget#colorTexture()}.
     */
    void flush();
}
