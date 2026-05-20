package org.llw.studio.build;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildIconWriterTest {
    @Test
    void writeIcoCreatesFile(@TempDir Path tempDir) throws Exception {
        Path png = tempDir.resolve("icon.png");
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                image.setRGB(x, y, Color.RED.getRGB());
            }
        }
        ImageIO.write(image, "PNG", png.toFile());

        Path ico = tempDir.resolve("out.ico");
        BuildIconWriter.writeIco(png, ico);

        assertTrue(Files.isRegularFile(ico));
        assertTrue(Files.size(ico) > 32);
    }
}
