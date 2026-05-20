package org.llw.studio.systems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationClipSerializer;
import org.llw.studio.animation.AnimationTrackPaths;
import org.llw.studio.animation.AnimationTrackType;
import org.llw.studio.animation.FloatKeyframe;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnimationSystemTest {
    @Test
    void advancesTimeAndStopsAtEnd(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        Path clipPath = assetsRoot.resolve("test.anim.json");
        AnimationClip clip = new AnimationClip();
        clip.length = 0.5f;
        clip.loop = false;
        clip.trackOrCreate(AnimationTrackPaths.POS_X, AnimationTrackType.FLOAT)
                .floatKeyframes.add(new FloatKeyframe(0f, 0f));
        AnimationClipSerializer.save(clipPath, clip);
        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, clipPath);
        meta.type = AssetType.ANIMATION_CLIP.name();
        MetaFile.write(projectRoot, assetsRoot, clipPath, meta);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        Scene scene = new Scene();
        var object = scene.createGameObject("Player");
        Animation2DComponent anim = new Animation2DComponent();
        anim.clipGuid = meta.guid;
        anim.playOnStart = true;
        anim.loop = false;
        object.addComponent(Animation2DComponent.class, anim);
        object.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

        AnimationSystem system = new AnimationSystem(assets);
        scene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, system);
        scene.world().scheduler().update(scene.world(), 0.3f);
        assertTrue(system.normalizedTime(object.entity()) < 1f);
        scene.world().scheduler().update(scene.world(), 0.3f);
        assertEquals(1f, system.normalizedTime(object.entity()), 0.01f);
    }
}
