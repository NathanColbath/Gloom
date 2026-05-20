package org.llw.studio.editor.gizmo;



import org.llw.math.vector.Vector2f;

import org.llw.render.core.IntSize;

import org.llw.render.graphics.Camera2d;

import org.llw.studio.editor.EditorCamera;



/**

 * Screen/world mapping and hit-test helpers for gizmos in the scene view.

 */

public final class GizmoContext {

    /** Pixel tolerance for handle and axis hit tests. */

    public static final float HIT_TOLERANCE_PX = 10f;

    /** Default axis length in world units. */

    public static final float AXIS_LENGTH_WORLD = 64f;



    private final EditorCamera editorCamera;

    private final Camera2d camera;

    private final int viewWidth;

    private final int viewHeight;



    /**

     * @param editorCamera editor pan/zoom state

     * @param camera       render camera applied to the offscreen target

     * @param viewWidth    viewport width in screen pixels

     * @param viewHeight   viewport height in screen pixels

     */

    public GizmoContext(EditorCamera editorCamera, Camera2d camera, int viewWidth, int viewHeight) {

        this.editorCamera = editorCamera;

        this.camera = camera;

        this.viewWidth = viewWidth;

        this.viewHeight = viewHeight;

    }



    /** @return editor camera backing this context */

    public EditorCamera editorCamera() {

        return editorCamera;

    }



    /** @return editor zoom (pixels per world unit) */

    public float zoom() {

        return editorCamera.zoom();

    }



    /**

     * @param worldX world X

     * @param worldY world Y

     * @return position in scene-view screen pixels (origin top-left of the image)

     */

    public Vector2f worldToScreen(float worldX, float worldY) {

        return camera.worldToScreen(new Vector2f(worldX, worldY), new IntSize(viewWidth, viewHeight));

    }



    /**

     * @param screenX X relative to the scene image (screen pixels)

     * @param screenY Y relative to the scene image (screen pixels)

     * @return position in world units

     */

    public Vector2f screenToWorld(float screenX, float screenY) {

        return camera.screenToWorld(new Vector2f(screenX, screenY), new IntSize(viewWidth, viewHeight));

    }



    /** @return world units per screen pixel at the current zoom */

    public float worldUnitsPerPixel() {

        return 1f / Math.max(0.0001f, zoom());

    }



    /** @return gizmo handle size in world units derived from {@link #HIT_TOLERANCE_PX} */

    public float handleSizeWorld() {

        return HIT_TOLERANCE_PX * worldUnitsPerPixel();

    }



    /**

     * @return shortest distance from point P to segment AB in screen space

     */

    public static float distancePointToSegment(float px, float py, float ax, float ay, float bx, float by) {

        float abx = bx - ax;

        float aby = by - ay;

        float apx = px - ax;

        float apy = py - ay;

        float abLenSq = abx * abx + aby * aby;

        if (abLenSq <= 0.0001f) {

            return (float) Math.hypot(px - ax, py - ay);

        }

        float t = Math.max(0f, Math.min(1f, (apx * abx + apy * aby) / abLenSq));

        float closestX = ax + abx * t;

        float closestY = ay + aby * t;

        return (float) Math.hypot(px - closestX, py - closestY);

    }



    /**

     * @param screenX screen X of the cursor

     * @param screenY screen Y of the cursor

     * @param worldX  world position to test

     * @param worldY  world position to test

     * @return true if the cursor is within {@link #HIT_TOLERANCE_PX} of the projected point

     */

    public boolean nearPoint(float screenX, float screenY, float worldX, float worldY) {

        Vector2f screen = worldToScreen(worldX, worldY);

        return Math.hypot(screenX - screen.x, screenY - screen.y) <= HIT_TOLERANCE_PX;

    }



    /**

     * @param screenX screen X of the cursor

     * @param screenY screen Y of the cursor

     * @param ax      world start X of segment

     * @param ay      world start Y of segment

     * @param bx      world end X of segment

     * @param by      world end Y of segment

     * @return true if the cursor is within tolerance of the projected segment

     */

    public boolean nearSegment(float screenX, float screenY, float ax, float ay, float bx, float by) {

        Vector2f a = worldToScreen(ax, ay);

        Vector2f b = worldToScreen(bx, by);

        return distancePointToSegment(screenX, screenY, a.x, a.y, b.x, b.y) <= HIT_TOLERANCE_PX;

    }

}

