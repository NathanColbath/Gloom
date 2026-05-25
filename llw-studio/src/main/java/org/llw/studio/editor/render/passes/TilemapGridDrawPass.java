package org.llw.studio.editor.render.passes;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.tilemap.TilemapMath;

/**
 * Draws a cell-aligned grid for the active tilemap in the scene view.
 *
 * <p>Requires {@link org.llw.studio.editor.render.EditorWorldTransforms#ensureUpdated(Scene)} before draw.
 */
public final class TilemapGridDrawPass {
    private static final DrawState GRID_STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.GRID + 1);

    private TilemapGridDrawPass() {
    }

    public static void draw(
            Scene scene,
            OffscreenTarget target,
            EditorCamera camera,
            int viewWidth,
            int viewHeight,
            EntityId tilemapEntity
    ) {
        if (tilemapEntity == null || tilemapEntity.isNone()) {
            return;
        }
        TilemapComponent tilemap = scene.world().getComponent(tilemapEntity, TilemapComponent.class);
        Transform2DComponent local = scene.world().getComponent(tilemapEntity, Transform2DComponent.class);
        if (tilemap == null || local == null) {
            return;
        }
        WorldTransformComponent world = scene.world().getComponent(tilemapEntity, WorldTransformComponent.class);
        // Cell lines align to tilemap origin in world space (parented maps use baked world pose).
        float originX = world != null ? world.worldX : local.x;
        float originY = world != null ? world.worldY : local.y;
        float scaleX = world != null ? world.worldScaleX : local.scaleX;
        float scaleY = world != null ? world.worldScaleY : local.scaleY;

        float stepX = Math.max(tilemap.cellWidth * Math.abs(scaleX), 1e-4f);
        float stepY = Math.max(tilemap.cellHeight * Math.abs(scaleY), 1e-4f);
        float zoom = camera.zoom();
        float lineThickness = EditorViewportMath.pixelsToWorld(zoom, 1f);
        Color line = new Color(80, 120, 80, 255);

        float left = EditorViewportMath.worldLeft(camera, viewWidth);
        float right = left + EditorViewportMath.worldWidth(camera, viewWidth);
        float top = EditorViewportMath.worldTop(camera, viewHeight);
        float bottom = top + EditorViewportMath.worldHeight(camera, viewHeight);

        int minCellX = TilemapMath.worldToCellX(left, originX, tilemap.cellWidth, scaleX) - 1;
        int maxCellX = TilemapMath.worldToCellX(right, originX, tilemap.cellWidth, scaleX) + 2;
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            float x = TilemapMath.cellToWorldX(cx, originX, tilemap, scaleX);
            float snapped = EditorViewportMath.snapWorldX(camera, viewWidth, x);
            Rectangle bar = new Rectangle();
            bar.setPosition(snapped - lineThickness * 0.5f, top);
            bar.setSize(lineThickness, bottom - top);
            bar.setFillColor(line);
            target.draw(bar, GRID_STATE);
        }

        int minCellY = TilemapMath.worldToCellY(top, originY, tilemap.cellHeight, scaleY) - 1;
        int maxCellY = TilemapMath.worldToCellY(bottom, originY, tilemap.cellHeight, scaleY) + 2;
        for (int cy = minCellY; cy <= maxCellY; cy++) {
            float y = TilemapMath.cellToWorldY(cy, originY, tilemap, scaleY);
            float snapped = EditorViewportMath.snapWorldY(camera, viewHeight, y);
            Rectangle bar = new Rectangle();
            bar.setPosition(left, snapped - lineThickness * 0.5f);
            bar.setSize(right - left, lineThickness);
            bar.setFillColor(line);
            target.draw(bar, GRID_STATE);
        }
    }
}

