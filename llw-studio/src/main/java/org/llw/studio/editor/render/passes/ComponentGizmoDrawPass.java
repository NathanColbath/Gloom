package org.llw.studio.editor.render.passes;

import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.gizmo.ComponentGizmoRegistry;
import org.llw.studio.editor.gizmo.GizmoDrawContext;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.scene.Scene;

/**
 * Draws registered {@link org.llw.studio.editor.gizmo.ComponentSceneGizmo} overlays.
 */
public final class ComponentGizmoDrawPass {
    private static final float LINE_PIXELS = 2f;
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.COMPONENT_GIZMO);

    private ComponentGizmoDrawPass() {
    }

    public static void draw(
            Scene scene,
            OffscreenTarget target,
            EditorCamera editorCamera,
            SelectionService selection,
            AssetDatabase assets,
            ComponentGizmoRegistry registry,
            int viewWidth,
            int viewHeight
    ) {
        float line = EditorViewportMath.pixelsToWorld(editorCamera.zoom(), LINE_PIXELS);
        GizmoDrawContext context = new GizmoDrawContext(
                scene,
                target,
                editorCamera,
                viewWidth,
                viewHeight,
                assets,
                selection,
                line,
                STATE
        );
        // Unselected emitters/lights draw faintly; selected entity gets full handles on top.
        registry.drawAll(context);
        registry.drawSelected(context);
    }
}

