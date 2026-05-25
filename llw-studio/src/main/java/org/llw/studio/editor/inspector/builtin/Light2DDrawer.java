package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorPickerField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.Light2DComponent;

/** Inspector for {@link Light2DComponent}. */
public final class Light2DDrawer implements ComponentDrawer<Light2DComponent> {
    @Override
    public void draw(Light2DComponent component, InspectorContext context) {
        if (ImGui.beginCombo("Type", component.type == null ? "POINT" : component.type)) {
            for (String type : new String[]{"GLOBAL", "DIRECTIONAL", "POINT", "SPOT"}) {
                if (ImGui.selectable(type, type.equalsIgnoreCase(component.type))) {
                    component.type = type;
                }
            }
            ImGui.endCombo();
        }
        float[] color = ColorPickerField.draw("Color", component.r, component.g, component.b, 1f);
        component.r = color[0];
        component.g = color[1];
        component.b = color[2];
        component.intensity = FloatField.draw("Intensity", component.intensity);
        component.range = FloatField.draw("Range", component.range);
        component.innerAngle = FloatField.draw("Inner Angle", component.innerAngle);
        component.outerAngle = FloatField.draw("Outer Angle", component.outerAngle);
        ImBoolean bake = new ImBoolean(component.includeInBake);
        if (ImGui.checkbox("Include In Bake", bake)) {
            component.includeInBake = bake.get();
        }
        context.markDirty();
    }
}
