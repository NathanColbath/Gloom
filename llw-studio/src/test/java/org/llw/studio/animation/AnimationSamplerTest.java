package org.llw.studio.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimationSamplerTest {
    @Test
    void spriteHoldAndFloatLerp() {
        AnimationClip clip = new AnimationClip();
        clip.length = 1f;
        AnimationTrack sprite = clip.trackOrCreate(AnimationTrackPaths.SPRITE, AnimationTrackType.SPRITE);
        sprite.spriteKeyframes.add(new SpriteKeyframe(0f, "a"));
        sprite.spriteKeyframes.add(new SpriteKeyframe(0.5f, "b"));

        AnimationTrack posX = clip.trackOrCreate(AnimationTrackPaths.POS_X, AnimationTrackType.FLOAT);
        posX.floatKeyframes.add(new FloatKeyframe(0f, 0f));
        posX.floatKeyframes.add(new FloatKeyframe(1f, 10f));

        AnimationSample at025 = AnimationSampler.sample(clip, 0.25f);
        assertEquals("a", at025.spriteGuid);
        assertTrue(at025.hasPosX);
        assertEquals(2.5f, at025.posX, 0.001f);

        AnimationSample at075 = AnimationSampler.sample(clip, 0.75f);
        assertEquals("b", at075.spriteGuid);
        assertEquals(7.5f, at075.posX, 0.001f);
    }
}
