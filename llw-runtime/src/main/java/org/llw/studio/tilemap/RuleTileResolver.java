package org.llw.studio.tilemap;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.RuleTileDefinition;
import org.llw.studio.assets.RuleTileRule;
import org.llw.studio.assets.TileDefinition;
import org.llw.studio.assets.TileNeighborConstraint;
import org.llw.studio.assets.TileNeighborMask;
import org.llw.studio.assets.TilesetDefinition;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;

/**
 * Resolves rule-tile neighbor patterns to variant sprite GUIDs.
 */
public final class RuleTileResolver {
    private RuleTileResolver() {
    }

    /**
     * @return sprite GUID to draw for a cell (resolved rule variant or logical guid)
     */
    public static String resolveSpriteGuid(
            TilemapComponent tilemap,
            TilemapLayer layer,
            int cellX,
            int cellY,
            TilemapCell cell,
            AssetDatabase assets
    ) {
        if (cell == null || cell.spriteGuid == null || cell.spriteGuid.isBlank()) {
            return "";
        }
        TilesetDefinition tileset = assets.tileset(tilemap.tilesetTextureGuid);
        if (tileset == null) {
            return cell.spriteGuid;
        }
        TileDefinition tile = tileset.tileForSprite(cell.spriteGuid);
        if (tile == null) {
            tile = assets.tileDefinition(cell.spriteGuid);
        }
        if (tile == null || tile.ruleTile == null || tile.ruleTile.rules.isEmpty()) {
            return cell.spriteGuid;
        }
        RuleTileDefinition ruleTile = tile.ruleTile;
        for (RuleTileRule rule : ruleTile.rules) {
            if (matches(tilemap, layer, cellX, cellY, cell.spriteGuid, rule.neighbors, assets)) {
                return rule.spriteGuid.isBlank() ? cell.spriteGuid : rule.spriteGuid;
            }
        }
        return ruleTile.defaultSpriteGuid.isBlank() ? cell.spriteGuid : ruleTile.defaultSpriteGuid;
    }

    private static boolean matches(
            TilemapComponent tilemap,
            TilemapLayer layer,
            int cellX,
            int cellY,
            String logicalSpriteGuid,
            TileNeighborMask mask,
            AssetDatabase assets
    ) {
        return matchesConstraint(mask.north, neighborGroup(tilemap, layer, cellX, cellY - 1, logicalSpriteGuid, assets))
                && matchesConstraint(mask.northEast, neighborGroup(tilemap, layer, cellX + 1, cellY - 1, logicalSpriteGuid, assets))
                && matchesConstraint(mask.east, neighborGroup(tilemap, layer, cellX + 1, cellY, logicalSpriteGuid, assets))
                && matchesConstraint(mask.southEast, neighborGroup(tilemap, layer, cellX + 1, cellY + 1, logicalSpriteGuid, assets))
                && matchesConstraint(mask.south, neighborGroup(tilemap, layer, cellX, cellY + 1, logicalSpriteGuid, assets))
                && matchesConstraint(mask.southWest, neighborGroup(tilemap, layer, cellX - 1, cellY + 1, logicalSpriteGuid, assets))
                && matchesConstraint(mask.west, neighborGroup(tilemap, layer, cellX - 1, cellY, logicalSpriteGuid, assets))
                && matchesConstraint(mask.northWest, neighborGroup(tilemap, layer, cellX - 1, cellY - 1, logicalSpriteGuid, assets));
    }

    private static boolean matchesConstraint(TileNeighborConstraint constraint, String neighborGroup) {
        return switch (constraint) {
            case SAME -> neighborGroup != null && !neighborGroup.isEmpty();
            case NOT_SAME -> neighborGroup == null;
            default -> true;
        };
    }

    /**
     * @return rule-tile group id for neighbor cell, or {@code null} when empty
     */
    private static String neighborGroup(
            TilemapComponent tilemap,
            TilemapLayer layer,
            int cellX,
            int cellY,
            String logicalSpriteGuid,
            AssetDatabase assets
    ) {
        TilemapCell neighbor = layer.getCell(cellX, cellY);
        if (neighbor == null || neighbor.spriteGuid == null || neighbor.spriteGuid.isBlank()) {
            return null;
        }
        String myGroup = ruleTileGroupId(tilemap, logicalSpriteGuid, assets);
        String theirGroup = ruleTileGroupId(tilemap, neighbor.spriteGuid, assets);
        if (myGroup.isEmpty() || theirGroup.isEmpty()) {
            return null;
        }
        return myGroup.equals(theirGroup) ? myGroup : null;
    }

    private static String ruleTileGroupId(TilemapComponent tilemap, String spriteGuid, AssetDatabase assets) {
        TilesetDefinition tileset = assets.tileset(tilemap.tilesetTextureGuid);
        TileDefinition tile = tileset != null ? tileset.tileForSprite(spriteGuid) : null;
        if (tile == null) {
            tile = assets.tileDefinition(spriteGuid);
        }
        if (tile == null) {
            return "";
        }
        String group = tile.ruleTileGroupId();
        return group.isEmpty() ? spriteGuid : group;
    }
}
