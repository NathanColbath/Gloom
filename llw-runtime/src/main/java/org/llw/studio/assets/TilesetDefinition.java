package org.llw.studio.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tileset metadata for a texture: cell size and per-sprite tile definitions.
 */
public final class TilesetDefinition {
    public final String textureGuid;
    public int cellWidth = 32;
    public int cellHeight = 32;
    public final List<TileDefinition> tiles = new ArrayList<>();

    public TilesetDefinition(String textureGuid) {
        this.textureGuid = textureGuid == null ? "" : textureGuid;
    }

    /**
     * Builds a tile entry for every sprite slice when tile meta is missing.
     */
    public static TilesetDefinition fromSprites(String textureGuid, List<SpriteDefinition> sprites, int cellW, int cellH) {
        TilesetDefinition def = new TilesetDefinition(textureGuid);
        def.cellWidth = cellW;
        def.cellHeight = cellH;
        for (SpriteDefinition sprite : sprites) {
            def.tiles.add(new TileDefinition(sprite.guid()));
        }
        return def;
    }

    /** @return tile definition for a sprite GUID, or {@code null} */
    public TileDefinition tileForSprite(String spriteGuid) {
        if (spriteGuid == null || spriteGuid.isBlank()) {
            return null;
        }
        for (TileDefinition tile : tiles) {
            if (spriteGuid.equals(tile.spriteGuid)) {
                return tile;
            }
        }
        return null;
    }

    /** @return map from sprite GUID to tile definition */
    public Map<String, TileDefinition> tilesBySpriteGuid() {
        Map<String, TileDefinition> map = new HashMap<>();
        for (TileDefinition tile : tiles) {
            if (tile.spriteGuid != null && !tile.spriteGuid.isBlank()) {
                map.put(tile.spriteGuid, tile);
            }
        }
        return map;
    }
}
