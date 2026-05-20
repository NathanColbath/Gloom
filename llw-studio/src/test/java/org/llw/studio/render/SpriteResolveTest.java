package org.llw.studio.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.math.geometry.RectF;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.SpriteSliceSettings;
import org.llw.studio.assets.TextureSpriteImporter;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteResolveTest {
    @TempDir
    Path tempDir;

    private Path projectRoot;
    private Path texturePath;
    private String textureGuid;
    private String sliceGuid;
    private AssetDatabase assets;

    @BeforeEach
    void setUp() throws Exception {
        projectRoot = tempDir;
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        texturePath = assetsRoot.resolve("sheet.png");
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", texturePath.toFile());

        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, texturePath);
        textureGuid = meta.guid;
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);
        MetaFile.write(projectRoot, assetsRoot, texturePath, meta);

        assets = new AssetDatabase(projectRoot, null);
        assets.refresh();
        sliceGuid = assets.spriteChildren(textureGuid).get(0).guid();
    }

    @Test
    void resolveSliceByGuidReturnsValidUvRect() {
        SpriteRendererComponent renderer = new SpriteRendererComponent();
        renderer.spriteGuid = sliceGuid;

        var slice = SpriteResolve.resolve(assets, renderer);
        assertNotNull(slice);
        assertEquals(textureGuid, slice.textureGuid());
        assertEquals(16, slice.width());
        assertEquals(16, slice.height());

        RectF uv = slice.uvRect(64, 64);
        assertTrue(uv.left >= 0f && uv.top >= 0f);
        assertTrue(uv.left + uv.width <= 1.001f);
        assertTrue(uv.top + uv.height <= 1.001f);
        assertEquals(0.25f, uv.width, 0.02f);
        assertEquals(0.25f, uv.height, 0.02f);
        float vAtlasTop = uv.top + uv.height;
        float vAtlasBottom = uv.top;
        assertTrue(vAtlasTop > vAtlasBottom, "screen top must sample higher V after STB flip");
    }
}
