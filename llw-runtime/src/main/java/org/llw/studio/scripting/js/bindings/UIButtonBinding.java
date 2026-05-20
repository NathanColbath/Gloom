package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.UIButtonComponent;

/** Play-mode {@link UIButtonComponent} host binding. */
public final class UIButtonBinding {
    private final World world;
    private final EntityId entity;

    public UIButtonBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getLabel() {
        UIButtonComponent button = component();
        return button == null ? "" : button.label;
    }

    @HostAccess.Export
    public void setLabel(String label) {
        UIButtonComponent button = component();
        if (button != null) {
            button.label = label == null ? "" : label;
        }
    }

    @HostAccess.Export
    public boolean isInteractable() {
        UIButtonComponent button = component();
        return button != null && button.interactable;
    }

    @HostAccess.Export
    public void setInteractable(boolean interactable) {
        UIButtonComponent button = component();
        if (button != null) {
            button.interactable = interactable;
        }
    }

    @HostAccess.Export
    public boolean isHovered() {
        UIButtonComponent button = component();
        return button != null && button.hovered;
    }

    @HostAccess.Export
    public boolean isPressed() {
        UIButtonComponent button = component();
        return button != null && button.pressed;
    }

    @HostAccess.Export
    public boolean isClicked() {
        UIButtonComponent button = component();
        return button != null && button.clickedThisFrame;
    }

    private UIButtonComponent component() {
        return world.getComponent(entity, UIButtonComponent.class);
    }
}
