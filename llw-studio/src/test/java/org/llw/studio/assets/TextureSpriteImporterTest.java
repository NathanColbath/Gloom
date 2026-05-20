package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextureSpriteImporterTest {
    @TempDir
    Path tempDir;

    private Path texturePath;
    private MetaFile.MetaData meta;

    @BeforeEach
    void setUp() throws Exception {
        Path assetsRoot = tempDir.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(tempDir, assetsRoot, assetsRoot);
        texturePath = assetsRoot.resolve("sheet.png");
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", texturePath.toFile());
        meta = MetaFile.read(tempDir, assetsRoot, texturePath);
    }

    @Test
    void applyGridSliceTwiceReplacesSprites() throws Exception {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;

        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);
        int firstCount = spriteCount(meta.importer);

        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);
        int secondCount = spriteCount(meta.importer);

        assertEquals(16, firstCount);
        assertEquals(16, secondCount);
    }

    @Test
    void applyGridSliceWithNewCellSizeReplacesNotAccumulates() throws Exception {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);
        assertEquals(16, spriteCount(meta.importer));

        settings.cellWidth = 32;
        settings.cellHeight = 32;
        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);
        assertEquals(4, spriteCount(meta.importer));
    }

    private static int spriteCount(ObjectNode importer) {
        ObjectNode texture = (ObjectNode) importer.get("texture");
        return texture.path("sprites").size();
    }
}
