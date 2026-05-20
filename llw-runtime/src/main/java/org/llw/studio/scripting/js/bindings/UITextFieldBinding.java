package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ui.PlayUiInputBridge;

/** Play-mode {@link UITextFieldComponent} host binding. */
public final class UITextFieldBinding {
    private final World world;
    private final EntityId entity;

    public UITextFieldBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getValue() {
        UITextFieldComponent field = component();
        return field == null ? "" : field.value;
    }

    @HostAccess.Export
    public void setValue(String value) {
        UITextFieldComponent field = component();
        if (field != null) {
            field.value = value == null ? "" : value;
        }
    }

    @HostAccess.Export
    public boolean isFocused() {
        UITextFieldComponent field = component();
        return field != null && field.focused;
    }

    @HostAccess.Export
    public void setFocus(boolean focus) {
        if (focus) {
            PlayUiInputBridge.setFocusedTextField(entity);
        } else if (entity.equals(PlayUiInputBridge.focusedTextField())) {
            PlayUiInputBridge.setFocusedTextField(EntityId.none());
        }
    }

    @HostAccess.Export
    public boolean isInteractable() {
        UITextFieldComponent field = component();
        return field != null && field.interactable;
    }

    @HostAccess.Export
    public void setInteractable(boolean interactable) {
        UITextFieldComponent field = component();
        if (field != null) {
            field.interactable = interactable;
        }
    }

    private UITextFieldComponent component() {
        return world.getComponent(entity, UITextFieldComponent.class);
    }
}
