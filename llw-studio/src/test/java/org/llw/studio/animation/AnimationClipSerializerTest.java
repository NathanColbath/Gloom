package org.llw.studio.animation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnimationClipSerializerTest {
    @Test
    void roundTrip(@TempDir Path dir) throws Exception {
        AnimationClip clip = new AnimationClip();
        clip.length = 0.8f;
        clip.frameRate = 12f;
        clip.loop = true;
        clip.ensureDefaultTracks();
        clip.trackOrCreate(AnimationTrackPaths.SPRITE, AnimationTrackType.SPRITE)
                .spriteKeyframes.add(new SpriteKeyframe(0f, "sprite-1"));
        clip.findTrack(AnimationTrackPaths.POS_X).floatKeyframes.add(new FloatKeyframe(0f, 1f));

        Path path = dir.resolve("Walk.anim.json");
        AnimationClipSerializer.save(path, clip);
        AnimationClip loaded = AnimationClipSerializer.load(path);

        assertEquals(clip.length, loaded.length, 0.001f);
        assertEquals(clip.frameRate, loaded.frameRate, 0.001f);
        assertEquals(clip.loop, loaded.loop);
        assertEquals(1, loaded.findTrack(AnimationTrackPaths.SPRITE).spriteKeyframes.size());
        assertEquals("sprite-1", loaded.findTrack(AnimationTrackPaths.SPRITE).spriteKeyframes.get(0).spriteGuid());
        assertEquals(1f, loaded.findTrack(AnimationTrackPaths.POS_X).floatKeyframes.get(0).value(), 0.001f);
    }
}
