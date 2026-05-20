package org.llw.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.render.graphics.FontStyle;
import org.llw.render.graphics.system.SystemFonts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerSystemFontsMockTest {

    @Test
    void acquireSystemFontLazilyRegistersSizedAsset(@TempDir Path temp) throws Exception {
        try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
            Path fontFile = temp.resolve("MockFont.ttf");
            Files.write(fontFile, TestAssets.loadClasspath("llw/render/fonts/Roboto-Regular.ttf"));

            ResourceManager resources = new ResourceManager(
                    harness.graphics.backend(),
                    harness.audio,
                    Map.of("Mock Family", fontFile)
            );

            String assetId = SystemFonts.assetId("Mock Family", FontStyle.PLAIN, 28);
            assertTrue(resources.systemFontFaces().contains("Mock Family"));

            var ref = resources.acquireSystemFont("Mock Family", 28);
            assertTrue(resources.isLoaded(assetId));
            assertTrue(ref.get().lineHeight() > 0f);
            assertEquals(1, resources.refCount(assetId));
            ref.release();
        }
    }
}
