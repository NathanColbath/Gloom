package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.render.graphics.TextureFilter;
import org.llw.render.graphics.TextureWrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextureImportSettingsTest {
    @Test
    void roundTripsFilterAndWrap() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();
        TextureImportSettings settings = new TextureImportSettings();
        settings.filter = TextureFilter.POINT;
        settings.wrap = TextureWrap.REPEAT;
        TextureSpriteData.writeImportSettings(importer, settings);

        TextureImportSettings parsed = TextureSpriteData.readImportSettings(importer);
        assertEquals(TextureFilter.POINT, parsed.filter);
        assertEquals(TextureWrap.REPEAT, parsed.wrap);
        assertEquals(SpriteArtFacing.RIGHT, parsed.artFacing);
    }

    @Test
    void parsesAliases() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();
        ObjectNode texture = mapper.createObjectNode();
        texture.put("filter", "nearest");
        texture.put("wrap", "repeat");
        importer.set("texture", texture);

        TextureImportSettings parsed = TextureSpriteData.readImportSettings(importer);
        assertEquals(TextureFilter.POINT, parsed.filter);
        assertEquals(TextureWrap.REPEAT, parsed.wrap);
    }
}
