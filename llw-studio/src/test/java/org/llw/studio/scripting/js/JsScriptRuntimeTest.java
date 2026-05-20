package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;
import org.llw.studio.scripting.ScriptFieldSchema;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.js.ScriptFieldApplicator;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsScriptRuntimeTest {
    @Test
    void bundledFactoryFunctionReturnsInstantiableClass() {
        Scene scene = new Scene();
        GameObject player = scene.createGameObject("Player");
        EntityId entity = player.entity();
        Transform2DComponent transform = player.transform();
        transform.x = 10f;
        transform.y = 20f;
        ScriptAttachment attachment = ScriptTestSupport.first(ScriptTestSupport.single("test-guid"));

        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();

            Value factory = context.eval("js", """
                    (function (LLW) {
                      const Script = LLW.Script;
                      return class Player extends Script {
                        update() {
                          this.transform.position.x += 1;
                          this.transform.position.y += 2;
                        }
                      };
                    })
                    """);

            assertDoesNotThrow(() -> JsScriptInstance.create(
                    context, hostApi, entity, attachment, "Player", factory
            ).update());
            assertEquals(11f, transform.x, 0.001f);
            assertEquals(22f, transform.y, 0.001f);
        }
    }

    @Test
    void bareScriptClassCanStillBeInstantiated() {
        Scene scene = new Scene();
        GameObject player = scene.createGameObject("Player");
        EntityId entity = player.entity();
        ScriptAttachment attachment = ScriptTestSupport.first(ScriptTestSupport.single("test-guid"));

        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();

            Value factory = context.eval("js", """
                    (function (LLW) {
                      const Script = LLW.Script;
                      return class Player extends Script {
                        start() {}
                      };
                    })
                    """).execute(context.getBindings("js").getMember("LLW"));

            assertDoesNotThrow(() -> JsScriptInstance.create(
                    context, hostApi, entity, attachment, "Player", factory
            ).start());
        }
    }

    @Test
    void sceneAndMathBindingsAreInstalled() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();
            assertNotNull(context.getBindings("js").getMember("Scene"));
            assertNotNull(context.getBindings("js").getMember("Math"));
            assertNotNull(context.getBindings("js").getMember("Vec2"));
        }
    }

    @Test
    void vec2HelpersWorkFromScript() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();
            Value result = context.eval("js", "Vec2.create(3, 4).length()");
            assertEquals(5d, result.asDouble(), 0.001d);
        }
    }

    @Test
    void vec2CreateExposesXYProperties() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();
            Value x = context.eval("js", "Vec2.create(3, 4).x");
            Value y = context.eval("js", "Vec2.create(3, 4).y");
            assertEquals(3d, x.asDouble(), 0.001d);
            assertEquals(4d, y.asDouble(), 0.001d);
        }
    }

    @Test
    void vector2InspectorFieldKeepsVec2Methods() {
        Scene scene = new Scene();
        ObjectMapper mapper = new ObjectMapper();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();
            Value instance = context.eval("js", "({})");
            ScriptAttachment attachment = ScriptTestSupport.first(ScriptTestSupport.single("test-guid"));
            attachment.fields.put("moveVector", mapper.createObjectNode().put("x", 1).put("y", 0));
            ScriptSchema schema = new ScriptSchema(java.util.List.of(
                    new ScriptFieldSchema("moveVector", "vector2", mapper.createObjectNode().put("x", 1).put("y", 0))
            ));
            ScriptFieldApplicator.applySerializedFields(
                    instance, hostApi, hostApi.scriptContext().world(), attachment, schema);
            context.getBindings("js").putMember("instance", instance);
            assertEquals(2d, context.eval("js", """
                    (() => {
                      const v = instance.moveVector;
                      v.mul(2);
                      return v.x;
                    })()
                    """).asDouble(), 0.001d);
        }
    }

    @Test
    void vec2IsMutableAndMutatesInPlace() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();
            assertEquals(4d, context.eval("js", """
                    (() => {
                      const v = Vec2.create(1, 0);
                      v.x = 3;
                      v.y = 4;
                      v.add(1, 1);
                      return v.x;
                    })()
                    """).asDouble(), 0.001d);
            assertEquals(5d, context.eval("js", """
                    (() => {
                      const v = Vec2.create(3, 4);
                      v.add(1, 1);
                      return v.y;
                    })()
                    """).asDouble(), 0.001d);
            assertEquals(5d, context.eval("js", "Vec2.create(3, 4).length()").asDouble(), 0.001d);
            assertEquals(4d, context.eval("js", """
                    (() => {
                      const v = Vec2.create(4, 5);
                      const c = v.clone();
                      c.x = 0;
                      return v.x;
                    })()
                    """).asDouble(), 0.001d);
        }
    }

    @Test
    void entityProxyExposesSceneId() {
        Scene scene = new Scene();
        GameObject target = scene.createGameObject("Target");
        org.llw.studio.scene.SceneObjectIds.assign(scene, target.entity(), 3);
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();
            Value proxy = hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), target.entity()));
            context.getBindings("js").putMember("entity", proxy);
            assertEquals(3, context.eval("js", "entity.sceneId").asInt());
        }
    }

    @Test
    void entityTransformIsReadableFromScript() {
        Scene scene = new Scene();
        GameObject player = scene.createGameObject("Player");
        player.transform().x = 4f;
        player.transform().y = 6f;
        GameObject target = scene.createGameObject("Target");
        target.transform().x = 10f;
        target.transform().y = 20f;
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();
            Value targetProxy = hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), target.entity()));
            context.getBindings("js").putMember("target", targetProxy);
            Value x = context.eval("js", "target.transform.position.x");
            Value y = context.eval("js", "target.transform.position.y");
            assertEquals(10d, x.asDouble(), 0.001d);
            assertEquals(20d, y.asDouble(), 0.001d);
        }
    }

    @Test
    void resolvesEsbuildBundleDefaultExport() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();
            context.eval("js", """
                    var __LLWScriptBundle = (() => {
                      return { default: class Player {} };
                    })();
                    """);
            Value factory = GraalScriptRuntime.resolveFactory(null, context.getBindings("js"));
            assertNotNull(factory);
            assertTrue(factory.canInstantiate());
        }
    }

    @Test
    void entityFindByNameWorksFromScript() {
        Scene scene = new Scene();
        scene.createGameObject("Enemy");
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();
            Value found = context.eval("js", "Scene.findByName('Enemy') !== null");
            assertTrue(found.asBoolean());
        }
    }

    @Test
    void destroyEntityAcceptsJsEntityProxy() {
        Scene scene = new Scene();
        GameObject bullet = scene.createGameObject("Bullet");
        org.llw.studio.scene.SceneObjectIds.assign(scene, bullet.entity(), 7);
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();
            Value proxy = hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), bullet.entity()));
            context.getBindings("js").putMember("bullet", proxy);
            context.eval("js", "Scene.destroyEntity(bullet)");
            scene.world().commandBuffer().flush(scene.world());
            assertFalse(scene.world().isAlive(bullet.entity()));
        }
    }

    @Test
    void prefabTemplateProxyRoundTripsThroughCreateEntity() throws Exception {
        java.nio.file.Path projectRoot = java.nio.file.Files.createTempDirectory("llw-prefab-spawn");
        java.nio.file.Path assetsDir = projectRoot.resolve("Assets");
        java.nio.file.Files.createDirectories(assetsDir);

        Scene templateScene = new Scene();
        GameObject bulletTemplate = templateScene.createGameObject("Bullet");
        bulletTemplate.setTag("Bullet");
        org.llw.studio.serialization.PrefabSerializer.saveSubtree(
                templateScene,
                bulletTemplate.entity(),
                assetsDir.resolve("Bullet.prefab.json")
        );

        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, projectRoot);
            hostApi.install();
            Value prefab = hostApi.wrapPrefabTemplate("Assets/Bullet.prefab.json");
            context.getBindings("js").putMember("bulletPrefab", prefab);
            assertEquals("Assets/Bullet.prefab.json", context.eval("js", "bulletPrefab.prefabGuid").asString());
            Value spawned = context.eval("js", """
                    (() => {
                      const spawned = Scene.createEntity(bulletPrefab);
                      return spawned == null ? null : spawned.tag;
                    })()
                    """);
            assertNotNull(spawned);
            assertFalse(spawned.isNull());
            assertEquals("Bullet", spawned.asString());
        }
    }
}
