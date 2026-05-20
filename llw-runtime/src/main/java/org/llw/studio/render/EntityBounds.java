package org.llw.studio.render;

import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.camera.CameraViewBounds;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.tilemap.TilemapCellKey;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;

/**
 * Axis-aligned world bounds for selection, gizmos, and hit testing.
 *
 * <p><b>Coordinates (Y-down):</b> {@code minX}/{@code minY} is the top-left corner;
 * {@code maxX}/{@code maxY} is the bottom-right. Sprite bounds are centered on the entity
 * pivot ({@link Transform2DComponent#x}/{@code y} in world space).
 */
public final class EntityBounds {
    /** World half-extent used when no texture size is available. */
    public static final float FALLBACK_SIZE = 64f;
    /** Half-size of the editor camera icon hit box in world units. */
    public static final float ICON_HIT_HALF = 24f;

    /** Left edge (minimum world X). */
    public final float minX;
    /** Top edge (minimum world Y). */
    public final float minY;
    /** Right edge (maximum world X). */
    public final float maxX;
    /** Bottom edge (maximum world Y). */
    public final float maxY;

    /**
     * @param minX left edge in world space
     * @param minY top edge in world space
     * @param maxX right edge in world space
     * @param maxY bottom edge in world space
     */
    public EntityBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /** @return {@code maxX - minX} */
    public float width() {
        return maxX - minX;
    }

    /** @return {@code maxY - minY} */
    public float height() {
        return maxY - minY;
    }

    /** @return horizontal center of the bounds rectangle */
    public float pivotX() {
        return (minX + maxX) * 0.5f;
    }

    /** @return vertical center of the bounds rectangle */
    public float pivotY() {
        return (minY + maxY) * 0.5f;
    }

    /** @return alias for {@link #pivotX()} */
    public float centerX() {
        return pivotX();
    }

    /** @return alias for {@link #pivotY()} */
    public float centerY() {
        return pivotY();
    }

    /**
     * Computes bounds using the default game-view aspect for camera entities.
     *
     * @see CameraViewBounds#DEFAULT_VIEWPORT_WIDTH
     * @see CameraViewBounds#DEFAULT_VIEWPORT_HEIGHT
     */
    public static EntityBounds forEntity(World world, EntityId entity, AssetDatabase assets) {
        return forEntity(world, entity, assets, CameraViewBounds.DEFAULT_VIEWPORT_WIDTH, CameraViewBounds.DEFAULT_VIEWPORT_HEIGHT);
    }

    /**
     * Computes axis-aligned bounds for an entity.
     *
     * <p>Cameras union the orthographic frustum with a small icon hit box at the pivot.
     * Sprites use texture size scaled about the world pivot.
     *
     * @param gameViewWidth  viewport width used for camera aspect
     * @param gameViewHeight viewport height used for camera aspect
     */
    public static EntityBounds forEntity(
            World world,
            EntityId entity,
            AssetDatabase assets,
            int gameViewWidth,
            int gameViewHeight
    ) {
        WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
        Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
        if (local == null) {
            return new EntityBounds(0f, 0f, FALLBACK_SIZE, FALLBACK_SIZE);
        }
        float x = worldTransform != null ? worldTransform.worldX : local.x;
        float y = worldTransform != null ? worldTransform.worldY : local.y;
        Camera2DComponent camera = world.getComponent(entity, Camera2DComponent.class);
        if (camera != null) {
            CameraViewBounds view = CameraViewBounds.fromCenter(
                    x,
                    y,
                    camera.orthographicSize,
                    CameraViewBounds.aspectFromViewport(gameViewWidth, gameViewHeight)
            );
            float iconMinX = x - ICON_HIT_HALF;
            float iconMinY = y - ICON_HIT_HALF;
            float iconMaxX = x + ICON_HIT_HALF;
            float iconMaxY = y + ICON_HIT_HALF;
            return new EntityBounds(
                    Math.min(view.minX, iconMinX),
                    Math.min(view.minY, iconMinY),
                    Math.max(view.maxX, iconMaxX),
                    Math.max(view.maxY, iconMaxY)
            );
        }
        float scaleX = worldTransform != null ? worldTransform.worldScaleX : local.scaleX;
        float scaleY = worldTransform != null ? worldTransform.worldScaleY : local.scaleY;
        float width = FALLBACK_SIZE;
        float height = FALLBACK_SIZE;
        SpriteRendererComponent sprite = world.getComponent(entity, SpriteRendererComponent.class);
        if (sprite != null && assets != null) {
            SpriteDefinition slice = SpriteResolve.resolve(assets, sprite);
            if (slice != null) {
                width = SpriteResolve.pixelWidth(slice);
                height = SpriteResolve.pixelHeight(slice);
            }
        }
        TilemapComponent tilemap = world.getComponent(entity, TilemapComponent.class);
        if (tilemap != null && !tilemap.layers.isEmpty()) {
            return boundsForTilemap(tilemap, x, y, scaleX, scaleY);
        }
        float halfWidth = width * Math.abs(scaleX) * 0.5f;
        float halfHeight = height * Math.abs(scaleY) * 0.5f;
        return new EntityBounds(
                x - halfWidth,
                y - halfHeight,
                x + halfWidth,
                y + halfHeight
        );
    }

    private static EntityBounds boundsForTilemap(
            TilemapComponent tilemap,
            float originX,
            float originY,
            float scaleX,
            float scaleY
    ) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean any = false;
        float cellW = tilemap.cellWidth * Math.abs(scaleX);
        float cellH = tilemap.cellHeight * Math.abs(scaleY);
        for (TilemapLayer layer : tilemap.layers) {
            if (!layer.enabled) {
                continue;
            }
            for (var entry : layer.cells.entrySet()) {
                int cx = TilemapCellKey.unpackX(entry.getKey());
                int cy = TilemapCellKey.unpackY(entry.getKey());
                TilemapCell cell = entry.getValue();
                if (cell == null || cell.spriteGuid == null || cell.spriteGuid.isBlank()) {
                    continue;
                }
                float left = originX + cx * cellW;
                float top = originY + cy * cellH;
                float right = left + cellW;
                float bottom = top + cellH;
                minX = Math.min(minX, left);
                minY = Math.min(minY, top);
                maxX = Math.max(maxX, right);
                maxY = Math.max(maxY, bottom);
                any = true;
            }
        }
        if (!any) {
            float half = FALLBACK_SIZE * 0.5f;
            return new EntityBounds(originX - half, originY - half, originX + half, originY + half);
        }
        return new EntityBounds(minX, minY, maxX, maxY);
    }
}
