package org.llw.studio.editor.assets;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SvgIconRasterizerTest {
    @Test
    void rasterizesClasspathSvg() throws IOException {
        byte[] svg;
        try (InputStream in = getClass().getResourceAsStream("/editor-icons/test-icon.svg")) {
            assertNotNull(in);
            svg = in.readAllBytes();
        }
        BufferedImage image = SvgIconRasterizer.rasterizeToImage(svg, 32);
        assertEquals(32, image.getWidth());
        assertEquals(32, image.getHeight());
        assertTrue(hasNonTransparentPixel(image));
    }

    private static boolean hasNonTransparentPixel(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
