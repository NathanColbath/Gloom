package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TextureSpriteDataTest {
    @Test
    void replaceSpritesOverwritesArray() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();
        SpriteDefinition first = new SpriteDefinition(
                "a", "one", "tex", 0, 0, 16, 16, 0.5f, 0.5f, 64, 64);
        SpriteDefinition second = new SpriteDefinition(
                "b", "two", "tex", 16, 0, 16, 16, 0.5f, 0.5f, 64, 64);
        TextureSpriteData.replaceSprites(importer, List.of(first, second));
        assertEquals(2, importer.path("texture").path("sprites").size());

        SpriteDefinition only = new SpriteDefinition(
                "c", "only", "tex", 0, 0, 32, 32, 0.5f, 0.5f, 64, 64);
        TextureSpriteData.replaceSprites(importer, List.of(only));
        assertEquals(1, importer.path("texture").path("sprites").size());
        assertEquals("c", importer.path("texture").path("sprites").get(0).path("guid").asText());
    }

    @Test
    void parseAndRoundTripSprites() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();
        TextureSpriteData.setSpriteMode(importer, "multiple");
        SpriteDefinition sprite = new SpriteDefinition(
                "guid-1",
                "tile_0",
                "tex-guid",
                0,
                0,
                32,
                32,
                0.5f,
                0.5f,
                128,
                128
        );
        TextureSpriteData.writeSprites(importer, List.of(sprite), null);
        List<SpriteDefinition> parsed = TextureSpriteData.parseSprites(importer, "tex-guid", 128, 128);
        assertEquals(1, parsed.size());
        assertEquals("guid-1", parsed.get(0).guid());
        assertEquals(32, parsed.get(0).width());
        assertNotNull(parsed.get(0).uvRect());
        assertEquals(31f / 128f, parsed.get(0).uvRect().width, 0.001f);
    }
}
