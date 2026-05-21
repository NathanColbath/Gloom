package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.editor.panels.ParticlePanel;
import org.llw.studio.editor.widgets.fields.ParticleSystemReferenceField;
import org.llw.studio.ecs.components.ParticleEmitterComponent;

/** Inspector fields for {@link ParticleEmitterComponent}. */
public final class ParticleEmitterDrawer implements ComponentDrawer<ParticleEmitterComponent> {
    @Override
    public void draw(ParticleEmitterComponent component, InspectorContext context) {
        ParticlePanel particlePanel = context.editorSession().particlePanel();
        component.particleSystemGuid = ParticleSystemReferenceField.draw(
                "Particle System",
                component.particleSystemGuid,
                context.assets(),
                particlePanel
        );
        component.playOnAwake = BoolField.draw("Play On Awake", component.playOnAwake);
        component.looping = BoolField.draw("Looping", component.looping);
        component.emitting = BoolField.draw("Emitting", component.emitting);
        component.sortingOrder = (int) FloatField.draw("Sorting Order", component.sortingOrder);
        context.markDirty();
    }
}
