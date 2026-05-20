package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.UILabelComponent;

/** Inspector fields for {@link UILabelComponent}. */
public final class UILabelDrawer implements ComponentDrawer<UILabelComponent> {
    private final ImString textBuffer = new ImString(512);

    @Override
    public void draw(UILabelComponent component, InspectorContext context) {
        textBuffer.set(component.text == null ? "" : component.text);
        if (ImGui.inputTextMultiline("Text", textBuffer, 120f, 60f)) {
            component.text = textBuffer.get();
            context.markDirty();
        }
        component.width = FloatField.draw("Width", component.width);
        component.height = FloatField.draw("Height", component.height);
        component.fontSize = (int) FloatField.draw("Font Size", component.fontSize);
        float[] color = ColorField.draw("Color", component.r, component.g, component.b, component.a);
        component.r = color[0];
        component.g = color[1];
        component.b = color[2];
        component.a = color[3];
        component.alignment = (int) FloatField.draw("Alignment", component.alignment);
        context.markDirty();
    }
}
