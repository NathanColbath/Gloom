package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.physics.PhysicsBodyType;

public final class Rigidbody2DDrawer implements ComponentDrawer<Rigidbody2DComponent> {
    @Override
    public void draw(Rigidbody2DComponent component, InspectorContext context) {
        if (ImGui.beginCombo("Body Type", component.bodyType.name())) {
            for (PhysicsBodyType type : PhysicsBodyType.values()) {
                if (ImGui.selectable(type.name(), type == component.bodyType)) {
                    component.bodyType = type;
                }
            }
            ImGui.endCombo();
        }
        component.mass = FloatField.draw("Mass", component.mass);
        component.gravityScale = FloatField.draw("Gravity Scale", component.gravityScale);
        component.linearDrag = FloatField.draw("Linear Drag", component.linearDrag);
        component.angularDrag = FloatField.draw("Angular Drag", component.angularDrag);
        component.freezeRotation = BoolField.draw("Freeze Rotation", component.freezeRotation);
        component.simulated = BoolField.draw("Simulated", component.simulated);
        context.markDirty();
    }
}
