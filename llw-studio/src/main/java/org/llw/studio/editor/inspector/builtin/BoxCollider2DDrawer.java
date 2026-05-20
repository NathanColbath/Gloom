package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.studio.ecs.components.BoxCollider2DComponent;

public final class BoxCollider2DDrawer implements ComponentDrawer<BoxCollider2DComponent> {
    @Override
    public void draw(BoxCollider2DComponent component, InspectorContext context) {
        component.sizeX = FloatField.draw("Size X", component.sizeX);
        component.sizeY = FloatField.draw("Size Y", component.sizeY);
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
