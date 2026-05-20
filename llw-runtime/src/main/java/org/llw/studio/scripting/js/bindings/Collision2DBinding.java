package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.physics.PhysicsContactEvent;

/**
 * Unity {@code Collision2D} message payload.
 */
public final class Collision2DBinding {
    private final ScriptHostApi hostApi;
    private final PhysicsContactEvent event;

    public Collision2DBinding(ScriptHostApi hostApi, PhysicsContactEvent event) {
        this.hostApi = hostApi;
        this.event = event;
    }

    @HostAccess.Export
    public Object getOther() {
        if (event.other().isNone()) {
            return null;
        }
        return hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), event.other()));
    }

    @HostAccess.Export
    public double getRelativeVelocityX() {
        return event.relativeVelocityX();
    }

    @HostAccess.Export
    public double getRelativeVelocityY() {
        return event.relativeVelocityY();
    }
}
