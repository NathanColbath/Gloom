package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;

/**
 * Play-mode API for {@link TilemapComponent}.
 */
public final class Tilemap2DBinding {
    private final World world;
    private final EntityId entity;

    public Tilemap2DBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getTilesetTextureGuid() {
        TilemapComponent tilemap = component();
        return tilemap == null ? "" : tilemap.tilesetTextureGuid;
    }

    @HostAccess.Export
    public void setTilesetTextureGuid(String guid) {
        TilemapComponent tilemap = component();
        if (tilemap != null) {
            tilemap.tilesetTextureGuid = guid == null ? "" : guid;
        }
    }

    @HostAccess.Export
    public String getTile(int layer, int x, int y) {
        TilemapComponent tilemap = component();
        if (tilemap == null) {
            return null;
        }
        TilemapLayer mapLayer = tilemap.layerAt(layer);
        TilemapCell cell = mapLayer.getCell(x, y);
        if (cell == null || cell.spriteGuid == null || cell.spriteGuid.isBlank()) {
            return null;
        }
        return cell.spriteGuid;
    }

    @HostAccess.Export
    public void setTile(int layer, int x, int y, String spriteGuid) {
        TilemapComponent tilemap = component();
        if (tilemap == null) {
            return;
        }
        TilemapLayer mapLayer = tilemap.layerAt(layer);
        if (spriteGuid == null || spriteGuid.isBlank()) {
            mapLayer.removeCell(x, y);
        } else {
            TilemapCell cell = new TilemapCell();
            cell.spriteGuid = spriteGuid;
            mapLayer.setCell(x, y, cell);
        }
    }

    /**
     * Reserved for future explicit rule-tile refresh; rendering resolves rules each frame.
     */
    @HostAccess.Export
    public void refresh(int layer, int x, int y, int radius) {
        // Rule tiles resolve at draw time via RuleTileResolver.
    }

    private TilemapComponent component() {
        return world.getComponent(entity, TilemapComponent.class);
    }
}
