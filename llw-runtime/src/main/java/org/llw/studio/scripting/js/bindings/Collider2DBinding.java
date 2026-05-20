package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;

/**
 * Unity {@code Collider2D} reference for trigger messages.
 */
public final class Collider2DBinding {
    private final ScriptHostApi hostApi;
    private final EntityId entity;

    public Collider2DBinding(ScriptHostApi hostApi, EntityId entity) {
        this.hostApi = hostApi;
        this.entity = entity;
    }

    @HostAccess.Export
    public Object getEntity() {
        return hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), entity));
    }

    @HostAccess.Export
    public boolean isTrigger() {
        return true;
    }
}
