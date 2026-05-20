package org.llw.studio.editor;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenePickerTest {
    @Test
    void picksSmallestContainingBounds() {
        Scene scene = new Scene();
        var big = scene.createGameObject("Big");
        big.transform().x = 0f;
        big.transform().y = 0f;
        big.transform().scaleX = 4f;
        big.transform().scaleY = 4f;
        big.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

        var small = scene.createGameObject("Small");
        small.transform().x = 32f;
        small.transform().y = 32f;
        small.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

        EntityId picked = ScenePicker.pick(scene, null, 40f, 40f);
        assertEquals(small.entity(), picked);
    }

    @Test
    void picksMainCameraInsideViewport() {
        Scene scene = new Scene();
        var cameraObject = scene.createGameObject("Main Camera");
        cameraObject.transform().x = 400f;
        cameraObject.transform().y = 225f;
        Camera2DComponent camera = new Camera2DComponent();
        camera.orthographicSize = 180f;
        camera.mainCamera = true;
        cameraObject.addComponent(Camera2DComponent.class, camera);

        EntityId picked = ScenePicker.pick(scene, null, 400f, 225f, 800, 450);
        assertEquals(cameraObject.entity(), picked);
    }

    @Test
    void ignoresSceneRoot() {
        Scene scene = new Scene();
        EntityId picked = ScenePicker.pick(scene, null, 0f, 0f);
        assertTrue(picked.isNone());
    }
}
