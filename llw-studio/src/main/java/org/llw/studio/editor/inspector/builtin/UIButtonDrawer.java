package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.UIButtonComponent;

/** Inspector fields for {@link UIButtonComponent}. */
public final class UIButtonDrawer implements ComponentDrawer<UIButtonComponent> {
    private final ImString labelBuffer = new ImString(256);

    @Override
    public void draw(UIButtonComponent component, InspectorContext context) {
        labelBuffer.set(component.label == null ? "" : component.label);
        if (ImGui.inputText("Label", labelBuffer)) {
            component.label = labelBuffer.get();
            context.markDirty();
        }
        component.width = FloatField.draw("Width", component.width);
        component.height = FloatField.draw("Height", component.height);
        component.fontSize = (int) FloatField.draw("Font Size", component.fontSize);
        float[] normal = ColorField.draw("Normal", component.r, component.g, component.b, component.a);
        component.r = normal[0];
        component.g = normal[1];
        component.b = normal[2];
        component.a = normal[3];
        float[] hover = ColorField.draw("Hover", component.hoverR, component.hoverG, component.hoverB, component.hoverA);
        component.hoverR = hover[0];
        component.hoverG = hover[1];
        component.hoverB = hover[2];
        component.hoverA = hover[3];
        float[] pressed = ColorField.draw("Pressed", component.pressedR, component.pressedG, component.pressedB, component.pressedA);
        component.pressedR = pressed[0];
        component.pressedG = pressed[1];
        component.pressedB = pressed[2];
        component.pressedA = pressed[3];
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
