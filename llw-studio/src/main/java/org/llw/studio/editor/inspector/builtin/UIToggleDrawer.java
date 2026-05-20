package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.UIToggleComponent;

/** Inspector fields for {@link UIToggleComponent}. */
public final class UIToggleDrawer implements ComponentDrawer<UIToggleComponent> {
    private final ImString labelBuffer = new ImString(256);

    @Override
    public void draw(UIToggleComponent component, InspectorContext context) {
        labelBuffer.set(component.label == null ? "" : component.label);
        if (ImGui.inputText("Label", labelBuffer)) {
            component.label = labelBuffer.get();
            context.markDirty();
        }
        if (ImGui.checkbox("Is On", component.isOn)) {
            context.markDirty();
        }
        component.width = FloatField.draw("Width", component.width);
        component.height = FloatField.draw("Height", component.height);
        component.boxSize = FloatField.draw("Box Size", component.boxSize);
        component.fontSize = (int) FloatField.draw("Font Size", component.fontSize);
        float[] off = ColorField.draw("Off", component.r, component.g, component.b, component.a);
        component.r = off[0];
        component.g = off[1];
        component.b = off[2];
        component.a = off[3];
        float[] on = ColorField.draw("On", component.onR, component.onG, component.onB, component.onA);
        component.onR = on[0];
        component.onG = on[1];
        component.onB = on[2];
        component.onA = on[3];
        float[] text = ColorField.draw("Text", component.textR, component.textG, component.textB, component.textA);
        component.textR = text[0];
        component.textG = text[1];
        component.textB = text[2];
        component.textA = text[3];
        if (ImGui.checkbox("Interactable", component.interactable)) {
            context.markDirty();
        }
        context.markDirty();
    }
}
