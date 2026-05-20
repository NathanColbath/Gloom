package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.UIToggleComponent;

/** Play-mode {@link UIToggleComponent} host binding. */
public final class UIToggleBinding {
    private final World world;
    private final EntityId entity;

    public UIToggleBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getLabel() {
        UIToggleComponent toggle = component();
        return toggle == null ? "" : toggle.label;
    }

    @HostAccess.Export
    public void setLabel(String label) {
        UIToggleComponent toggle = component();
        if (toggle != null) {
            toggle.label = label == null ? "" : label;
        }
    }

    @HostAccess.Export
    public boolean isOn() {
        UIToggleComponent toggle = component();
        return toggle != null && toggle.isOn;
    }

    @HostAccess.Export
    public void setOn(boolean on) {
        UIToggleComponent toggle = component();
        if (toggle != null) {
            toggle.isOn = on;
        }
    }

    @HostAccess.Export
    public boolean isInteractable() {
        UIToggleComponent toggle = component();
        return toggle != null && toggle.interactable;
    }

    @HostAccess.Export
    public void setInteractable(boolean interactable) {
        UIToggleComponent toggle = component();
        if (toggle != null) {
            toggle.interactable = interactable;
        }
    }

    private UIToggleComponent component() {
        return world.getComponent(entity, UIToggleComponent.class);
    }
}
