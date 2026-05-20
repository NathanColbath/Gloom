package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.UICanvasComponent;
import imgui.ImGui;

/** Inspector fields for {@link UICanvasComponent}. */
public final class UICanvasDrawer implements ComponentDrawer<UICanvasComponent> {
    @Override
    public void draw(UICanvasComponent component, InspectorContext context) {
        component.sortingOrder = (int) FloatField.draw("Sorting Order", component.sortingOrder);
        if (ImGui.checkbox("Enabled", component.enabled)) {
            context.markDirty();
        }
        context.markDirty();
    }
}
