/**
 * SFML-style 2D rendering API built around render targets, cameras, and batched draw queues.
 *
 * <p>All coordinates in this package use a <strong>Y-down</strong> screen space: the origin
 * is the top-left corner of a render target, {@code x} increases to the right, and
 * {@code y} increases downward. This matches SFML, many UI toolkits, and typical 2D game
 * conventions rather than OpenGL's default bottom-left origin.
 *
 * <p>The central abstraction is {@link RenderTarget}, implemented by the on-screen
 * {@link GraphicsContext} and off-screen {@link OffscreenTarget}. Draw calls submitted via
 * {@link RenderTarget#draw(Renderable)} are queued and not executed until
 * {@link RenderTarget#flush()} is called.
 *
 * <p>Typical on-screen frame lifecycle:
 * <ol>
 *   <li>{@link GraphicsContext#pollEvents()} — process window input events</li>
 *   <li>{@link RenderTarget#clear(org.llw.render.core.Color)} — clear the framebuffer</li>
 *   <li>{@link RenderTarget#draw(Renderable)} — enqueue one or more draw commands</li>
 *   <li>{@link GraphicsContext#present()} — {@link RenderTarget#flush()} the queue, then swap buffers</li>
 * </ol>
 *
 * <p>Off-screen rendering follows the same clear → draw → {@link OffscreenTarget#flush()}
 * pattern; call {@link OffscreenTarget#dispose()} when the framebuffer is no longer needed.
 * Shut down the application with {@link GraphicsContext#dispose()}, which releases the GL
 * backend and destroys the window.
 *
 * @see RenderTarget
 * @see GraphicsContext
 * @see OffscreenTarget
 * @see Camera2d
 */
package org.llw.render.graphics;
