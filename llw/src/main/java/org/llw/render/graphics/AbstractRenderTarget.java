package org.llw.render.graphics;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.backend.RenderBackend;

/**
 * Shared {@link RenderTarget} implementation that queues draws and flushes through
 * {@link RenderBackend}.
 */
abstract class AbstractRenderTarget implements RenderTarget {
    protected final RenderBackend backend;
    protected final org.llw.render.gl.DrawQueue drawQueue = new org.llw.render.gl.DrawQueue();
    protected final Camera2d camera = new Camera2d();

    protected AbstractRenderTarget(RenderBackend backend) {
        this.backend = backend;
    }

    @Override
    public void draw(Renderable renderable) {
        draw(renderable, DrawState.DEFAULT);
    }

    @Override
    public void draw(Renderable renderable, DrawState state) {
        drawQueue.enqueue(renderable, state);
    }

    @Override
    public void setCamera(Camera2d camera) {
        this.camera.setCenter(camera.getCenter());
        this.camera.setSize(camera.getSize());
        this.camera.setViewport(
                camera.getViewport().left,
                camera.getViewport().top,
                camera.getViewport().width,
                camera.getViewport().height
        );
    }

    @Override
    public Camera2d getCamera() {
        return camera;
    }

    protected void flushQueuedDraws(IntSize size) {
        backend.beginFrame(size);
        backend.setViewProjection(camera.getViewProjection(size));
        drawQueue.flush(backend);
    }
}
