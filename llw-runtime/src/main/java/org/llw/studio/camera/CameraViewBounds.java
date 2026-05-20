package org.llw.studio.camera;

import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.math.vector.Vector2f;

/**
 * Shared orthographic camera view math for scripting, play mode, and the editor.
 *
 * <p><b>Coordinates (Y-down):</b> {@code centerX}/{@code centerY} are world-space pivot points.
 * {@code minX}/{@code minY} is the top-left corner of the visible rectangle;
 * {@code maxX}/{@code maxY} is the bottom-right. Screen mapping uses the same Y-down convention
 * via {@link #worldToScreen(float, float, IntSize)} and {@link #screenToWorld(float, float, IntSize)}.
 */
public final class CameraViewBounds {
    /** Default game-view width in pixels when no viewport is available. */
    public static final int DEFAULT_VIEWPORT_WIDTH = 640;
    /** Default game-view height in pixels when no viewport is available. */
    public static final int DEFAULT_VIEWPORT_HEIGHT = 360;

    /** World X of the camera center. */
    public final float centerX;
    /** World Y of the camera center. */
    public final float centerY;
    /** Orthographic half-height in world units (vertical extent from center to top/bottom edge). */
    public final float orthographicSize;
    /** Viewport width divided by height. */
    public final float aspect;
    /** Half of the visible world width. */
    public final float halfWidth;
    /** Half of the visible world height (equals {@link #orthographicSize}). */
    public final float halfHeight;
    /** Left edge of the visible world rectangle (smallest X). */
    public final float minX;
    /** Top edge of the visible world rectangle (smallest Y). */
    public final float minY;
    /** Right edge of the visible world rectangle (largest X). */
    public final float maxX;
    /** Bottom edge of the visible world rectangle (largest Y). */
    public final float maxY;

    private CameraViewBounds(
            float centerX,
            float centerY,
            float orthographicSize,
            float aspect,
            float halfWidth,
            float halfHeight
    ) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.orthographicSize = orthographicSize;
        this.aspect = aspect;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.minX = centerX - halfWidth;
        this.minY = centerY - halfHeight;
        this.maxX = centerX + halfWidth;
        this.maxY = centerY + halfHeight;
    }

    /** @return full visible width in world units */
    public float worldWidth() {
        return halfWidth * 2f;
    }

    /** @return full visible height in world units */
    public float worldHeight() {
        return halfHeight * 2f;
    }

    /** @return alias for {@link #worldWidth()} */
    public float width() {
        return worldWidth();
    }

    /** @return alias for {@link #worldHeight()} */
    public float height() {
        return worldHeight();
    }

    /**
     * Builds bounds from a camera center and orthographic size.
     *
     * @param centerX          world X of the camera pivot
     * @param centerY          world Y of the camera pivot
     * @param orthographicSize vertical half-extent in world units (clamped to at least 1)
     * @param aspect           width/height ratio (clamped to a small positive value)
     */
    public static CameraViewBounds fromCenter(float centerX, float centerY, float orthographicSize, float aspect) {
        float safeSize = Math.max(1f, orthographicSize);
        float safeAspect = Math.max(0.0001f, aspect);
        float halfHeight = safeSize;
        float halfWidth = safeSize * safeAspect;
        return new CameraViewBounds(centerX, centerY, safeSize, safeAspect, halfWidth, halfHeight);
    }

    /**
     * @return width divided by height, each dimension clamped to at least 1 pixel
     */
    public static float aspectFromViewport(int viewportWidth, int viewportHeight) {
        int width = Math.max(1, viewportWidth);
        int height = Math.max(1, viewportHeight);
        return width / (float) height;
    }

    /**
     * @return pixel viewport size with each dimension clamped to at least 1
     */
    public static IntSize viewportSize(int viewportWidth, int viewportHeight) {
        return new IntSize(
                Math.max(1, viewportWidth),
                Math.max(1, viewportHeight)
        );
    }

    /** @return render-backend camera matching this view rectangle */
    public Camera2d toCamera2d() {
        Camera2d camera = new Camera2d();
        camera.setCenter(centerX, centerY);
        camera.setSize(worldWidth(), worldHeight());
        return camera;
    }

    /**
     * Maps a world point to screen pixels (origin top-left, +Y down).
     *
     * @param worldX   world X
     * @param worldY   world Y
     * @param viewport target pixel dimensions
     */
    public Vector2f worldToScreen(float worldX, float worldY, IntSize viewport) {
        return toCamera2d().worldToScreen(new Vector2f(worldX, worldY), viewport);
    }

    /**
     * Maps screen pixels to world space (origin top-left, +Y down).
     *
     * @param screenX  pixel X relative to the viewport origin
     * @param screenY  pixel Y relative to the viewport origin
     * @param viewport source pixel dimensions
     */
    public Vector2f screenToWorld(float screenX, float screenY, IntSize viewport) {
        return toCamera2d().screenToWorld(new Vector2f(screenX, screenY), viewport);
    }
}
