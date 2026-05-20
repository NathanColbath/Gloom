package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.Camera2DBinding;
import org.llw.studio.scripting.js.bindings.CameraBinding;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Camera2DBindingTest {
    @Test
    void mainCameraComponentRoundTripsCoordinates() {
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

        try (var runtime = new GraalScriptRuntime(null, scene, null, null)) {
            ScriptHostApi hostApi = runtime.hostApi();
            Camera2DBinding binding = new Camera2DBinding(hostApi.scriptContext(), hostApi, cameraObject.entity());
            CameraBinding.Vector2Result screen = binding.worldToScreen(100d, 50d);
            assertEquals(400d, screen.getX(), 1d);
            assertEquals(225d, screen.getY(), 1d);
            CameraBinding.Vector2Result world = binding.screenToWorld(screen.getX(), screen.getY());
            assertEquals(100d, world.getX(), 1d);
            assertEquals(50d, world.getY(), 1d);
        }
    }

    @Test
    void entityGetComponentReturnsCamera2D() {
        Scene scene = new Scene();
        GameObject cameraObject = scene.createGameObject("Main Camera");
        Camera2DComponent camera = new Camera2DComponent();
        camera.mainCamera = true;
        cameraObject.addComponent(Camera2DComponent.class, camera);

        PlayCameraBridge.syncScene(scene, 800, 450);

        try (var runtime = new GraalScriptRuntime(null, scene, null, java.nio.file.Path.of("."))) {
            var context = runtime.context();
            var hostApi = runtime.hostApi();
            var main = hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), cameraObject.entity()));
            context.getBindings("js").putMember("cam", main);
            var has = context.eval("js", "cam.getComponent('Camera2D') !== null");
            assertTrue(has.asBoolean());
        }
    }

    @Test
    void scriptNameMatchesClassName() {
        assertTrue(JsScriptInstance.namesMatch("PlayerController.ts", "PlayerController"));
        assertTrue(JsScriptInstance.namesMatch("PlayerController", "PlayerController"));
    }
}
