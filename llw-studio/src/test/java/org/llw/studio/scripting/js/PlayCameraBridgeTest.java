package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayCameraBridgeTest {
    @Test
    void worldToScreenAndBackRoundTrip() {
        Scene scene = new Scene();
        GameObject cameraObject = scene.createGameObject("Main Camera");
        cameraObject.transform().x = 100f;
        cameraObject.transform().y = 50f;
        Camera2DComponent camera = new Camera2DComponent();
        camera.orthographicSize = 180f;
        camera.mainCamera = true;
        cameraObject.addComponent(Camera2DComponent.class, camera);

        PlayCameraBridge.syncScene(scene, 800, 450);
        PlayCameraBridge.setViewportScreenRect(0f, 0f);

        double[] screen = PlayCameraBridge.worldToScreen(100d, 50d);
        assertEquals(400d, screen[0], 1d);
        assertEquals(225d, screen[1], 1d);

        double[] world = PlayCameraBridge.screenToWorld(screen[0], screen[1]);
        assertEquals(100d, world[0], 1d);
        assertEquals(50d, world[1], 1d);
    }
}
