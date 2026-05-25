package org.llw.studio.editor;

/**
 * Converts between editor zoom, world units, and screen pixels for crisp viewport overlays.
 *
 * <p><strong>Screen</strong> coordinates are top-left origin pixels within the scene view.
 * <strong>World</strong> coordinates are Y-down units in {@link EditorCamera} space.
 */
public final class EditorViewportMath {
    private static final float GRID_TARGET_PIXELS = 64f;
    private static final float GRID_MIN_PIXELS = 32f;
    private static final float GRID_MAX_PIXELS = 128f;

    private EditorViewportMath() {
    }

    /**
     * World units represented by one screen pixel at the given zoom.
     *
     * @param zoom editor camera zoom (pixels per world unit)
     * @return world units per screen pixel
     */
    public static float worldUnitsPerPixel(float zoom) {
        return 1f / Math.max(zoom, 0.0001f);
    }

    /**
     * Converts a screen-space distance to world units.
     *
     * @param zoom   editor camera zoom (pixels per world unit)
     * @param pixels length in screen pixels
     * @return equivalent length in world units
     */
    public static float pixelsToWorld(float zoom, float pixels) {
        return pixels * worldUnitsPerPixel(zoom);
    }

    /**
     * Picks a grid step in world units so major lines stay near 64 screen pixels apart.
     *
     * @param zoom editor camera zoom (pixels per world unit)
     * @return grid spacing in world units
     */
    public static float chooseGridStep(float zoom) {
        // Snap to 1/2/5/10 decades so major lines stay near 64px without jitter while panning/zooming.
        float step = GRID_TARGET_PIXELS * worldUnitsPerPixel(zoom);
        float magnitude = (float) Math.pow(10, Math.floor(Math.log10(Math.max(step, 1e-6f))));
        float normalized = step / magnitude;
        float nice;
        if (normalized < 1.5f) {
            nice = 1f;
        } else if (normalized < 3.5f) {
            nice = 2f;
        } else if (normalized < 7.5f) {
            nice = 5f;
        } else {
            nice = 10f;
        }
        step = nice * magnitude;
        while (step * zoom < GRID_MIN_PIXELS) {
            step *= 2f;
        }
        while (step * zoom > GRID_MAX_PIXELS) {
            step *= 0.5f;
        }
        return Math.max(step, 1e-4f);
    }

    /**
     * Visible world width for a viewport of the given screen width.
     *
     * @param camera     editor camera defining center and zoom
     * @param viewWidth  viewport width in screen pixels
     * @return span along world X visible in the view
     */
    public static float worldWidth(EditorCamera camera, int viewWidth) {
        return viewWidth / Math.max(camera.zoom(), 0.0001f);
    }

    /**
     * Visible world height for a viewport of the given screen height.
     *
     * @param camera      editor camera defining center and zoom
     * @param viewHeight  viewport height in screen pixels
     * @return span along world Y visible in the view
     */
    public static float worldHeight(EditorCamera camera, int viewHeight) {
        return viewHeight / Math.max(camera.zoom(), 0.0001f);
    }

    /**
     * World X of the left edge of the visible rect (screen X = 0).
     *
     * @param camera     editor camera defining center and zoom
     * @param viewWidth  viewport width in screen pixels
     * @return world X at the left of the view
     */
    public static float worldLeft(EditorCamera camera, int viewWidth) {
        return camera.centerX() - worldWidth(camera, viewWidth) * 0.5f;
    }

    /**
     * World Y of the top edge of the visible rect (screen Y = 0).
     *
     * @param camera      editor camera defining center and zoom
     * @param viewHeight  viewport height in screen pixels
     * @return world Y at the top of the view
     */
    public static float worldTop(EditorCamera camera, int viewHeight) {
        return camera.centerY() - worldHeight(camera, viewHeight) * 0.5f;
    }

    /**
     * Snaps a world X so its projection lands on an integer screen pixel (crisp overlays).
     *
     * @param camera     editor camera defining center and zoom
     * @param viewWidth  viewport width in screen pixels
     * @param worldX     input position in world units
     * @return snapped world X
     */
    public static float snapWorldX(EditorCamera camera, int viewWidth, float worldX) {
        // Round-trip through screen space so grid/gizmo lines land on integer pixels at any zoom.
        float left = worldLeft(camera, viewWidth);
        float sizeX = worldWidth(camera, viewWidth);
        float screenX = (worldX - left) / sizeX * viewWidth;
        float snappedScreen = Math.round(screenX);
        return left + (snappedScreen / viewWidth) * sizeX;
    }

    /**
     * Snaps a world Y so its projection lands on an integer screen pixel (crisp overlays).
     *
     * @param camera      editor camera defining center and zoom
     * @param viewHeight  viewport height in screen pixels
     * @param worldY      input position in world units
     * @return snapped world Y
     */
    public static float snapWorldY(EditorCamera camera, int viewHeight, float worldY) {
        float top = worldTop(camera, viewHeight);
        float sizeY = worldHeight(camera, viewHeight);
        float screenY = (worldY - top) / sizeY * viewHeight;
        float snappedScreen = Math.round(screenY);
        return top + (snappedScreen / viewHeight) * sizeY;
    }
}
