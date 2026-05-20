package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Camera2DComponent;

/**
 * Resolves the scene {@linkplain Camera2DComponent#mainCamera main camera} each frame by choosing
 * the enabled main-camera entity with the lowest {@code depth} value.
 */
public final class CameraSystem implements EcsSystem {
    private EntityId mainCamera = EntityId.none();

    /**
     * @return entity id of the resolved main camera, or {@link EntityId#none()} if none qualifies
     */
    public EntityId mainCamera() {
        return mainCamera;
    }

    /**
     * @return orthographic half-height in world units (currently fixed at 360 when a main camera exists)
     */
    public float orthographicSize() {
        return mainCamera.isNone() ? 360f : 360f;
    }

    /**
     * Scans all {@link Camera2DComponent} instances and updates {@link #mainCamera()}.
     *
     * @param world      scene ECS world
     * @param deltaTime  unused
     */
    @Override
    public void onUpdate(World world, float deltaTime) {
        mainCamera = EntityId.none();
        float bestDepth = Float.MAX_VALUE;
        var cameras = world.store(Camera2DComponent.class);
        for (int i = 0; i < cameras.size(); i++) {
            Camera2DComponent camera = cameras.componentAt(i);
            if (!camera.mainCamera) {
                continue;
            }
            if (camera.depth < bestDepth) {
                bestDepth = camera.depth;
                mainCamera = cameras.entityAt(i);
            }
        }
    }

    /**
     * @param world scene ECS world
     * @return the main camera component, or {@code null} if no main camera was resolved
     */
    public Camera2DComponent mainCameraComponent(World world) {
        return mainCamera.isNone() ? null : world.getComponent(mainCamera, Camera2DComponent.class);
    }
}
