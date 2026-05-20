package org.llw.studio.ecs.components;

/**
 * Plays clips from a parent animation asset via named states.
 */
public final class Animation2DComponent implements Cloneable {
    public String animationGuid = "";
    public String defaultState = "Idle";
    public String currentState = "Idle";
    /** @deprecated Use {@link #animationGuid} and states; kept for legacy scenes. */
    @Deprecated
    public String clipGuid = "";
    public boolean playOnStart = true;
    public float speed = 1f;
    public boolean loop = true;

    public Animation2DComponent copy() {
        Animation2DComponent copy = new Animation2DComponent();
        copy.animationGuid = animationGuid;
        copy.defaultState = defaultState;
        copy.currentState = currentState;
        copy.clipGuid = clipGuid;
        copy.playOnStart = playOnStart;
        copy.speed = speed;
        copy.loop = loop;
        return copy;
    }

    @Override
    public Animation2DComponent clone() {
        return copy();
    }
}
