package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.AssetReferenceField;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.AudioSourceComponent;

/** Inspector fields for {@link AudioSourceComponent}. */
public final class AudioSourceDrawer implements ComponentDrawer<AudioSourceComponent> {
    @Override
    public void draw(AudioSourceComponent component, InspectorContext context) {
        component.clipGuid = AssetReferenceField.draw("Clip", component.clipGuid, context.assets());
        component.volume = FloatField.draw("Volume", component.volume);
        component.playOnStart = BoolField.draw("Play On Start", component.playOnStart);
        context.markDirty();
    }
}
