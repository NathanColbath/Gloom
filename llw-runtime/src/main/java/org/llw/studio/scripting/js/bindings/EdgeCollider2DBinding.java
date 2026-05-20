package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;

public final class EdgeCollider2DBinding {
    private final ScriptContext context;
    private final EntityId entity;

    public EdgeCollider2DBinding(ScriptContext context, EntityId entity) {
        this.context = context;
        this.entity = entity;
    }

    @HostAccess.Export
    public boolean getIsTrigger() {
        EdgeCollider2DComponent c = component();
        return c != null && c.isTrigger;
    }

    @HostAccess.Export
    public void setIsTrigger(boolean trigger) {
        EdgeCollider2DComponent c = component();
        if (c != null) {
            c.isTrigger = trigger;
        }
    }

    private EdgeCollider2DComponent component() {
        return context.world().getComponent(entity, EdgeCollider2DComponent.class);
    }
}
