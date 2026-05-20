package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpriteArtFacingTest {
    @Test
    void upArtAddsNinetyDegreePhase() {
        assertEquals(90f, SpriteArtFacing.UP.applyToRotation(0f), 0.001f);
        assertEquals(0f, SpriteArtFacing.RIGHT.applyToRotation(0f), 0.001f);
    }

    @Test
    void roundTripsInTextureMeta() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode importer = mapper.createObjectNode();
        TextureImportSettings settings = new TextureImportSettings();
        settings.artFacing = SpriteArtFacing.UP;
        TextureSpriteData.writeImportSettings(importer, settings);

        TextureImportSettings parsed = TextureSpriteData.readImportSettings(importer);
        assertEquals(SpriteArtFacing.UP, parsed.artFacing);
    }
}
