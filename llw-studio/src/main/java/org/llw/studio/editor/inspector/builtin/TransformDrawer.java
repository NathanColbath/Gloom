package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.editor.widgets.fields.Vector2Field;
import org.llw.studio.ecs.components.Transform2DComponent;

/**
 * Inspector fields for {@link Transform2DComponent} (position, rotation in degrees, scale).
 */
public final class TransformDrawer implements ComponentDrawer<Transform2DComponent> {
    @Override
    public void draw(Transform2DComponent component, InspectorContext context) {
        float[] pos = Vector2Field.draw("Position", component.x, component.y);
        component.x = pos[0];
        component.y = pos[1];
        component.rotation = FloatField.draw("Rotation", component.rotation);
        float[] scale = Vector2Field.draw("Scale", component.scaleX, component.scaleY);
        component.scaleX = scale[0];
        component.scaleY = scale[1];
        context.markDirty();
    }
}
