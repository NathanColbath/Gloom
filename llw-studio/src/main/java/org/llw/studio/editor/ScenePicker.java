package org.llw.studio.editor;



import org.llw.studio.assets.AssetDatabase;

import org.llw.studio.ecs.ComponentStore;

import org.llw.studio.ecs.EntityId;

import org.llw.studio.ecs.components.NameComponent;

import org.llw.studio.ecs.components.SpriteRendererComponent;

import org.llw.studio.ecs.components.Transform2DComponent;

import org.llw.studio.ecs.components.WorldTransformComponent;

import org.llw.studio.camera.CameraViewBounds;

import org.llw.studio.render.EntityBounds;

import org.llw.studio.scene.ActiveUtility;

import org.llw.studio.scene.Scene;

import org.llw.studio.systems.TransformSystem;



/**

 * Top-down entity picking in world space by smallest overlapping bounds (front-to-back by area).

 */

public final class ScenePicker {

    private ScenePicker() {

    }



    /**

     * Picks at a world point using default game-view dimensions for sprite bounds.

     *

     * @param scene   scene to search

     * @param assets  asset database for renderer bounds

     * @param worldX  X in world units

     * @param worldY  Y in world units

     * @return smallest hit entity, or {@link EntityId#none()}

     */

    public static EntityId pick(Scene scene, AssetDatabase assets, float worldX, float worldY) {

        return pick(scene, assets, worldX, worldY,

                CameraViewBounds.DEFAULT_VIEWPORT_WIDTH,

                CameraViewBounds.DEFAULT_VIEWPORT_HEIGHT);

    }



    /**

     * Picks at a world point with explicit viewport size for bounds calculation.

     *

     * @param scene           scene to search

     * @param assets          asset database for renderer bounds

     * @param worldX          X in world units

     * @param worldY          Y in world units

     * @param gameViewWidth   viewport width for bounds sampling

     * @param gameViewHeight  viewport height for bounds sampling

     * @return smallest hit entity, or {@link EntityId#none()}

     */

    public static EntityId pick(

            Scene scene,

            AssetDatabase assets,

            float worldX,

            float worldY,

            int gameViewWidth,

            int gameViewHeight

    ) {

        org.llw.studio.editor.render.EditorWorldTransforms.ensureUpdated(scene);

        // Smallest overlapping bounds wins (front-to-back by screen area, not entity id).
        EntityId best = EntityId.none();

        float bestArea = Float.MAX_VALUE;

        ComponentStore<Transform2DComponent> transforms = scene.world().store(Transform2DComponent.class);

        for (int i = 0; i < transforms.size(); i++) {

            EntityId entity = transforms.entityAt(i);

            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {

                continue;

            }

            NameComponent name = scene.world().getComponent(entity, NameComponent.class);

            if (name != null && "Scene Root".equals(name.name())) {

                continue;

            }

            EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, assets, gameViewWidth, gameViewHeight);

            if (worldX < bounds.minX || worldX > bounds.maxX || worldY < bounds.minY || worldY > bounds.maxY) {

                continue;

            }

            float area = bounds.width() * bounds.height();

            if (area < bestArea) {

                bestArea = area;

                best = entity;

            }

        }

        return best;

    }

}

