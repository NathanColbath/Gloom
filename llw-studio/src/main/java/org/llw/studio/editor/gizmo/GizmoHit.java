package org.llw.studio.editor.gizmo;



/**

 * Hit target on a transform gizmo (axis, plane, rotate ring, or scale handle).

 */

public enum GizmoHit {

    /** No gizmo part under the cursor. */

    NONE,

    /** X translation axis. */

    X_AXIS,

    /** Y translation axis. */

    Y_AXIS,

    /** XY translation plane (center handle). */

    XY_PLANE,

    /** Rotate ring. */

    ROTATE,

    /** Scale along X. */

    SCALE_X,

    /** Scale along Y. */

    SCALE_Y,

    /** Uniform scale (corner handle). */

    SCALE_UNIFORM

}

