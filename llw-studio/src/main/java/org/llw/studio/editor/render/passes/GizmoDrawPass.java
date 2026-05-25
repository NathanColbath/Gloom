package org.llw.studio.editor.render.passes;

import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.SceneToolState;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.gizmo.GizmoContext;
import org.llw.studio.editor.gizmo.GizmoController;
import org.llw.studio.scene.Scene;

/**
 * Delegates transform gizmo rendering to {@link GizmoController} with editor and render cameras.
 */
public final class GizmoDrawPass {
    private GizmoDrawPass() {
    }

    /**
     * @param scene         scene being edited
     * @param target        offscreen scene-view target
     * @param editorCamera  pan/zoom editor camera
     * @param toolState     active scene tool (move, rotate, etc.)
     * @param selection     current entity selection
     * @param controller    gizmo implementation
     * @param viewWidth     viewport width in pixels
     * @param viewHeight    viewport height in pixels
     */
    public static void draw(
            Scene scene,
            OffscreenTarget target,
            EditorCamera editorCamera,
            SceneToolState toolState,
            SelectionService selection,
            GizmoController controller,
            int viewWidth,
            int viewHeight
    ) {
        GizmoContext context = new GizmoContext(editorCamera, target.getCamera(), viewWidth, viewHeight);
        controller.draw(scene, toolState, selection, context, target);
    }
}

