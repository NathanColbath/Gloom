package org.llw.studio.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.render.RenderLayers;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;
import org.llw.studio.tilemap.RuleTileResolver;
import org.llw.studio.tilemap.TilemapCellKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Renders all {@link TilemapComponent} layers into an offscreen target.
 */
public final class TilemapDrawPass {
    private TilemapDrawPass() {
    }

    public static void draw(Scene scene, OffscreenTarget target, AssetDatabase assets) {
        new TransformSystem().onUpdate(scene.world(), 0f);
        ComponentStore<TilemapComponent> tilemaps = scene.world().store(TilemapComponent.class);
        List<DrawEntry> entries = new ArrayList<>();
        for (int i = 0; i < tilemaps.size(); i++) {
            EntityId entity = tilemaps.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            TilemapComponent tilemap = tilemaps.componentAt(i);
            if (tilemap.tilesetTextureGuid == null || tilemap.tilesetTextureGuid.isBlank()) {
                continue;
            }
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            if (local == null) {
                continue;
            }
            float originX = world != null ? world.worldX : local.x;
            float originY = world != null ? world.worldY : local.y;
            float scaleX = world != null ? world.worldScaleX : local.scaleX;
            float scaleY = world != null ? world.worldScaleY : local.scaleY;
            collectEntries(entries, tilemap, originX, originY, scaleX, scaleY, assets);
        }
        entries.sort(Comparator.comparingInt(e -> e.sortingOrder));
        for (DrawEntry entry : entries) {
            Sprite sprite = new Sprite(entry.texture);
            int tw = entry.texture.size().width();
            int th = entry.texture.size().height();
            sprite.setTextureRect(entry.slice.uvRect(tw, th));
            TilePlacement.applyTopLeft(
                    sprite,
                    entry.slice,
                    entry.worldX,
                    entry.worldY,
                    entry.scaleX,
                    entry.scaleY,
                    entry.flags
            );
            sprite.setTint(Color.WHITE);
            DrawState state = DrawState.DEFAULT.withLayer(RenderLayers.SCENE_BASE + entry.sortingOrder);
            target.draw(sprite, state);
        }
    }

    private static void collectEntries(
            List<DrawEntry> entries,
            TilemapComponent tilemap,
            float originX,
            float originY,
            float scaleX,
            float scaleY,
            AssetDatabase assets
    ) {
        for (TilemapLayer layer : tilemap.layers) {
            if (!layer.enabled) {
                continue;
            }
            for (Map.Entry<Long, TilemapCell> cellEntry : layer.cells.entrySet()) {
                int cellX = TilemapCellKey.unpackX(cellEntry.getKey());
                int cellY = TilemapCellKey.unpackY(cellEntry.getKey());
                TilemapCell cell = cellEntry.getValue();
                String resolvedGuid = RuleTileResolver.resolveSpriteGuid(
                        tilemap, layer, cellX, cellY, cell, assets);
                if (resolvedGuid == null || resolvedGuid.isBlank()) {
                    continue;
                }
                SpriteDefinition slice = assets.sprite(resolvedGuid);
                if (slice == null) {
                    continue;
                }
                Texture2d texture = assets.texture(slice.textureGuid());
                if (texture == null) {
                    continue;
                }
                float worldX = originX + cellX * tilemap.cellWidth * scaleX;
                float worldY = originY + cellY * tilemap.cellHeight * scaleY;
                entries.add(new DrawEntry(
                        layer.sortingOrder,
                        texture,
                        slice,
                        worldX,
                        worldY,
                        scaleX,
                        scaleY,
                        cell.flags
                ));
            }
        }
    }

    private record DrawEntry(
            int sortingOrder,
            Texture2d texture,
            SpriteDefinition slice,
            float worldX,
            float worldY,
            float scaleX,
            float scaleY,
            byte flags
    ) {
    }
}
