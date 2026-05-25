package org.llw.studio.editor.render;



/**

 * Draw-order layer constants for editor overlays (grid, selection, gizmos).

 */

public final class EditorRenderLayers {

    /** Background grid behind scene content. */

    public static final int GRID = -10_000;

    /** Default layer for scene sprites and shapes. */

    public static final int SCENE_BASE = 0;

    /** Camera frustum / preview gizmo. */

    public static final int CAMERA_GIZMO = 5_000;

    /** Component-specific overlays (lights, particles, colliders). */

    public static final int COMPONENT_GIZMO = 6_000;

    /** Script {@code onDrawGizmos} overlays. */

    public static final int SCRIPT_GIZMO = 7_000;

    /** Selection outline around picked entities. */

    public static final int SELECTION = 10_000;

    /** Transform gizmo handles and axes. */

    public static final int GIZMO = 20_000;



    private EditorRenderLayers() {

    }

}

