package org.llw.studio.editor;



/**

 * Active scene-view manipulation tool (hand pan vs transform gizmos).

 */

public enum SceneToolMode {

    /** Pan the editor camera; gizmos are inactive. */

    HAND,

    /** Translate gizmo. */

    MOVE,

    /** Rotate gizmo. */

    ROTATE,

    /** Scale gizmo. */

    SCALE,

    /** Paint tiles on a tilemap. */

    TILE_PAINT,

    /** Erase tiles from a tilemap. */

    TILE_ERASE

}

