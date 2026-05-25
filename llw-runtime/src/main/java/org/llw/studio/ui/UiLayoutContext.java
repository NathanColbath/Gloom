package org.llw.studio.ui;

import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scripting.js.PlayCameraBridge;

/**
 * Inputs for {@link UiLayout#collect}; layout always outputs screen-pixel rects.
 */
public final class UiLayoutContext {
    public final Camera2d camera;
    public final int viewportWidth;
    public final int viewportHeight;
    public final EntityId filterCanvas;
    /** When true, layout uses reference pixel space (UI Editor), ignoring world projection. */
    public final boolean authoringSpace;

    public UiLayoutContext(
            Camera2d camera,
            int viewportWidth,
            int viewportHeight,
            EntityId filterCanvas,
            boolean authoringSpace
    ) {
        this.camera = camera;
        this.viewportWidth = Math.max(1, viewportWidth);
        this.viewportHeight = Math.max(1, viewportHeight);
        this.filterCanvas = filterCanvas == null ? EntityId.none() : filterCanvas;
        this.authoringSpace = authoringSpace;
    }

    /**
     * Play-mode / game view layout using the mirrored play camera.
     */
    public static UiLayoutContext forPlay() {
        return new UiLayoutContext(
                PlayCameraBridge.mirrorCamera(),
                PlayCameraBridge.viewport().width(),
                PlayCameraBridge.viewport().height(),
                EntityId.none(),
                false
        );
    }

    /**
     * @param camera         orthographic camera for world-space projection
     * @param viewportWidth  target width in pixels
     * @param viewportHeight target height in pixels
     */
    public static UiLayoutContext forViewport(Camera2d camera, int viewportWidth, int viewportHeight) {
        return new UiLayoutContext(camera, viewportWidth, viewportHeight, EntityId.none(), false);
    }

    /**
     * UI Editor: one canvas in reference resolution, screen-space coordinates only.
     */
    public static UiLayoutContext forAuthoring(EntityId canvas, int referenceWidth, int referenceHeight) {
        Camera2d cam = new Camera2d();
        cam.setCenter(referenceWidth * 0.5f, referenceHeight * 0.5f);
        cam.setSize(referenceWidth, referenceHeight);
        return new UiLayoutContext(cam, referenceWidth, referenceHeight, canvas, true);
    }

    /** @return viewport size for {@link Camera2d#worldToScreen} */
    public IntSize viewportSize() {
        return new IntSize(viewportWidth, viewportHeight);
    }
}
