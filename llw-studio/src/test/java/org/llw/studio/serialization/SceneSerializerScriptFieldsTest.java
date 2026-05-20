package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SceneSerializerScriptFieldsTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsScriptFieldsAndSceneIds() throws Exception {
        var scene = new org.llw.studio.scene.Scene();
        var player = scene.createGameObject("Player");
        SceneObjectIds.assign(scene, player.entity(), 0);
        var script = ScriptTestSupport.single("guid-1");
        ScriptAttachment attachment = ScriptTestSupport.first(script);
        attachment.setNumberField("speed", 7.5);
        attachment.setTextField("label", "Hero");
        attachment.setEntityField("target", 2);
        player.addComponent(ScriptComponent.class, script);

        var target = scene.createGameObject("Target");
        SceneObjectIds.assign(scene, target.entity(), 2);

        Path file = tempDir.resolve("scene.json");
        SceneSerializer.save(scene, file);
        var loadedScene = SceneSerializer.load(file);

        var loadedPlayer = loadedScene.rootObjects().stream()
                .filter(o -> "Player".equals(o.name()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, SceneObjectIds.get(loadedScene.world(), loadedPlayer.entity()));
        ScriptAttachment loadedAttachment = ScriptTestSupport.first(loadedPlayer.getComponent(ScriptComponent.class));
        assertNotNull(loadedAttachment);
        assertEquals(7.5, loadedAttachment.field("speed").asDouble(), 0.001);
        assertEquals("Hero", loadedAttachment.field("label").asText());
        assertEquals(2, loadedAttachment.entityFieldSceneId("target"));
    }

    @Test
    void roundTripsMultipleScripts() throws Exception {
        var scene = new org.llw.studio.scene.Scene();
        var player = scene.createGameObject("Player");
        ScriptComponent scripts = new ScriptComponent();
        ScriptAttachment a = scripts.addAttachment();
        a.scriptGuid = "guid-a";
        a.setNumberField("speed", 1);
        ScriptAttachment b = scripts.addAttachment();
        b.scriptGuid = "guid-b";
        b.setNumberField("speed", 2);
        player.addComponent(ScriptComponent.class, scripts);

        Path file = tempDir.resolve("multi.json");
        SceneSerializer.save(scene, file);
        var loaded = SceneSerializer.load(file);
        var loadedPlayer = loaded.rootObjects().stream()
                .filter(o -> "Player".equals(o.name()))
                .findFirst()
                .orElseThrow();
        ScriptComponent loadedScripts = loadedPlayer.getComponent(ScriptComponent.class);
        assertEquals(2, loadedScripts.attachments.size());
        assertEquals("guid-a", loadedScripts.findByGuid("guid-a").scriptGuid);
        assertEquals(2, loadedScripts.findByGuid("guid-b").field("speed").asInt());
    }

    @Test
    void readsLegacySingleScriptObject() throws Exception {
        var scene = new org.llw.studio.scene.Scene();
        var player = scene.createGameObject("Player");
        SceneObjectIds.assign(scene, player.entity(), 0);
        var script = ScriptTestSupport.single("legacy-guid");
        ScriptTestSupport.first(script).setNumberField("hp", 100);
        player.addComponent(ScriptComponent.class, script);

        Path file = tempDir.resolve("legacy.json");
        SceneSerializer.save(scene, file);
        var loadedScene = SceneSerializer.load(file);
        var loadedPlayer = loadedScene.rootObjects().stream()
                .filter(o -> "Player".equals(o.name()))
                .findFirst()
                .orElseThrow();
        ScriptAttachment loadedAttachment = ScriptTestSupport.first(loadedPlayer.getComponent(ScriptComponent.class));
        assertEquals("legacy-guid", loadedAttachment.scriptGuid);
        assertEquals(100, loadedAttachment.field("hp").asInt());
    }
}
