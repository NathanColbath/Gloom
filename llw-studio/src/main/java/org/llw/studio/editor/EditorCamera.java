package org.llw.studio.editor;



import org.llw.math.vector.Vector2f;



/**

 * Pan/zoom camera for the scene view; maps between screen pixels and world units (Y-down).

 */

public final class EditorCamera {

    private float centerX;

    private float centerY;

    private float zoom = 1f;



    /** @return world X at the center of the view */

    public float centerX() {

        return centerX;

    }



    /** @return world Y at the center of the view */

    public float centerY() {

        return centerY;

    }



    /**

     * @return zoom factor as screen pixels per world unit

     */

    public float zoom() {

        return zoom;

    }



    /**

     * Pans the camera by a screen-pixel delta (converted using current zoom).

     *

     * @param dx horizontal drag in screen pixels

     * @param dy vertical drag in screen pixels

     */

    public void pan(float dx, float dy) {

        centerX += dx / zoom;

        centerY += dy / zoom;

    }



    /**

     * Multiplies zoom by {@code factor}, clamped to a usable editor range.

     *

     * @param factor scale multiplier (e.g. 1.1f or 0.9f per wheel notch)

     */

    public void zoomBy(float factor) {

        zoom = Math.max(0.1f, Math.min(8f, zoom * factor));

    }



    /**

     * Copies center and effective size into a render {@link org.llw.render.graphics.Camera2d}.

     *

     * @param camera  target camera updated in place

     * @param width   viewport width in screen pixels

     * @param height  viewport height in screen pixels

     */

    public void applyTo(org.llw.render.graphics.Camera2d camera, int width, int height) {

        camera.setSize(width / zoom, height / zoom);

        camera.setCenter(centerX, centerY);

    }



    /**

     * Converts a point in scene-view screen space to world units.

     *

     * @param screenX    X relative to the viewport image (0 = left)

     * @param screenY    Y relative to the viewport image (0 = top)

     * @param viewWidth  viewport width in screen pixels

     * @param viewHeight viewport height in screen pixels

     * @return position in world space

     */

    public Vector2f screenToWorld(float screenX, float screenY, int viewWidth, int viewHeight) {

        org.llw.render.graphics.Camera2d camera = new org.llw.render.graphics.Camera2d();

        applyTo(camera, viewWidth, viewHeight);

        return camera.screenToWorld(

                new Vector2f(screenX, screenY),

                new org.llw.render.core.IntSize(viewWidth, viewHeight)

        );

    }



    /**

     * Frames the camera to fit an axis-aligned world bounds rect inside the viewport.

     *

     * @param minX       world left

     * @param minY       world top

     * @param maxX       world right

     * @param maxY       world bottom

     * @param viewWidth  viewport width in screen pixels

     * @param viewHeight viewport height in screen pixels

     */

    public void frameBounds(float minX, float minY, float maxX, float maxY, int viewWidth, int viewHeight) {

        if (minX > maxX || minY > maxY) {

            centerX = 0f;

            centerY = 0f;

            zoom = 1f;

            return;

        }

        float contentWidth = Math.max(1f, maxX - minX);

        float contentHeight = Math.max(1f, maxY - minY);

        centerX = (minX + maxX) * 0.5f;

        centerY = (minY + maxY) * 0.5f;

        float zoomX = viewWidth / contentWidth;

        float zoomY = viewHeight / contentHeight;

        zoom = Math.max(0.1f, Math.min(8f, Math.min(zoomX, zoomY) * 0.9f));

    }

}

