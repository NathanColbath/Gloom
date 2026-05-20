package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.ecs.components.ActiveComponent;

/** Inspector fields for {@link ActiveComponent}. */
public final class ActiveDrawer implements ComponentDrawer<ActiveComponent> {
    @Override
    public void draw(ActiveComponent component, InspectorContext context) {
        component.selfActive = BoolField.draw("Active", component.selfActive);
        context.markDirty();
    }
}
