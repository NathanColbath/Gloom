package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImBoolean;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.ecs.components.StaticLightmapContributor;

/** Inspector for {@link StaticLightmapContributor}. */
public final class StaticLightmapContributorDrawer implements ComponentDrawer<StaticLightmapContributor> {
    @Override
    public void draw(StaticLightmapContributor component, InspectorContext context) {
        ImBoolean enabled = new ImBoolean(component.enabled);
        if (ImGui.checkbox("Contribute To Bake", enabled)) {
            component.enabled = enabled.get();
        }
        context.markDirty();
    }
}
