package org.llw.studio.editor.gizmo;

import org.junit.jupiter.api.Test;
import org.llw.render.graphics.Camera2d;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GizmoHitTestTest {
    @Test
    void translateGizmoDetectsXAxis() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Player");
        object.transform().x = 100f;
        object.transform().y = 100f;

        EditorCamera editorCamera = new EditorCamera();
        editorCamera.frameBounds(0f, 0f, 200f, 200f, 400, 400);
        Camera2d camera = new Camera2d();
        editorCamera.applyTo(camera, 400, 400);
        GizmoContext context = new GizmoContext(editorCamera, camera, 400, 400);

        TranslateGizmo gizmo = new TranslateGizmo();
        var pivotScreen = context.worldToScreen(100f, 100f);
        var axisScreen = context.worldToScreen(164f, 100f);
        GizmoHit hit = gizmo.hitTest(context, scene, object.entity(), axisScreen.x, axisScreen.y);
        assertEquals(GizmoHit.X_AXIS, hit);

        GizmoHit centerHit = gizmo.hitTest(context, scene, object.entity(), pivotScreen.x, pivotScreen.y);
        assertEquals(GizmoHit.XY_PLANE, centerHit);
    }
}
