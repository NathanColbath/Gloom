package org.llw.studio.render;

import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

/**
 * Computes a scene-wide axis-aligned bounding box from active transforms and sprites.
 *
 * <p><b>Coordinates (Y-down):</b> {@link Bounds#minX}/{@link Bounds#minY} is the top-left
 * extent; {@link Bounds#maxX}/{@link Bounds#maxY} is the bottom-right.
 */
public final class SceneBounds {
    /** Immutable world-space rectangle with an {@link #empty} sentinel. */
    public static final class Bounds {
        /** Left edge (minimum world X). */
        public final float minX;
        /** Top edge (minimum world Y). */
        public final float minY;
        /** Right edge (maximum world X). */
        public final float maxX;
        /** Bottom edge (maximum world Y). */
        public final float maxY;
        /** {@code true} when no contributing objects were found. */
        public final boolean empty;

        private Bounds(float minX, float minY, float maxX, float maxY, boolean empty) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.empty = empty;
        }

        /** @return sentinel bounds with {@link #empty} {@code true} */
        public static Bounds empty() {
            return new Bounds(0f, 0f, 0f, 0f, true);
        }

        /**
         * @param minX top-left world X
         * @param minY top-left world Y
         * @param maxX bottom-right world X
         * @param maxY bottom-right world Y
         */
        public static Bounds of(float minX, float minY, float maxX, float maxY) {
            return new Bounds(minX, minY, maxX, maxY, false);
        }
    }

    private SceneBounds() {
    }

    /**
     * Refreshes transforms, then unions sprite footprints and bare transform points.
     *
     * @param scene  scene to measure
     * @param assets texture lookup for sprite extents
     * @return computed bounds, or {@link Bounds#empty()} when the scene has no contributors
     */
    public static Bounds compute(Scene scene, AssetDatabase assets) {
        new TransformSystem().onUpdate(scene.world(), 0f);
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        boolean found = false;

        ComponentStore<SpriteRendererComponent> sprites = scene.world().store(SpriteRendererComponent.class);
        for (int i = 0; i < sprites.size(); i++) {
            EntityId entity = sprites.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            SpriteRendererComponent renderer = sprites.componentAt(i);
            SpriteDefinition slice = SpriteResolve.resolve(assets, renderer);
            float width = slice != null ? SpriteResolve.pixelWidth(slice) : 64f;
            float height = slice != null ? SpriteResolve.pixelHeight(slice) : 64f;
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            if (local == null) {
                continue;
            }
            float x = world != null ? world.worldX : local.x;
            float y = world != null ? world.worldY : local.y;
            float scaleX = world != null ? world.worldScaleX : local.scaleX;
            float scaleY = world != null ? world.worldScaleY : local.scaleY;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + width * Math.abs(scaleX));
            maxY = Math.max(maxY, y + height * Math.abs(scaleY));
            found = true;
        }

        ComponentStore<Transform2DComponent> transforms = scene.world().store(Transform2DComponent.class);
        for (int i = 0; i < transforms.size(); i++) {
            EntityId entity = transforms.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            if (scene.world().hasComponent(entity, SpriteRendererComponent.class)) {
                continue;
            }
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = transforms.componentAt(i);
            float x = world != null ? world.worldX : local.x;
            float y = world != null ? world.worldY : local.y;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            found = true;
        }

        if (!found) {
            return Bounds.empty();
        }
        if (minX == maxX) {
            maxX = minX + 1f;
        }
        if (minY == maxY) {
            maxY = minY + 1f;
        }
        return Bounds.of(minX, minY, maxX, maxY);
    }
}
