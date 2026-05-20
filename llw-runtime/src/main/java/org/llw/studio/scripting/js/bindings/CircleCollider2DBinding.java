package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.CircleCollider2DComponent;

public final class CircleCollider2DBinding {
    private final ScriptContext context;
    private final EntityId entity;

    public CircleCollider2DBinding(ScriptContext context, EntityId entity) {
        this.context = context;
        this.entity = entity;
    }

    @HostAccess.Export
    public double getRadius() {
        CircleCollider2DComponent c = component();
        return c == null ? 0.5d : c.radius;
    }

    @HostAccess.Export
    public void setRadius(double radius) {
        CircleCollider2DComponent c = component();
        if (c != null) {
            c.radius = (float) radius;
        }
    }

    @HostAccess.Export
    public boolean getIsTrigger() {
        CircleCollider2DComponent c = component();
        return c != null && c.isTrigger;
    }

    @HostAccess.Export
    public void setIsTrigger(boolean trigger) {
        CircleCollider2DComponent c = component();
        if (c != null) {
            c.isTrigger = trigger;
        }
    }

    private CircleCollider2DComponent component() {
        return context.world().getComponent(entity, CircleCollider2DComponent.class);
    }
}
