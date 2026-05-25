package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorPickerField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.SceneLightingComponent;

/** Inspector for {@link SceneLightingComponent}. */
public final class SceneLightingDrawer implements ComponentDrawer<SceneLightingComponent> {
    @Override
    public void draw(SceneLightingComponent component, InspectorContext context) {
        float[] ambient = ColorPickerField.draw(
                "Ambient Color",
                component.ambientR,
                component.ambientG,
                component.ambientB,
                1f
        );
        component.ambientR = ambient[0];
        component.ambientG = ambient[1];
        component.ambientB = ambient[2];
        component.ambientIntensity = FloatField.draw("Ambient Intensity", component.ambientIntensity);
        ImBoolean enabled = new ImBoolean(component.lightmapEnabled);
        if (ImGui.checkbox("Use Baked Lightmap", enabled)) {
            component.lightmapEnabled = enabled.get();
        }
        component.lightmapMinX = FloatField.draw("Lightmap Min X", component.lightmapMinX);
        component.lightmapMinY = FloatField.draw("Lightmap Min Y", component.lightmapMinY);
        component.lightmapMaxX = FloatField.draw("Lightmap Max X", component.lightmapMaxX);
        component.lightmapMaxY = FloatField.draw("Lightmap Max Y", component.lightmapMaxY);
        if (ImGui.button("Bake Lighting")) {
            org.llw.studio.editor.lighting.LightBakeService.bake(
                    context.studioContext().editScene(),
                    context.assets(),
                    component
            );
        }
        context.markDirty();
    }
}
