package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackBootstrapTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void publishedDisplayName_prefersImporterDisplayName() {
        MetaFile.MetaData meta = new MetaFile.MetaData();
        meta.guid = "5b3dbdc6-e6c7-44ad-962e-d150edcc33e4";
        meta.type = AssetType.SCRIPT.name();
        meta.importer = MAPPER.createObjectNode().put("displayName", "Laser.ts");

        assertEquals("Laser.ts", PackBootstrap.publishedDisplayName(meta, meta.guid));
        assertEquals("fallback-guid", PackBootstrap.publishedDisplayName(new MetaFile.MetaData(), "fallback-guid"));
    }
}
