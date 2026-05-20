package org.llw.studio.animation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A single animated property on a clip.
 */
public final class AnimationTrack {
    public String path = "";
    public AnimationTrackType type = AnimationTrackType.FLOAT;
    public final List<SpriteKeyframe> spriteKeyframes = new ArrayList<>();
    public final List<FloatKeyframe> floatKeyframes = new ArrayList<>();

    public AnimationTrack copy() {
        AnimationTrack copy = new AnimationTrack();
        copy.path = path;
        copy.type = type;
        for (SpriteKeyframe key : spriteKeyframes) {
            copy.spriteKeyframes.add(new SpriteKeyframe(key.time(), key.spriteGuid()));
        }
        for (FloatKeyframe key : floatKeyframes) {
            copy.floatKeyframes.add(new FloatKeyframe(key.time(), key.value()));
        }
        return copy;
    }

    public void sortKeyframes() {
        spriteKeyframes.sort(Comparator.comparing(SpriteKeyframe::time));
        floatKeyframes.sort(Comparator.comparing(FloatKeyframe::time));
    }

    public static AnimationTrack spriteTrack() {
        AnimationTrack track = new AnimationTrack();
        track.path = AnimationTrackPaths.SPRITE;
        track.type = AnimationTrackType.SPRITE;
        return track;
    }

    public static AnimationTrack floatTrack(String path) {
        AnimationTrack track = new AnimationTrack();
        track.path = path;
        track.type = AnimationTrackType.FLOAT;
        return track;
    }
}
