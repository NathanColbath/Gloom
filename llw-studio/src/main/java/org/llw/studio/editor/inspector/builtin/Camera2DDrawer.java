package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import org.llw.studio.camera.CameraViewBounds;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.ColorPickerField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.Camera2DComponent;

/**
 * Inspector fields for {@link Camera2DComponent}; shows world size hints when marked main camera.
 */
public final class Camera2DDrawer implements ComponentDrawer<Camera2DComponent> {
    private static final float MIN_ORTHO_SIZE = 16f;
    private static final float MAX_ORTHO_SIZE = 4000f;

    @Override
    public void draw(Camera2DComponent component, InspectorContext context) {
        PropertyRow.begin("Orthographic Size");
        float[] ortho = {clamp(component.orthographicSize, MIN_ORTHO_SIZE, MAX_ORTHO_SIZE)};
        ImGui.setNextItemWidth(-1f);
        ImGui.sliderFloat("##orthographicSize", ortho, MIN_ORTHO_SIZE, MAX_ORTHO_SIZE, "%.0f");
        PropertyRow.end();
        component.orthographicSize = ortho[0];

        component.depth = FloatField.draw("Depth", component.depth);
        component.mainCamera = BoolField.draw("Main Camera", component.mainCamera);

        float[] background = ColorPickerField.draw(
                "Background",
                component.backgroundR,
                component.backgroundG,
                component.backgroundB,
                component.backgroundA
        );
        component.backgroundR = background[0];
        component.backgroundG = background[1];
        component.backgroundB = background[2];
        component.backgroundA = background[3];

        if (component.mainCamera) {
            EditorSession session = context.editorSession();
            CameraViewBounds bounds = CameraViewBounds.fromCenter(
                    0f,
                    0f,
                    component.orthographicSize,
                    CameraViewBounds.aspectFromViewport(session.gameViewWidth(), session.gameViewHeight())
            );
            ImGui.textDisabled(String.format("World Width: %.1f", bounds.worldWidth()));
            ImGui.textDisabled(String.format("World Height: %.1f", bounds.worldHeight()));
        }
        context.markDirty();
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
