package org.llw.studio.systems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.assets.*;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnimationSystemStateTest {
    @Test
    void resolvesStateClip(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        Path animPath = assetsRoot.resolve("Hero.animation.json");
        AnimationSetSerializer.save(animPath, new org.llw.studio.assets.AnimationSetDefinition());
        MetaFile.MetaData animMeta = MetaFile.read(projectRoot, assetsRoot, animPath);
        animMeta.type = AssetType.ANIMATION.name();
        MetaFile.write(projectRoot, assetsRoot, animPath, animMeta);

        Path idlePath = assetsRoot.resolve("Idle.anim.json");
        org.llw.studio.animation.AnimationClip clip = new org.llw.studio.animation.AnimationClip();
        clip.length = 0.5f;
        org.llw.studio.animation.AnimationClipSerializer.save(idlePath, clip);
        MetaFile.MetaData idleMeta = MetaFile.read(projectRoot, assetsRoot, idlePath);
        idleMeta.type = AssetType.ANIMATION_CLIP.name();
        MetaFile.write(projectRoot, assetsRoot, idlePath, idleMeta);

        var set = org.llw.studio.assets.AnimationSetSerializer.load(animPath);
        set.defaultState = "Idle";
        set.states.add(new org.llw.studio.assets.AnimationStateDefinition("Idle", idleMeta.guid));
        set.clips.add(new org.llw.studio.assets.AnimationClipEntry(idleMeta.guid, "Idle", "Idle.anim.json"));
        AnimationSetSerializer.save(animPath, set);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        Scene scene = new Scene();
        var object = scene.createGameObject("Hero");
        Animation2DComponent anim = new Animation2DComponent();
        anim.animationGuid = animMeta.guid;
        anim.currentState = "Idle";
        anim.defaultState = "Idle";
        anim.playOnStart = true;
        anim.loop = false;
        object.addComponent(Animation2DComponent.class, anim);
        object.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

        AnimationSystem system = new AnimationSystem(assets);
        scene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, system);
        system.playState(object.entity(), "Idle");
        scene.world().scheduler().update(scene.world(), 0.6f);
        assertEquals(1f, system.normalizedTime(object.entity()), 0.05f);
    }

    @Test
    void playStateDoesNotRestartWhenAlreadyInState(@TempDir Path projectRoot) throws Exception {
        Path assetsRoot = projectRoot.resolve("Assets");
        Files.createDirectories(assetsRoot);
        MetaFile.read(projectRoot, assetsRoot, assetsRoot);
        Path animPath = assetsRoot.resolve("Hero.animation.json");
        AnimationSetSerializer.save(animPath, new org.llw.studio.assets.AnimationSetDefinition());
        MetaFile.MetaData animMeta = MetaFile.read(projectRoot, assetsRoot, animPath);
        animMeta.type = AssetType.ANIMATION.name();
        MetaFile.write(projectRoot, assetsRoot, animPath, animMeta);

        Path walkPath = assetsRoot.resolve("Walk.anim.json");
        org.llw.studio.animation.AnimationClip clip = new org.llw.studio.animation.AnimationClip();
        clip.length = 1f;
        org.llw.studio.animation.AnimationClipSerializer.save(walkPath, clip);
        MetaFile.MetaData walkMeta = MetaFile.read(projectRoot, assetsRoot, walkPath);
        walkMeta.type = AssetType.ANIMATION_CLIP.name();
        MetaFile.write(projectRoot, assetsRoot, walkPath, walkMeta);

        var set = AnimationSetSerializer.load(animPath);
        set.defaultState = "Walk";
        set.states.add(new org.llw.studio.assets.AnimationStateDefinition("Walk", walkMeta.guid));
        set.clips.add(new org.llw.studio.assets.AnimationClipEntry(walkMeta.guid, "Walk", "Walk.anim.json"));
        AnimationSetSerializer.save(animPath, set);

        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        assets.refresh();

        Scene scene = new Scene();
        var object = scene.createGameObject("Hero");
        Animation2DComponent anim = new Animation2DComponent();
        anim.animationGuid = animMeta.guid;
        anim.currentState = "Walk";
        anim.defaultState = "Walk";
        anim.playOnStart = true;
        object.addComponent(Animation2DComponent.class, anim);

        AnimationSystem system = new AnimationSystem(assets);
        scene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, system);
        system.playState(object.entity(), "Walk");
        scene.world().scheduler().update(scene.world(), 0.25f);
        float afterAdvance = system.normalizedTime(object.entity());
        assertTrue(afterAdvance > 0.1f, "expected playback to advance");
        system.playState(object.entity(), "Walk");
        assertEquals(afterAdvance, system.normalizedTime(object.entity()), 0.001f);
    }

    @Test
    void legacyClipGuidStillWorks() {
        Animation2DComponent anim = new Animation2DComponent();
        anim.clipGuid = "legacy-guid";
        assertEquals("legacy-guid", AnimationSystem.resolveClipGuid(anim, null));
    }
}
