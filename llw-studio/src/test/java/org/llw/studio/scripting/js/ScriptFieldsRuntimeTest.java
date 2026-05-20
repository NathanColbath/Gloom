package org.llw.studio.scripting.js;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;
import org.llw.studio.scripting.ScriptFieldSchema;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScriptFieldsRuntimeTest {
    @Test
    void appliesSerializedFieldsBeforeStart() {
        Scene scene = new Scene();
        GameObject player = scene.createGameObject("Player");
        SceneObjectIds.assign(scene, player.entity(), 1);
        GameObject target = scene.createGameObject("Target");
        SceneObjectIds.assign(scene, target.entity(), 2);

        ScriptComponent script = ScriptTestSupport.single("test-guid");
        ScriptAttachment attachment = ScriptTestSupport.first(script);
        attachment.setNumberField("speed", 9);
        attachment.setTextField("label", "Hero");
        attachment.setEntityField("target", 2);
        player.addComponent(ScriptComponent.class, script);

        ScriptSchema schema = new ScriptSchema(List.of(
                new ScriptFieldSchema("speed", "number", JsonNodeFactory.instance.numberNode(0)),
                new ScriptFieldSchema("label", "string", JsonNodeFactory.instance.textNode("")),
                new ScriptFieldSchema("target", "entity", JsonNodeFactory.instance.nullNode())
        ));

        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            ScriptHostApi hostApi = new ScriptHostApi(context, null, scene, null, Path.of("."));
            hostApi.install();

            Value factory = context.eval("js", """
                    (function (LLW) {
                      const Script = LLW.Script;
                      return class Player extends Script {
                        speed = 0;
                        label = "";
                        target = null;
                        start() {
                          if (this.speed !== 9) throw new Error("bad speed");
                          if (this.label !== "Hero") throw new Error("bad label");
                          if (!this.target) throw new Error("missing target");
                        }
                      };
                    })
                    """);

            assertDoesNotThrow(() -> {
                Value llw = context.getBindings("js").getMember("LLW");
                Value scriptClass = factory.execute(llw);
                Value host = hostApi.createHost(hostApi.scriptContext(), player.entity(), attachment.enabled);
                Value created = scriptClass.newInstance(host);
                ScriptFieldApplicator.applySerializedFields(
                        created, hostApi, scene.world(), attachment, schema);
                created.invokeMember("start");
            });
        }
    }
}
