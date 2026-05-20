package org.llw.studio.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialized animation clip with sprite and transform tracks.
 */
public final class AnimationClip {
    public int version = 1;
    public float length = 1f;
    public float frameRate = 12f;
    public boolean loop = true;
    public final List<AnimationTrack> tracks = new ArrayList<>();

    public AnimationClip copy() {
        AnimationClip copy = new AnimationClip();
        copy.version = version;
        copy.length = length;
        copy.frameRate = frameRate;
        copy.loop = loop;
        for (AnimationTrack track : tracks) {
            copy.tracks.add(track.copy());
        }
        return copy;
    }

    public AnimationTrack findTrack(String path) {
        for (AnimationTrack track : tracks) {
            if (path.equals(track.path)) {
                return track;
            }
        }
        return null;
    }

    public AnimationTrack trackOrCreate(String path, AnimationTrackType type) {
        AnimationTrack existing = findTrack(path);
        if (existing != null) {
            return existing;
        }
        AnimationTrack track = type == AnimationTrackType.SPRITE
                ? AnimationTrack.spriteTrack()
                : AnimationTrack.floatTrack(path);
        if (type == AnimationTrackType.FLOAT) {
            track.path = path;
        }
        tracks.add(track);
        return track;
    }

    public void ensureDefaultTracks() {
        trackOrCreate(AnimationTrackPaths.SPRITE, AnimationTrackType.SPRITE);
        for (String path : AnimationTrackPaths.defaultFloatPaths()) {
            trackOrCreate(path, AnimationTrackType.FLOAT);
        }
    }

    public float snapTime(float time) {
        if (frameRate <= 0f) {
            return time;
        }
        float step = 1f / frameRate;
        return Math.round(time / step) * step;
    }
}
