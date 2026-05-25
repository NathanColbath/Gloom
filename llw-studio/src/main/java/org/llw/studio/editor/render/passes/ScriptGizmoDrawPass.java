package org.llw.studio.editor.render.passes;

import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.log.StudioLogSink;
import org.llw.util.log.LogLevel;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.EditorScriptGizmoRuntime;

/**
 * Invokes script gizmo hooks and flushes the draw buffer into the scene view.
 */
public final class ScriptGizmoDrawPass {
    private static final float LINE_PIXELS = 2f;
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.SCRIPT_GIZMO);

    private ScriptGizmoDrawPass() {
    }

    public static void draw(
            Scene scene,
            OffscreenTarget target,
            EditorCamera editorCamera,
            SelectionService selection,
            EditorScriptGizmoRuntime runtime,
            StudioLogSink console,
            int viewWidth,
            int viewHeight
    ) {
        if (runtime == null) {
            return;
        }
        float line = EditorViewportMath.pixelsToWorld(editorCamera.zoom(), LINE_PIXELS);
        runtime.setEditScene(scene);
        runtime.prepareFrame(line);
        // All entities first, then selected-only pass so selection highlights stack correctly.
        runtime.invokeGizmos(false, null);
        runtime.invokeGizmos(true, selection.allSelected());
        if (runtime.buffer().budgetExceeded()) {
            console.append(LogLevel.WARN, "Script gizmo primitive budget exceeded");
        }
        runtime.buffer().flush(target, STATE, line);
    }
}

