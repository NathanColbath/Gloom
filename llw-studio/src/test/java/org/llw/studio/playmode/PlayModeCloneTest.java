package org.llw.studio.playmode;

import org.junit.jupiter.api.Test;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;

import static org.junit.jupiter.api.Assertions.*;

class PlayModeCloneTest {
    @Test
    void copiesSceneObjectIdAndScriptFields() {
        var edit = new org.llw.studio.scene.Scene();
        var player = edit.createGameObject("Player");
        SceneObjectIds.assign(edit, player.entity(), 5);
        var script = ScriptTestSupport.single("abc");
        ScriptTestSupport.first(script).setNumberField("speed", 3);
        player.addComponent(ScriptComponent.class, script);

        var runner = new PlayModeRunner();
        PlayModeRunner.PlayPrepareResult prepared = runner.prepareScene(
                edit,
                java.nio.file.Path.of("."),
                null,
                null
        );
        var play = runner.activate(prepared, null, null);

        assertEquals(5, SceneObjectIds.get(play.world(), runner.playEntityForSceneId(5)));
        ScriptComponent playScript = play.world().getComponent(runner.playEntityForSceneId(5), ScriptComponent.class);
        assertNotNull(playScript);
        assertEquals(3, ScriptTestSupport.first(playScript).field("speed").asDouble(), 0.001);
        runner.stop();
    }
}
