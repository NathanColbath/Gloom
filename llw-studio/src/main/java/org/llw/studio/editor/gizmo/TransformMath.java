package org.llw.studio.editor.gizmo;



import org.llw.studio.ecs.EntityId;

import org.llw.studio.ecs.World;

import org.llw.studio.ecs.components.HierarchyComponent;

import org.llw.studio.ecs.components.Transform2DComponent;

import org.llw.studio.ecs.components.WorldTransformComponent;



/**

 * Writes world-space transform values back to local {@link Transform2DComponent} fields.

 */

public final class TransformMath {

    private TransformMath() {

    }



    /**

     * Sets world translation by updating local position relative to parent scale/rotation.

     *

     * @param world  ECS world

     * @param entity target entity

     * @param worldX desired world X

     * @param worldY desired world Y

     */

    public static void writeWorldTranslation(World world, EntityId entity, float worldX, float worldY) {

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        if (local == null) {

            return;

        }

        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);

        if (hierarchy == null || hierarchy.parentIndex < 0) {

            local.x = worldX;

            local.y = worldY;

            return;

        }

        EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);

        WorldTransformComponent parentWorld = world.getComponent(parentId, WorldTransformComponent.class);

        if (parentWorld == null) {

            local.x = worldX;

            local.y = worldY;

            return;

        }

        float dx = worldX - parentWorld.worldX;

        float dy = worldY - parentWorld.worldY;

        float parentRad = (float) Math.toRadians(-parentWorld.worldRotation);

        float cos = (float) Math.cos(parentRad);

        float sin = (float) Math.sin(parentRad);

        float rotatedX = dx * cos - dy * sin;

        float rotatedY = dx * sin + dy * cos;

        local.x = rotatedX / parentWorld.worldScaleX;

        local.y = rotatedY / parentWorld.worldScaleY;

    }



    /**

     * Sets world rotation by updating local rotation relative to parent.

     *

     * @param world         ECS world

     * @param entity        target entity

     * @param worldRotation desired world rotation in degrees

     */

    public static void writeWorldRotation(World world, EntityId entity, float worldRotation) {

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        if (local == null) {

            return;

        }

        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);

        if (hierarchy == null || hierarchy.parentIndex < 0) {

            local.rotation = worldRotation;

            return;

        }

        EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);

        WorldTransformComponent parentWorld = world.getComponent(parentId, WorldTransformComponent.class);

        local.rotation = parentWorld == null ? worldRotation : worldRotation - parentWorld.worldRotation;

    }



    /**

     * Sets world scale by updating local scale relative to parent.

     *

     * @param world        ECS world

     * @param entity       target entity

     * @param worldScaleX  desired world scale X

     * @param worldScaleY  desired world scale Y

     */

    public static void writeWorldScale(World world, EntityId entity, float worldScaleX, float worldScaleY) {

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        if (local == null) {

            return;

        }

        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);

        if (hierarchy == null || hierarchy.parentIndex < 0) {

            local.scaleX = worldScaleX;

            local.scaleY = worldScaleY;

            return;

        }

        EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);

        WorldTransformComponent parentWorld = world.getComponent(parentId, WorldTransformComponent.class);

        if (parentWorld == null) {

            local.scaleX = worldScaleX;

            local.scaleY = worldScaleY;

            return;

        }

        local.scaleX = worldScaleX / parentWorld.worldScaleX;

        local.scaleY = worldScaleY / parentWorld.worldScaleY;

    }



    /**

     * @param world  ECS world

     * @param entity entity to query

     * @return world rotation in degrees

     */

    public static float worldRotation(World world, EntityId entity) {

        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);

        if (worldTransform != null) {

            return worldTransform.worldRotation;

        }

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        return local == null ? 0f : local.rotation;

    }



    /**

     * @param world  ECS world

     * @param entity entity to query

     * @return world scale X

     */

    public static float worldScaleX(World world, EntityId entity) {

        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);

        if (worldTransform != null) {

            return worldTransform.worldScaleX;

        }

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        return local == null ? 1f : local.scaleX;

    }



    /**

     * @param world  ECS world

     * @param entity entity to query

     * @return world scale Y

     */

    public static float worldScaleY(World world, EntityId entity) {

        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);

        if (worldTransform != null) {

            return worldTransform.worldScaleY;

        }

        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);

        return local == null ? 1f : local.scaleY;

    }

}

