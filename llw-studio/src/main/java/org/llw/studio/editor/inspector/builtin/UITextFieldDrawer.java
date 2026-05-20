package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.UITextFieldComponent;

/** Inspector fields for {@link UITextFieldComponent}. */
public final class UITextFieldDrawer implements ComponentDrawer<UITextFieldComponent> {
    private final ImString valueBuffer = new ImString(512);
    private final ImString placeholderBuffer = new ImString(256);

    @Override
    public void draw(UITextFieldComponent component, InspectorContext context) {
        valueBuffer.set(component.value == null ? "" : component.value);
        if (ImGui.inputText("Value", valueBuffer)) {
            component.value = valueBuffer.get();
            context.markDirty();
        }
        placeholderBuffer.set(component.placeholder == null ? "" : component.placeholder);
        if (ImGui.inputText("Placeholder", placeholderBuffer)) {
            component.placeholder = placeholderBuffer.get();
            context.markDirty();
        }
        component.width = FloatField.draw("Width", component.width);
        component.height = FloatField.draw("Height", component.height);
        component.fontSize = (int) FloatField.draw("Font Size", component.fontSize);
        component.maxLength = (int) FloatField.draw("Max Length", component.maxLength);
        float[] background = ColorField.draw("Background", component.r, component.g, component.b, component.a);
        component.r = background[0];
        component.g = background[1];
        component.b = background[2];
        component.a = background[3];
        float[] border = ColorField.draw("Border", component.borderR, component.borderG, component.borderB, component.borderA);
        component.borderR = border[0];
        component.borderG = border[1];
        component.borderB = border[2];
        component.borderA = border[3];
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
