package org.llw.studio.serialization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.prefab.PrefabInstantiator;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptTestSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrefabSerializerTest {
    @TempDir
    Path tempDir;

    @Test
    void saveSubtreeIncludesChildren() throws Exception {
        Scene scene = new Scene();
        GameObject root = scene.createGameObject("Enemy");
        root.transform().x = 5f;
        GameObject child = scene.createGameObject("Gun");
        child.setParent(root, false);
        child.transform().x = 10f;

        Path path = tempDir.resolve("Enemy.prefab.json");
        PrefabSerializer.saveSubtree(scene, root.entity(), path);

        PrefabSerializer.PrefabData data = PrefabSerializer.load(path);
        int count = 0;
        for (var ignored : data.objectNodes()) {
            count++;
        }
        assertEquals(2, count);
        assertEquals(1, data.exportRootId());
    }

    @Test
    void loadPreservesComponents() throws Exception {
        Scene scene = new Scene();
        GameObject root = scene.createGameObject("Root");
        SpriteRendererComponent sprite = new SpriteRendererComponent();
        sprite.textureGuid = "tex-guid";
        root.addComponent(SpriteRendererComponent.class, sprite);
        root.addComponent(ScriptComponent.class, ScriptTestSupport.single("script-guid"));

        Path path = tempDir.resolve("Root.prefab.json");
        PrefabSerializer.saveSubtree(scene, root.entity(), path);

        Scene loadedScene = new Scene();
        PrefabSerializer.PrefabData data = PrefabSerializer.load(path);
        GameObject loaded = null;
        for (var node : data.objectNodes()) {
            loaded = SceneObjectSerializer.readObject(loadedScene, node, node.path("id").asInt());
        }
        assertNotNull(loaded);
        assertEquals("tex-guid", loaded.getComponent(SpriteRendererComponent.class).textureGuid);
        assertEquals("script-guid", ScriptTestSupport.first(loaded.getComponent(ScriptComponent.class)).scriptGuid);
    }

    @Test
    void roundTripPreservesAllComponents() throws Exception {
        Scene scene = new Scene();
        GameObject root = scene.createGameObject("Root");
        root.getComponent(ActiveComponent.class).selfActive = false;
        root.transform().x = 1f;
        root.transform().y = 2f;
        root.transform().rotation = 45f;
        root.transform().scaleX = 2f;
        root.transform().scaleY = 3f;

        GameObject child = scene.createGameObject("Child");
        child.setParent(root, false);

        SpriteRendererComponent sprite = new SpriteRendererComponent();
        sprite.textureGuid = "tex-guid";
        sprite.sortingOrder = 3;
        sprite.r = 0.1f;
        sprite.g = 0.2f;
        sprite.b = 0.3f;
        sprite.a = 0.4f;
        root.addComponent(SpriteRendererComponent.class, sprite);

        Camera2DComponent camera = new Camera2DComponent();
        camera.orthographicSize = 400f;
        camera.depth = -2f;
        camera.mainCamera = false;
        camera.backgroundR = 0.5f;
        camera.backgroundG = 0.6f;
        camera.backgroundB = 0.7f;
        camera.backgroundA = 0.8f;
        root.addComponent(Camera2DComponent.class, camera);

        AudioSourceComponent audio = new AudioSourceComponent();
        audio.clipGuid = "audio-guid";
        audio.volume = 0.75f;
        audio.playOnStart = true;
        root.addComponent(AudioSourceComponent.class, audio);

        ScriptComponent script = ScriptTestSupport.single("script-guid");
        ScriptAttachment attachment = ScriptTestSupport.first(script);
        attachment.scriptClassName = "LegacyClass";
        attachment.enabled = false;
        attachment.setNumberField("speed", 9.5);
        attachment.setBooleanField("armed", true);
        attachment.setTextField("label", "hello");
        attachment.setEntityField("ally", SceneObjectIds.get(scene.world(), child.entity()));
        attachment.setPrefabField("spawn", "prefab-template-guid");
        root.addComponent(ScriptComponent.class, script);

        Path path = tempDir.resolve("Full.prefab.json");
        PrefabSerializer.saveSubtree(scene, root.entity(), path);

        Scene loadedScene = new Scene();
        PrefabSerializer.PrefabData data = PrefabSerializer.load(path);
        Map<Integer, GameObject> byPrefabId = new HashMap<>();
        Map<Integer, Integer> idRemap = new HashMap<>();
        for (var objectNode : data.objectNodes()) {
            int prefabId = objectNode.path("id").asInt();
            int sceneId = SceneObjectIds.allocate(loadedScene);
            GameObject object = SceneObjectSerializer.readObject(loadedScene, objectNode, sceneId);
            byPrefabId.put(prefabId, object);
            idRemap.put(prefabId, sceneId);
        }
        SceneObjectSerializer.remapScriptEntityRefs(byPrefabId, idRemap);
        SceneObjectSerializer.wireParents(data.objectNodes(), byPrefabId);

        GameObject loadedRoot = byPrefabId.get(data.exportRootId());
        assertNotNull(loadedRoot);
        assertFalse(loadedRoot.getComponent(ActiveComponent.class).selfActive);
        assertEquals(1f, loadedRoot.transform().x);
        assertEquals(2f, loadedRoot.transform().y);
        assertEquals(45f, loadedRoot.transform().rotation);
        assertEquals(2f, loadedRoot.transform().scaleX);
        assertEquals(3f, loadedRoot.transform().scaleY);
        assertEquals(1, loadedRoot.children().size());

        SpriteRendererComponent loadedSprite = loadedRoot.getComponent(SpriteRendererComponent.class);
        assertEquals("tex-guid", loadedSprite.textureGuid);
        assertEquals(3, loadedSprite.sortingOrder);
        assertEquals(0.1f, loadedSprite.r);
        assertEquals(0.2f, loadedSprite.g);
        assertEquals(0.3f, loadedSprite.b);
        assertEquals(0.4f, loadedSprite.a);

        Camera2DComponent loadedCamera = loadedRoot.getComponent(Camera2DComponent.class);
        assertEquals(400f, loadedCamera.orthographicSize);
        assertEquals(-2f, loadedCamera.depth);
        assertFalse(loadedCamera.mainCamera);
        assertEquals(0.5f, loadedCamera.backgroundR);
        assertEquals(0.6f, loadedCamera.backgroundG);
        assertEquals(0.7f, loadedCamera.backgroundB);
        assertEquals(0.8f, loadedCamera.backgroundA);

        AudioSourceComponent loadedAudio = loadedRoot.getComponent(AudioSourceComponent.class);
        assertEquals("audio-guid", loadedAudio.clipGuid);
        assertEquals(0.75f, loadedAudio.volume);
        assertTrue(loadedAudio.playOnStart);

        ScriptAttachment loadedScript = ScriptTestSupport.first(loadedRoot.getComponent(ScriptComponent.class));
        assertEquals("script-guid", loadedScript.scriptGuid);
        assertEquals("LegacyClass", loadedScript.scriptClassName);
        assertFalse(loadedScript.enabled);
        assertEquals(9.5, loadedScript.field("speed").asDouble(), 0.001);
        assertTrue(loadedScript.field("armed").asBoolean());
        assertEquals("hello", loadedScript.field("label").asText());
        GameObject loadedChild = loadedRoot.children().get(0);
        assertEquals(
                SceneObjectIds.get(loadedScene.world(), loadedChild.entity()),
                loadedScript.entityFieldSceneId("ally")
        );
        assertEquals("prefab-template-guid", loadedScript.entityFieldPrefabGuid("spawn"));
    }

    @Test
    void saveSubtreeInstantiatesWithMatchingHierarchy() throws Exception {
        Path assetsDir = tempDir.resolve("Assets");
        Files.createDirectories(assetsDir);

        Scene scene = new Scene();
        GameObject root = scene.createGameObject("Enemy");
        GameObject gun = scene.createGameObject("Gun");
        gun.setParent(root, false);
        gun.transform().x = 7f;

        Path prefabPath = assetsDir.resolve("Enemy.prefab.json");
        PrefabSerializer.saveSubtree(scene, root.entity(), prefabPath);

        Scene instanceScene = new Scene();
        GameObject instance = PrefabInstantiator.instantiate(
                instanceScene, null, tempDir, "Assets/Enemy.prefab.json", null, 0f, 0f, false, null);

        assertNotNull(instance);
        assertEquals("Enemy", instance.name());
        assertEquals(1, instance.children().size());
        assertEquals("Gun", instance.children().get(0).name());
        assertEquals(7f, instance.children().get(0).transform().x);
    }
}
