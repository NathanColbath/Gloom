package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.render.core.IntSize;
import org.llw.studio.camera.CameraViewBounds;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scripting.js.PlayCameraBridge;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: {@code Camera2D} component host binding.
 */
public final class Camera2DBinding {
    private final ScriptContext context;
    private final ScriptHostApi hostApi;
    private final World world;
    private final EntityId entity;

    /**
     * @param context play-mode script context
     * @param hostApi host API (unused, reserved for wrapping)
     * @param entity  camera entity
     */
    public Camera2DBinding(ScriptContext context, ScriptHostApi hostApi, EntityId entity) {
        this.context = context;
        this.hostApi = hostApi;
        this.world = context.world();
        this.entity = entity;
    }

    /** @return {@code true} when this camera is marked main */
    @HostAccess.Export
    public boolean getMainCamera() {
        Camera2DComponent camera = component();
        return camera == null || camera.mainCamera;
    }

    /**
     * @param value {@code true} to mark as main camera
     */
    @HostAccess.Export
    public void setMainCamera(boolean value) {
        Camera2DComponent camera = component();
        if (camera != null) {
            camera.mainCamera = value;
        }
    }

    /** @return orthographic half-height */
    @HostAccess.Export
    public double getOrthographicSize() {
        Camera2DComponent camera = component();
        return camera == null ? 360d : camera.orthographicSize;
    }

    /**
     * @param value orthographic half-height
     */
    @HostAccess.Export
    public void setOrthographicSize(double value) {
        Camera2DComponent camera = component();
        if (camera != null) {
            camera.orthographicSize = (float) value;
        }
    }

    /** @return render depth */
    @HostAccess.Export
    public double getDepth() {
        Camera2DComponent camera = component();
        return camera == null ? 0d : camera.depth;
    }

    /**
     * @param value render depth
     */
    @HostAccess.Export
    public void setDepth(double value) {
        Camera2DComponent camera = component();
        if (camera != null) {
            camera.depth = (float) value;
        }
    }

    /** @return world-space X of the camera */
    @HostAccess.Export
    public double getWorldX() {
        return readWorldPosition()[0];
    }

    /** @return world-space Y of the camera */
    @HostAccess.Export
    public double getWorldY() {
        return readWorldPosition()[1];
    }

    /** @return view center X (same as {@link #getWorldX()}) */
    @HostAccess.Export
    public double getCenterX() {
        return getWorldX();
    }

    /** @return view center Y (same as {@link #getWorldY()}) */
    @HostAccess.Export
    public double getCenterY() {
        return getWorldY();
    }

    /** @return visible world width */
    @HostAccess.Export
    public double getWorldWidth() {
        return viewBounds().worldWidth();
    }

    /** @return visible world height */
    @HostAccess.Export
    public double getWorldHeight() {
        return viewBounds().worldHeight();
    }

    /** @return game view width in pixels */
    @HostAccess.Export
    public double getViewportWidth() {
        return PlayCameraBridge.isActive()
                ? PlayCameraBridge.getViewportWidth()
                : CameraViewBounds.DEFAULT_VIEWPORT_WIDTH;
    }

    /** @return game view height in pixels */
    @HostAccess.Export
    public double getViewportHeight() {
        return PlayCameraBridge.isActive()
                ? PlayCameraBridge.getViewportHeight()
                : CameraViewBounds.DEFAULT_VIEWPORT_HEIGHT;
    }

    /** @return viewport width divided by height */
    @HostAccess.Export
    public double getAspect() {
        if (PlayCameraBridge.isActive()) {
            return PlayCameraBridge.getAspect();
        }
        return CameraViewBounds.aspectFromViewport(
                CameraViewBounds.DEFAULT_VIEWPORT_WIDTH,
                CameraViewBounds.DEFAULT_VIEWPORT_HEIGHT
        );
    }

    /** @return mouse X relative to the game view */
    @HostAccess.Export
    public double getViewportMouseX() {
        return PlayCameraBridge.getViewportMouseX();
    }

    /** @return mouse Y relative to the game view */
    @HostAccess.Export
    public double getViewportMouseY() {
        return PlayCameraBridge.getViewportMouseY();
    }

    /**
     * @return {@code true} when this entity is the synchronized main play camera
     */
    @HostAccess.Export
    public boolean isActiveMain() {
        return PlayCameraBridge.isActive() && entity.equals(PlayCameraBridge.mainCamera());
    }

    /**
     * @param worldX world X
     * @param worldY world Y
     * @return screen-space point
     */
    @HostAccess.Export
    public CameraBinding.Vector2Result worldToScreen(double worldX, double worldY) {
        if (isActiveMain()) {
            double[] screen = PlayCameraBridge.worldToScreen(worldX, worldY);
            return new CameraBinding.Vector2Result(screen[0], screen[1]);
        }
        IntSize viewport = currentViewport();
        var screen = viewBounds().worldToScreen((float) worldX, (float) worldY, viewport);
        return new CameraBinding.Vector2Result(screen.x, screen.y);
    }

    /**
     * @param screenX screen X within the game view
     * @param screenY screen Y within the game view
     * @return world-space point
     */
    @HostAccess.Export
    public CameraBinding.Vector2Result screenToWorld(double screenX, double screenY) {
        if (isActiveMain()) {
            double[] world = PlayCameraBridge.screenToWorld(screenX, screenY);
            return new CameraBinding.Vector2Result(world[0], world[1]);
        }
        IntSize viewport = currentViewport();
        var world = viewBounds().screenToWorld((float) screenX, (float) screenY, viewport);
        return new CameraBinding.Vector2Result(world.x, world.y);
    }

    private CameraViewBounds viewBounds() {
        float[] position = readWorldPosition();
        float size = component() == null ? 360f : component().orthographicSize;
        float aspect = (float) getAspect();
        return CameraViewBounds.fromCenter(position[0], position[1], size, aspect);
    }

    private IntSize currentViewport() {
        if (PlayCameraBridge.isActive()) {
            return PlayCameraBridge.viewport();
        }
        return CameraViewBounds.viewportSize(
                CameraViewBounds.DEFAULT_VIEWPORT_WIDTH,
                CameraViewBounds.DEFAULT_VIEWPORT_HEIGHT
        );
    }

    private float[] readWorldPosition() {
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        if (worldTransform != null) {
            return new float[]{worldTransform.worldX, worldTransform.worldY};
        }
        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
        if (local != null) {
            return new float[]{local.x, local.y};
        }
        return new float[]{0f, 0f};
    }

    private Camera2DComponent component() {
        return world.getComponent(entity, Camera2DComponent.class);
    }
}
