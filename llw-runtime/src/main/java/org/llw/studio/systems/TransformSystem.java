package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.ActiveUtility;

/**
 * Propagates {@link Transform2DComponent} local transforms into {@link WorldTransformComponent}
 * world-space values, respecting parent hierarchy and effective active state.
 *
 * <p>World positions use the studio Y-down convention: {@code worldX}/{@code worldY} are the
 * entity pivot in scene space (+X right, +Y down).
 */
public final class TransformSystem implements EcsSystem {
    /**
     * Recomputes world transforms for every effectively active entity that has a local transform.
     *
     * @param world      scene ECS world
     * @param deltaTime  unused; transform propagation is instantaneous
     */
    @Override
    public void onUpdate(World world, float deltaTime) {
        var transforms = world.store(Transform2DComponent.class);
        for (int i = 0; i < transforms.size(); i++) {
            EntityId entity = transforms.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(world, entity)) {
                continue;
            }
            Transform2DComponent local = transforms.componentAt(i);
            WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
            if (worldTransform == null) {
                worldTransform = new WorldTransformComponent();
                world.addComponent(entity, WorldTransformComponent.class, worldTransform);
            }
            computeWorld(world, entity, local, worldTransform);
        }
    }

    private void computeWorld(
            World world,
            EntityId entity,
            Transform2DComponent local,
            WorldTransformComponent out
    ) {
        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);
        if (hierarchy == null || hierarchy.parentIndex < 0) {
            out.worldX = local.x;
            out.worldY = local.y;
            out.worldRotation = local.rotation;
            out.worldScaleX = local.scaleX;
            out.worldScaleY = local.scaleY;
            return;
        }
        EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
        Transform2DComponent parentLocal = world.getComponent(parentId, Transform2DComponent.class);
        WorldTransformComponent parentWorld = world.getComponent(parentId, WorldTransformComponent.class);
        if (parentLocal == null || parentWorld == null) {
            out.worldX = local.x;
            out.worldY = local.y;
            out.worldRotation = local.rotation;
            out.worldScaleX = local.scaleX;
            out.worldScaleY = local.scaleY;
            return;
        }
        float parentRad = (float) Math.toRadians(parentWorld.worldRotation);
        float cos = (float) Math.cos(parentRad);
        float sin = (float) Math.sin(parentRad);
        float scaledX = local.x * parentWorld.worldScaleX;
        float scaledY = local.y * parentWorld.worldScaleY;
        out.worldX = parentWorld.worldX + scaledX * cos - scaledY * sin;
        out.worldY = parentWorld.worldY + scaledX * sin + scaledY * cos;
        out.worldRotation = parentWorld.worldRotation + local.rotation;
        out.worldScaleX = parentWorld.worldScaleX * local.scaleX;
        out.worldScaleY = parentWorld.worldScaleY * local.scaleY;
    }
}
