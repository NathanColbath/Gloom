package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;

public final class EdgeCollider2DDrawer implements ComponentDrawer<EdgeCollider2DComponent> {
    @Override
    public void draw(EdgeCollider2DComponent component, InspectorContext context) {
        component.isTrigger = BoolField.draw("Is Trigger", component.isTrigger);
        ImInt layer = new ImInt(component.layer);
        if (ImGui.inputInt("Layer", layer)) {
            component.layer = layer.get();
        }
        ImInt mask = new ImInt(component.layerMask);
        if (ImGui.inputInt("Layer Mask", mask)) {
            component.layerMask = mask.get();
        }
        ImGui.textDisabled("Edit edge points in scene (future)");
        context.markDirty();
    }
}
