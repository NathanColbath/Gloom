package org.llw.studio.animation;

/**
 * Standard animation property paths for built-in tracks.
 */
public final class AnimationTrackPaths {
    public static final String SPRITE = "SpriteRenderer.spriteGuid";
    public static final String POS_X = "Transform2D.x";
    public static final String POS_Y = "Transform2D.y";
    public static final String ROTATION = "Transform2D.rotation";
    public static final String SCALE_X = "Transform2D.scaleX";
    public static final String SCALE_Y = "Transform2D.scaleY";

    private AnimationTrackPaths() {
    }

    public static String[] defaultFloatPaths() {
        return new String[] {POS_X, POS_Y, ROTATION, SCALE_X, SCALE_Y};
    }
}
