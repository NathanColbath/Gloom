package org.llw.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.render.graphics.Font;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FontFileTest {

    @Test
    void fromFileLoadsRoboto(@TempDir Path temp) throws Exception {
        try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
            Path fontFile = temp.resolve("Roboto-Regular.ttf");
            Files.write(fontFile, TestAssets.loadClasspath("llw/render/fonts/Roboto-Regular.ttf"));

            Font font = Font.fromFile(fontFile, 24);
            try {
                assertTrue(font.lineHeight() > 0f);
                assertTrue(font.glyph('A').advance() > 0f);
            } finally {
                font.dispose();
            }
        }
    }
}
