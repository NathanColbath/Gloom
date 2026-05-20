package org.llw.render.graphics;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.gl.OpenGlBackend;

/**
 * Object that can emit GPU draw commands when a {@link RenderTarget} flushes its queue.
 *
 * <p>Implementations are enqueued via {@link RenderTarget#draw(Renderable)} and invoked
 * later during {@link RenderTarget#flush()} with the active {@link OpenGlBackend} and
 * the {@link DrawState} supplied at enqueue time.
 */
public interface Renderable {
    /**
     * Records geometry and draw state into the backend for the current flush pass.
     *
     * @param backend GPU backend executing the flush
     * @param state   blend mode, texture, shader, transform, and layer for this submission
     */
    void render(OpenGlBackend backend, DrawState state);
}
