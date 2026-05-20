package org.llw.studio.editor.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationSetSerializer;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationClipSerializer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetBrowserNestTest {
    @Test
    void hidesClipUnderParentAnimationInSameFolder(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);

        Path animPath = assetsRoot.resolve("Hero.animation.json");
        AnimationSetSerializer.save(animPath, new AnimationSetDefinition());
        MetaFile.MetaData animMeta = MetaFile.read(projectRoot, assetsRoot, animPath);
        animMeta.type = AssetType.ANIMATION.name();
        MetaFile.write(projectRoot, assetsRoot, animPath, animMeta);

        Path clipPath = assetsRoot.resolve("Idle.anim.json");
        AnimationClip clip = new AnimationClip();
        AnimationClipSerializer.save(clipPath, clip);
        MetaFile.MetaData clipMeta = MetaFile.read(projectRoot, assetsRoot, clipPath);
        clipMeta.type = AssetType.ANIMATION_CLIP.name();
        MetaFile.write(projectRoot, assetsRoot, clipPath, clipMeta);

        var set = AnimationSetSerializer.load(animPath);
        set.states.add(new AnimationStateDefinition("Idle", clipMeta.guid));
        AnimationSetSerializer.save(animPath, set);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        StudioAsset clipAsset = assets.get(clipMeta.guid);
        assertTrue(AssetBrowserNest.isClipShownUnderAnimation(assets, clipAsset));
        List<StudioAsset> top = AssetBrowserNest.childrenOf(assets, assets.rootGuid());
        assertTrue(top.stream().noneMatch(a -> a.guid().equals(clipMeta.guid)));
    }

    @Test
    void animationStatesBecomeNestChildren(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);

        Path animPath = assetsRoot.resolve("Hero.animation.json");
        AnimationSetSerializer.save(animPath, new AnimationSetDefinition());
        MetaFile.MetaData animMeta = MetaFile.read(projectRoot, assetsRoot, animPath);
        animMeta.type = AssetType.ANIMATION.name();
        MetaFile.write(projectRoot, assetsRoot, animPath, animMeta);

        Path clipPath = assetsRoot.resolve("Idle.anim.json");
        AnimationClipSerializer.save(clipPath, new AnimationClip());
        MetaFile.MetaData clipMeta = MetaFile.read(projectRoot, assetsRoot, clipPath);
        clipMeta.type = AssetType.ANIMATION_CLIP.name();
        MetaFile.write(projectRoot, assetsRoot, clipPath, clipMeta);

        var set = AnimationSetSerializer.load(animPath);
        set.states.add(new AnimationStateDefinition("Idle", clipMeta.guid));
        AnimationSetSerializer.save(animPath, set);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        StudioAsset anim = assets.get(animMeta.guid);
        List<AssetBrowserNest.NestChild> children = AssetBrowserNest.nestChildren(assets, anim);
        assertEquals(1, children.size());
        assertEquals("Idle", children.get(0).displayLabel());
        assertEquals(clipMeta.guid, children.get(0).asset().guid());
        assertEquals(AssetIconKind.ANIMATION_STATE, children.get(0).iconKind());
    }

    @Test
    void parentFolderGuidResolvesParent(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);

        Path sub = assetsRoot.resolve("Sub");
        Files.createDirectories(sub);
        MetaFile.read(projectRoot, assetsRoot, sub);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        StudioAsset subFolder = assets.getByPath(sub);
        assertNotNull(subFolder);
        assertEquals(assets.rootGuid(), assets.parentFolderGuid(subFolder.guid()));
    }
}
