package org.llw.studio.animation;

/**
 * Hold keyframe for a sprite track.
 */
public record SpriteKeyframe(float time, String spriteGuid) {
    public SpriteKeyframe {
        if (spriteGuid == null) {
            spriteGuid = "";
        }
    }
}
