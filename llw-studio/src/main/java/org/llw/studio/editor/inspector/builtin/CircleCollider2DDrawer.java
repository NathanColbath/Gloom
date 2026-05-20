package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.CircleCollider2DComponent;

public final class CircleCollider2DDrawer implements ComponentDrawer<CircleCollider2DComponent> {
    @Override
    public void draw(CircleCollider2DComponent component, InspectorContext context) {
        component.radius = FloatField.draw("Radius", component.radius);
        component.offsetX = FloatField.draw("Offset X", component.offsetX);
        component.offsetY = FloatField.draw("Offset Y", component.offsetY);
        component.isTrigger = BoolField.draw("Is Trigger", component.isTrigger);
        ImInt layer = new ImInt(component.layer);
        if (ImGui.inputInt("Layer", layer)) {
            component.layer = layer.get();
        }
        ImInt mask = new ImInt(component.layerMask);
        if (ImGui.inputInt("Layer Mask", mask)) {
            component.layerMask = mask.get();
        }
        context.markDirty();
    }
}
