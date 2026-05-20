package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.UILabelComponent;

/** Play-mode {@link UILabelComponent} host binding. */
public final class UILabelBinding {
    private final World world;
    private final EntityId entity;

    public UILabelBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getText() {
        UILabelComponent label = component();
        return label == null ? "" : label.text;
    }

    @HostAccess.Export
    public void setText(String text) {
        UILabelComponent label = component();
        if (label != null) {
            label.text = text == null ? "" : text;
        }
    }

    private UILabelComponent component() {
        return world.getComponent(entity, UILabelComponent.class);
    }
}
