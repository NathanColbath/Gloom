package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.panels.AnimationPanel;
import org.llw.studio.editor.widgets.fields.AnimationReferenceField;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.Animation2DComponent;

/** Inspector fields for {@link Animation2DComponent}. */
public final class Animation2DDrawer implements ComponentDrawer<Animation2DComponent> {
    @Override
    public void draw(Animation2DComponent component, InspectorContext context) {
        component.animationGuid = AnimationReferenceField.draw("Animation", component.animationGuid, context.assets());
        drawStateDropdown(component, context);
        component.playOnStart = BoolField.draw("Play On Start", component.playOnStart);
        component.speed = FloatField.draw("Speed", component.speed);
        component.loop = BoolField.draw("Loop", component.loop);
        AnimationPanel panel = context.editorSession().animationPanel();
        if (panel != null && ImGui.button("Open Animation Window")) {
            panel.openFromEntity(context.studioContext(), context.selection().selected(), component);
        }
        context.markDirty();
    }

    private static void drawStateDropdown(Animation2DComponent component, InspectorContext context) {
        if (component.animationGuid == null || component.animationGuid.isBlank()) {
            return;
        }
        AnimationSetDefinition set = context.assets().animationSet(component.animationGuid);
        if (set == null || set.states.isEmpty()) {
            ImGui.textDisabled("No states on animation asset.");
            return;
        }
        int current = 0;
        for (int i = 0; i < set.states.size(); i++) {
            if (set.states.get(i).name().equals(component.currentState)) {
                current = i;
                break;
            }
        }
        String[] labels = set.states.stream().map(AnimationStateDefinition::name).toArray(String[]::new);
        ImInt index = new ImInt(current);
        if (ImGui.combo("State", index, labels)) {
            component.currentState = labels[index.get()];
        }
        int defaultIndex = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(component.defaultState)) {
                defaultIndex = i;
                break;
            }
        }
        ImInt defIdx = new ImInt(defaultIndex);
        if (ImGui.combo("Default State", defIdx, labels)) {
            component.defaultState = labels[defIdx.get()];
        }
    }
}
