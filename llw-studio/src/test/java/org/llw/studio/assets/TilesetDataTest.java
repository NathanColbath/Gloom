package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TilesetDataTest {
    @Test
    void parseAndWriteRoundTrip() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();

        TilesetDefinition def = new TilesetDefinition("tex-1");
        def.cellWidth = 16;
        def.cellHeight = 16;

        TileDefinition tile = new TileDefinition("sprite-1");
        tile.collision = TileCollisionType.SOLID;
        RuleTileDefinition ruleTile = new RuleTileDefinition();
        ruleTile.defaultSpriteGuid = "sprite-1";
        TileNeighborMask mask = new TileNeighborMask();
        mask.north = TileNeighborConstraint.SAME;
        mask.east = TileNeighborConstraint.NOT_SAME;
        ruleTile.rules.add(new RuleTileRule(mask, "sprite-corner"));
        tile.ruleTile = ruleTile;
        def.tiles.add(tile);

        TilesetData.write(importer, def);
        SpriteDefinition sprite = new SpriteDefinition(
                "sprite-1", "t0", "tex-1", 0, 0, 16, 16, 0.5f, 0.5f, 64, 64);
        TilesetDefinition parsed = TilesetData.parse(importer, "tex-1", List.of(sprite));

        assertEquals(16, parsed.cellWidth);
        assertEquals(16, parsed.cellHeight);
        assertEquals(1, parsed.tiles.size());
        TileDefinition parsedTile = parsed.tiles.get(0);
        assertEquals("sprite-1", parsedTile.spriteGuid);
        assertEquals(TileCollisionType.SOLID, parsedTile.collision);
        assertNotNull(parsedTile.ruleTile);
        assertEquals("sprite-1", parsedTile.ruleTile.defaultSpriteGuid);
        assertEquals(1, parsedTile.ruleTile.rules.size());
        assertEquals(TileNeighborConstraint.SAME, parsedTile.ruleTile.rules.get(0).neighbors.north);
        assertEquals("sprite-corner", parsedTile.ruleTile.rules.get(0).spriteGuid);
    }

    @Test
    void parseCreatesTilesFromSpritesWhenMissing() {
        ObjectNode importer = new ObjectMapper().createObjectNode();
        SpriteDefinition s1 = new SpriteDefinition("a", "one", "tex", 0, 0, 32, 32, 0.5f, 0.5f, 64, 64);
        SpriteDefinition s2 = new SpriteDefinition("b", "two", "tex", 32, 0, 32, 32, 0.5f, 0.5f, 64, 64);
        TextureSpriteData.writeSliceSettings(importer, new SpriteSliceSettings());
        TilesetDefinition def = TilesetData.parse(importer, "tex", List.of(s1, s2));
        assertEquals(2, def.tiles.size());
    }
}
