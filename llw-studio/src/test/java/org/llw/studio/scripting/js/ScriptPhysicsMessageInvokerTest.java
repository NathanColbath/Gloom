package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.physics.PhysicsContactEvent;
import org.llw.studio.physics.PhysicsMessageType;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPhysicsMessageInvokerTest {
    @Test
    void invokesOnCollisionEnterAliasWithoutTwoDSuffix() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, java.nio.file.Path.of("."));
            hostApi.install();
            Value factory = context.eval("js", """
                    (function (LLW) {
                      const Script = LLW.Script;
                      return class Test extends Script {
                        onCollisionEnter(collision) {
                          globalThis.__hit = collision.getRelativeVelocityX();
                        }
                      };
                    })
                    """);
            Value host = hostApi.createHost(hostApi.scriptContext(), EntityId.none(), true);
            Value instance = factory.execute(context.getBindings("js").getMember("LLW")).newInstance(host);

            PhysicsContactEvent event = new PhysicsContactEvent(
                    PhysicsMessageType.COLLISION_ENTER,
                    EntityId.none(),
                    EntityId.none(),
                    false,
                    7.5f,
                    0f
            );
            ScriptPhysicsMessageInvoker.invoke(hostApi, instance, "Test", event);
            assertEquals(7.5, context.eval("js", "globalThis.__hit").asDouble(), 0.001);
        }
    }

    @Test
    void prefersTwoDSuffixWhenBothAliasesExist() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, java.nio.file.Path.of("."));
            hostApi.install();
            Value factory = context.eval("js", """
                    (function (LLW) {
                      const Script = LLW.Script;
                      return class Test extends Script {
                        onCollisionEnter2D() { globalThis.__twoD = true; }
                        onCollisionEnter() { globalThis.__plain = true; }
                      };
                    })
                    """);
            Value host = hostApi.createHost(hostApi.scriptContext(), EntityId.none(), true);
            Value instance = factory.execute(context.getBindings("js").getMember("LLW")).newInstance(host);

            ScriptPhysicsMessageInvoker.invoke(hostApi, instance, "Test", new PhysicsContactEvent(
                    PhysicsMessageType.COLLISION_ENTER,
                    EntityId.none(),
                    EntityId.none(),
                    false,
                    0f,
                    0f
            ));
            assertTrue(context.eval("js", "globalThis.__twoD === true").asBoolean());
            assertTrue(context.eval("js", "globalThis.__plain !== true").asBoolean());
        }
    }
}
