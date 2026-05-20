package org.llw.studio.prefab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;
import org.llw.studio.serialization.PrefabSerializer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrefabInstantiatorTest {
    @TempDir
    Path projectRoot;

    @Test
    void instantiateAssignsNewSceneIdsAndRemapsScriptRefs() throws Exception {
        Path assetsDir = projectRoot.resolve("Assets");
        Files.createDirectories(assetsDir);

        Scene source = new Scene();
        GameObject root = source.createGameObject("Root");
        GameObject target = source.createGameObject("Target");
        target.setParent(root, false);

        ScriptComponent script = ScriptTestSupport.single("script-guid");
        ScriptTestSupport.first(script).setEntityField("ally", SceneObjectIds.get(source.world(), target.entity()));
        root.addComponent(ScriptComponent.class, script);

        SpriteRendererComponent sprite = new SpriteRendererComponent();
        sprite.textureGuid = "tex";
        root.addComponent(SpriteRendererComponent.class, sprite);

        Path prefabPath = assetsDir.resolve("Root.prefab.json");
        PrefabSerializer.saveSubtree(source, root.entity(), prefabPath);

        Scene scene = new Scene();
        GameObject instance = PrefabInstantiator.instantiate(
                scene, null, projectRoot, "Assets/Root.prefab.json", null, 12f, 34f, true, null);

        assertNotNull(instance);
        assertEquals(12f, instance.transform().x);
        assertEquals(34f, instance.transform().y);
        assertEquals("tex", instance.getComponent(SpriteRendererComponent.class).textureGuid);

        int newTargetSceneId = -1;
        for (GameObject child : instance.children()) {
            if ("Target".equals(child.name())) {
                newTargetSceneId = SceneObjectIds.get(scene.world(), child.entity());
            }
        }
        assertEquals(newTargetSceneId, ScriptTestSupport.first(instance.getComponent(ScriptComponent.class)).entityFieldSceneId("ally"));
        assertNotEquals(-1, newTargetSceneId);
    }

    @Test
    void instantiatePreservesPrefabTemplateField() throws Exception {
        Path assetsDir = projectRoot.resolve("Assets");
        Files.createDirectories(assetsDir);

        Scene source = new Scene();
        GameObject root = source.createGameObject("Spawner");
        ScriptComponent script = ScriptTestSupport.single("script-guid");
        ScriptTestSupport.first(script).setPrefabField("template", "other-prefab-guid");
        root.addComponent(ScriptComponent.class, script);

        Path prefabPath = assetsDir.resolve("Spawner.prefab.json");
        PrefabSerializer.saveSubtree(source, root.entity(), prefabPath);

        Scene scene = new Scene();
        GameObject instance = PrefabInstantiator.instantiate(
                scene, null, projectRoot, "Assets/Spawner.prefab.json", null, 0f, 0f, false, null);

        assertNotNull(instance);
        assertEquals("other-prefab-guid", ScriptTestSupport.first(instance.getComponent(ScriptComponent.class)).entityFieldPrefabGuid("template"));
    }
}
