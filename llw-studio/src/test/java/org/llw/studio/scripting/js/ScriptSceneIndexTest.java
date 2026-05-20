package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptSceneIndexTest {
    @Test
    void collectsUniqueScriptGuidsFromScene() {
        Scene scene = new Scene();
        ScriptComponent shared = ScriptTestSupport.single("guid-a");
        scene.createGameObject("One").addComponent(ScriptComponent.class, shared.copy());
        scene.createGameObject("Two").addComponent(ScriptComponent.class, shared.copy());

        scene.createGameObject("Three").addComponent(ScriptComponent.class, ScriptTestSupport.single("guid-b"));

        var guids = ScriptSceneIndex.collectGuids(scene);
        assertEquals(2, guids.size());
        assertTrue(guids.contains("guid-a"));
        assertTrue(guids.contains("guid-b"));
    }

    @Test
    void ignoresBlankScriptGuids() {
        Scene scene = new Scene();
        ScriptComponent empty = new ScriptComponent();
        empty.addAttachment();
        scene.createGameObject("Empty").addComponent(ScriptComponent.class, empty);
        assertTrue(ScriptSceneIndex.collectGuids(scene).isEmpty());
    }
}
