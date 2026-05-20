package org.llw.studio.animation;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.physics.PhysicsTransformSync;

/**
 * Samples animation clips and applies results to ECS components.
 */
public final class AnimationSampler {
    private AnimationSampler() {
    }

    public static AnimationSample sample(AnimationClip clip, float time) {
        AnimationSample sample = new AnimationSample();
        if (clip == null) {
            return sample;
        }
        float t = clampTime(clip, time);
        for (AnimationTrack track : clip.tracks) {
            if (track.type == AnimationTrackType.SPRITE) {
                String sprite = sampleSprite(track, t);
                if (sprite != null && !sprite.isBlank()) {
                    sample.spriteGuid = sprite;
                    sample.hasSprite = true;
                }
            } else {
                Float value = sampleFloat(track, t);
                if (value != null) {
                    applyFloat(sample, track.path, value);
                }
            }
        }
        return sample;
    }

    public static void apply(World world, EntityId entity, AnimationClip clip, float time, AssetDatabase assets) {
        AnimationSample sample = sample(clip, time);
        applySample(world, entity, sample);
        if (assets != null) {
            PhysicsTransformSync.markDirty(world, entity);
        }
    }

    public static void applySample(World world, EntityId entity, AnimationSample sample) {
        if (world == null || entity == null || entity.isNone() || sample == null) {
            return;
        }
        if (sample.hasSprite) {
            SpriteRendererComponent sprite = world.getComponent(entity, SpriteRendererComponent.class);
            if (sprite != null) {
                sprite.spriteGuid = sample.spriteGuid;
            }
        }
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform == null) {
            return;
        }
        if (sample.hasPosX) {
            transform.x = sample.posX;
        }
        if (sample.hasPosY) {
            transform.y = sample.posY;
        }
        if (sample.hasRotation) {
            transform.rotation = sample.rotation;
        }
        if (sample.hasScaleX) {
            transform.scaleX = sample.scaleX;
        }
        if (sample.hasScaleY) {
            transform.scaleY = sample.scaleY;
        }
    }

    private static float clampTime(AnimationClip clip, float time) {
        if (clip.length <= 0f) {
            return 0f;
        }
        if (time < 0f) {
            return 0f;
        }
        if (time > clip.length) {
            return clip.length;
        }
        return time;
    }

    private static String sampleSprite(AnimationTrack track, float time) {
        if (track.spriteKeyframes.isEmpty()) {
            return null;
        }
        String result = track.spriteKeyframes.get(0).spriteGuid();
        for (SpriteKeyframe key : track.spriteKeyframes) {
            if (key.time() <= time) {
                result = key.spriteGuid();
            } else {
                break;
            }
        }
        return result;
    }

    private static Float sampleFloat(AnimationTrack track, float time) {
        if (track.floatKeyframes.isEmpty()) {
            return null;
        }
        if (track.floatKeyframes.size() == 1) {
            return track.floatKeyframes.get(0).value();
        }
        FloatKeyframe first = track.floatKeyframes.get(0);
        if (time <= first.time()) {
            return first.value();
        }
        FloatKeyframe last = track.floatKeyframes.get(track.floatKeyframes.size() - 1);
        if (time >= last.time()) {
            return last.value();
        }
        for (int i = 0; i < track.floatKeyframes.size() - 1; i++) {
            FloatKeyframe a = track.floatKeyframes.get(i);
            FloatKeyframe b = track.floatKeyframes.get(i + 1);
            if (time >= a.time() && time <= b.time()) {
                float span = b.time() - a.time();
                if (span <= 0f) {
                    return a.value();
                }
                float t = (time - a.time()) / span;
                return a.value() + (b.value() - a.value()) * t;
            }
        }
        return last.value();
    }

    private static void applyFloat(AnimationSample sample, String path, float value) {
        if (AnimationTrackPaths.POS_X.equals(path)) {
            sample.hasPosX = true;
            sample.posX = value;
        } else if (AnimationTrackPaths.POS_Y.equals(path)) {
            sample.hasPosY = true;
            sample.posY = value;
        } else if (AnimationTrackPaths.ROTATION.equals(path)) {
            sample.hasRotation = true;
            sample.rotation = value;
        } else if (AnimationTrackPaths.SCALE_X.equals(path)) {
            sample.hasScaleX = true;
            sample.scaleX = value;
        } else if (AnimationTrackPaths.SCALE_Y.equals(path)) {
            sample.hasScaleY = true;
            sample.scaleY = value;
        }
    }
}
