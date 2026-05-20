package org.llw.studio.scripting.js;

import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.studio.camera.CameraViewBounds;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.CameraSystem;
import org.llw.studio.systems.TransformSystem;

/**
 * Play-mode camera state mirrored from the Game view each frame.
 */
public final class PlayCameraBridge {
    private static final Camera2d camera = new Camera2d();
    private static IntSize viewport = new IntSize(1, 1);
    private static float orthographicSize = 360f;
    private static float viewportScreenX;
    private static float viewportScreenY;
    private static EntityId mainCamera = EntityId.none();
    private static boolean active;
    private static float backgroundR = 38f / 255f;
    private static float backgroundG = 38f / 255f;
    private static float backgroundB = 38f / 255f;
    private static float backgroundA = 1f;

    private PlayCameraBridge() {
    }

    /** Resets camera state to defaults. */
    public static void reset() {
        active = false;
        mainCamera = EntityId.none();
        viewport = new IntSize(1, 1);
        orthographicSize = 360f;
        viewportScreenX = 0f;
        viewportScreenY = 0f;
        camera.setCenter(0f, 0f);
        camera.setSize(1f, 1f);
        backgroundR = 38f / 255f;
        backgroundG = 38f / 255f;
        backgroundB = 38f / 255f;
        backgroundA = 1f;
    }

    /**
     * @param scene  play-mode scene to read transforms and main camera from
     * @param width  game view width in pixels
     * @param height game view height in pixels
     */
    public static void syncScene(Scene scene, int width, int height) {
        if (scene == null) {
            active = false;
            return;
        }
        new TransformSystem().onUpdate(scene.world(), 0f);
        CameraSystem cameraSystem = new CameraSystem();
        cameraSystem.onUpdate(scene.world(), 0f);
        viewport = CameraViewBounds.viewportSize(width, height);
        mainCamera = cameraSystem.mainCamera();
        Camera2DComponent component = cameraSystem.mainCameraComponent(scene.world());
        if (component == null || mainCamera.isNone()) {
            orthographicSize = viewport.height() * 0.5f;
            camera.setCenter(0f, 0f);
            camera.setSize(viewport.width(), viewport.height());
            active = true;
            return;
        }
        WorldTransformComponent world = scene.world().getComponent(mainCamera, WorldTransformComponent.class);
        Transform2DComponent local = scene.world().getComponent(mainCamera, Transform2DComponent.class);
        float centerX = world != null ? world.worldX : local != null ? local.x : 0f;
        float centerY = world != null ? world.worldY : local != null ? local.y : 0f;
        orthographicSize = Math.max(1f, component.orthographicSize);
        backgroundR = component.backgroundR;
        backgroundG = component.backgroundG;
        backgroundB = component.backgroundB;
        backgroundA = component.backgroundA;
        CameraViewBounds bounds = CameraViewBounds.fromCenter(
                centerX,
                centerY,
                orthographicSize,
                CameraViewBounds.aspectFromViewport(viewport.width(), viewport.height())
        );
        camera.setCenter(bounds.centerX, bounds.centerY);
        camera.setSize(bounds.worldWidth(), bounds.worldHeight());
        active = true;
    }

    /**
     * @param screenX game view origin X in screen coordinates
     * @param screenY game view origin Y in screen coordinates
     */
    public static void setViewportScreenRect(float screenX, float screenY) {
        viewportScreenX = screenX;
        viewportScreenY = screenY;
    }

    /**
     * @param target render camera to update from the mirrored play state
     */
    public static void applyTo(Camera2d target) {
        if (target == null) {
            return;
        }
        target.setCenter(camera.getCenter());
        target.setSize(camera.getSize());
    }

    /**
     * @return {@code true} after a successful {@link #syncScene}
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * @return main camera background red channel in {@code [0, 1]}
     */
    public static float backgroundR() {
        return backgroundR;
    }

    /**
     * @return main camera background green channel in {@code [0, 1]}
     */
    public static float backgroundG() {
        return backgroundG;
    }

    /**
     * @return main camera background blue channel in {@code [0, 1]}
     */
    public static float backgroundB() {
        return backgroundB;
    }

    /**
     * @return main camera background alpha channel in {@code [0, 1]}
     */
    public static float backgroundA() {
        return backgroundA;
    }

    /**
     * @return entity id of the main camera, or {@link EntityId#none()}
     */
    public static EntityId mainCamera() {
        return mainCamera;
    }

    /**
     * @return game view size in pixels
     */
    public static IntSize viewport() {
        return viewport;
    }

    /**
     * @return world-space camera center X
     */
    public static double getCenterX() {
        return camera.getCenter().x;
    }

    /**
     * @return world-space camera center Y
     */
    public static double getCenterY() {
        return camera.getCenter().y;
    }

    /**
     * @return visible world width
     */
    public static double getWorldWidth() {
        return camera.getSize().x;
    }

    /**
     * @return visible world height
     */
    public static double getWorldHeight() {
        return camera.getSize().y;
    }

    /**
     * @return orthographic half-height of the main camera
     */
    public static double getOrthographicSize() {
        return orthographicSize;
    }

    /**
     * @return game view width in pixels
     */
    public static double getViewportWidth() {
        return viewport.width();
    }

    /**
     * @return game view height in pixels
     */
    public static double getViewportHeight() {
        return viewport.height();
    }

    /**
     * @return width divided by height of the game view
     */
    public static double getAspect() {
        return CameraViewBounds.aspectFromViewport(viewport.width(), viewport.height());
    }

    /**
     * @return mouse X relative to the game view
     */
    public static double getViewportMouseX() {
        return PlayInputBridge.getMouseX() - viewportScreenX;
    }

    /**
     * @return mouse Y relative to the game view
     */
    public static double getViewportMouseY() {
        return PlayInputBridge.getMouseY() - viewportScreenY;
    }

    /**
     * @param worldX world X
     * @param worldY world Y
     * @return screen coordinates {@code [x, y]}
     */
    public static double[] worldToScreen(double worldX, double worldY) {
        var screen = camera.worldToScreen(
                new org.llw.math.vector.Vector2f((float) worldX, (float) worldY),
                viewport
        );
        return new double[]{screen.x, screen.y};
    }

    /**
     * @param screenX screen X within the game view
     * @param screenY screen Y within the game view
     * @return world coordinates {@code [x, y]}
     */
    public static double[] screenToWorld(double screenX, double screenY) {
        var world = camera.screenToWorld(
                new org.llw.math.vector.Vector2f((float) screenX, (float) screenY),
                viewport
        );
        return new double[]{world.x, world.y};
    }
}
