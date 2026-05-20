package org.llw.studio.serialization;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Camera2DComponent;

import static org.junit.jupiter.api.Assertions.*;

class SceneSerializerMainCameraTest {
    @Test
    void ensureMainCameraCreatesDefaultWhenMissing() {
        var scene = new org.llw.studio.scene.Scene();
        scene.createGameObject("Player");
        assertEquals(0, scene.world().store(Camera2DComponent.class).size());

        SceneSerializer.ensureMainCamera(scene);

        var cameras = scene.world().store(Camera2DComponent.class);
        assertEquals(1, cameras.size());
        assertTrue(cameras.componentAt(0).mainCamera);
    }

    @Test
    void loadAddsMainCameraWhenSceneFileHasNone() throws Exception {
        var tempDir = java.nio.file.Files.createTempDirectory("scene-camera-test");
        var file = tempDir.resolve("scene.json");
        java.nio.file.Files.writeString(file, """
                {
                  "version": 2,
                  "name": "Test",
                  "objects": [
                    {
                      "id": 0,
                      "name": "Player",
                      "parentId": -1,
                      "active": true,
                      "transform": { "x": 0, "y": 0, "rotation": 0, "scaleX": 1, "scaleY": 1 }
                    }
                  ]
                }
                """);

        var loaded = SceneSerializer.load(file);
        assertEquals(1, loaded.world().store(Camera2DComponent.class).size());
    }
}
