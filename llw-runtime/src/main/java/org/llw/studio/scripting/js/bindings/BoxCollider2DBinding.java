package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.BoxCollider2DComponent;

public final class BoxCollider2DBinding {
    private final ScriptContext context;
    private final EntityId entity;

    public BoxCollider2DBinding(ScriptContext context, EntityId entity) {
        this.context = context;
        this.entity = entity;
    }

    @HostAccess.Export
    public double getSizeX() {
        BoxCollider2DComponent c = component();
        return c == null ? 1d : c.sizeX;
    }

    @HostAccess.Export
    public void setSizeX(double value) {
        BoxCollider2DComponent c = component();
        if (c != null) {
            c.sizeX = (float) value;
        }
    }

    @HostAccess.Export
    public double getSizeY() {
        BoxCollider2DComponent c = component();
        return c == null ? 1d : c.sizeY;
    }

    @HostAccess.Export
    public void setSizeY(double value) {
        BoxCollider2DComponent c = component();
        if (c != null) {
            c.sizeY = (float) value;
        }
    }

    @HostAccess.Export
    public boolean getIsTrigger() {
        BoxCollider2DComponent c = component();
        return c != null && c.isTrigger;
    }

    @HostAccess.Export
    public void setIsTrigger(boolean trigger) {
        BoxCollider2DComponent c = component();
        if (c != null) {
            c.isTrigger = trigger;
        }
    }

    private BoxCollider2DComponent component() {
        return context.world().getComponent(entity, BoxCollider2DComponent.class);
    }
}
