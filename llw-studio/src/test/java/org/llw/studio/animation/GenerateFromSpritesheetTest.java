package org.llw.studio.animation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.SpriteSliceSettings;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TextureSpriteImporter;
import org.llw.studio.editor.animation.AnimationClipActions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GenerateFromSpritesheetTest {
    @Test
    void spacesKeysByFrameRate(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        Path texturePath = assetsRoot.resolve("sheet.png");
        BufferedImage image = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", texturePath.toFile());
        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, texturePath);
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        TextureSpriteImporter.applyGridSlice(texturePath, meta, settings);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.saveTextureSprites(texturePath, meta);
        StudioAsset texture = assets.getByPath(texturePath);
        assertNotNull(texture, "texture asset should be indexed after save");
        String textureGuid = texture.guid();
        int sliceCount = assets.spriteChildren(textureGuid).size();
        assertTrue(sliceCount >= 2, "expected sliced sprites, got " + sliceCount);

        AnimationClip clip = new AnimationClip();
        clip.frameRate = 10f;
        AnimationClipActions.generateFromSpritesheet(assets, clip, textureGuid);

        AnimationTrack track = clip.findTrack(AnimationTrackPaths.SPRITE);
        assertEquals(assets.spriteChildren(textureGuid).size(), track.spriteKeyframes.size());
        assertEquals(0f, track.spriteKeyframes.get(0).time(), 0.001f);
        assertEquals(0.1f, track.spriteKeyframes.get(1).time(), 0.001f);
        float step = 1f / clip.frameRate;
        assertEquals(step * (track.spriteKeyframes.size() - 1), clip.length, 0.001f);
    }
}
