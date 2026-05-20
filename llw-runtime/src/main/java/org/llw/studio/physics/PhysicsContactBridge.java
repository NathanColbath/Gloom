package org.llw.studio.physics;

import org.llw.studio.systems.JsScriptSystem;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dispatches queued physics contact events to script instances.
 */
public final class PhysicsContactBridge {
    private Consumer<PhysicsContactEvent> dispatcher = event -> {};

    public void setDispatcher(Consumer<PhysicsContactEvent> dispatcher) {
        this.dispatcher = dispatcher == null ? event -> {} : dispatcher;
    }

    public void flush(PhysicsWorld world) {
        if (world == null) {
            return;
        }
        List<PhysicsContactEvent> events = world.drainContactEvents();
        for (PhysicsContactEvent event : events) {
            dispatcher.accept(event);
        }
    }

    public static void wireScriptSystem(JsScriptSystem scriptSystem) {
        // wired from PlayModeRunner when script system is created
    }
}
