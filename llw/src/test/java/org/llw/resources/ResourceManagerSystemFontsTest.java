package org.llw.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.llw.render.graphics.FontStyle;
import org.llw.render.graphics.system.SystemFonts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs(OS.WINDOWS)
class ResourceManagerSystemFontsTest {

    @Test
    void autoIndexesSystemFontsAndAcquiresByNameAndSize() {
        try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
            ResourceManager resources = harness.resources;
            assertFalse(resources.systemFontFaces().isEmpty());
            assertTrue(resources.hasSystemFontFace("Segoe UI", FontStyle.PLAIN));

            var font24 = resources.acquireSystemFont("Segoe UI", 24);
            assertTrue(font24.get().lineHeight() > 0f);
            assertTrue(resources.isLoaded(SystemFonts.assetId("Segoe UI", FontStyle.PLAIN, 24)));

            var font36 = resources.acquireSystemFont("Segoe UI", 36);
            assertTrue(font36.get().lineHeight() > font24.get().lineHeight());
            assertNotEquals(
                    SystemFonts.assetId("Segoe UI", FontStyle.PLAIN, 24),
                    SystemFonts.assetId("Segoe UI", FontStyle.PLAIN, 36)
            );

            font24.release();
            font36.release();
        }
    }
}
