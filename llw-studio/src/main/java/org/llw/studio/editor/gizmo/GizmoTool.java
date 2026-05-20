package org.llw.studio.editor.gizmo;



import org.llw.render.graphics.OffscreenTarget;

import org.llw.studio.ecs.EntityId;

import org.llw.studio.scene.Scene;



/**

 * Translate, rotate, or scale gizmo: hit-test, drag, and draw in screen/world space.

 */

public interface GizmoTool {

    /**

     * @param context  camera and viewport for screen/world conversion

     * @param scene    scene containing the entity

     * @param entity   selected entity

     * @param screenX  mouse X relative to the scene image (screen pixels)

     * @param screenY  mouse Y relative to the scene image (screen pixels)

     * @return hit part, or {@link GizmoHit#NONE}

     */

    GizmoHit hitTest(GizmoContext context, Scene scene, EntityId entity, float screenX, float screenY);



    /**

     * Captures drag start state when the user presses on a gizmo handle.

     *

     * @param context  camera context

     * @param scene    edit scene

     * @param entity   dragged entity

     * @param hit      handle that was hit

     * @param screenX  mouse X in screen pixels

     * @param screenY  mouse Y in screen pixels

     */

    void beginDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY);



    /**

     * Applies drag delta while the mouse moves.

     *

     * @param context  camera context

     * @param scene    edit scene

     * @param entity   dragged entity

     * @param hit      active handle

     * @param screenX  mouse X in screen pixels

     * @param screenY  mouse Y in screen pixels

     */

    void updateDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY);



    /**

     * Draws the gizmo overlay into the offscreen target.

     *

     * @param context  camera context

     * @param target   scene view render target

     * @param scene    edit scene

     * @param entity   selected entity

     * @param hover    handle under the cursor, if any

     */

    void draw(GizmoContext context, OffscreenTarget target, Scene scene, EntityId entity, GizmoHit hover);

}

