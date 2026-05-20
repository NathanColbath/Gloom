package org.llw.studio.assets;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory model for a parent animation asset ({@code .animation.json}).
 */
public final class AnimationSetDefinition {
    public int version = 1;
    public String defaultState = "Idle";
    public final List<AnimationStateDefinition> states = new ArrayList<>();
    public final List<AnimationClipEntry> clips = new ArrayList<>();

    public AnimationSetDefinition copy() {
        AnimationSetDefinition copy = new AnimationSetDefinition();
        copy.version = version;
        copy.defaultState = defaultState;
        for (AnimationStateDefinition state : states) {
            copy.states.add(new AnimationStateDefinition(state.name(), state.clipGuid()));
        }
        for (AnimationClipEntry clip : clips) {
            copy.clips.add(new AnimationClipEntry(clip.guid(), clip.name(), clip.path()));
        }
        return copy;
    }

    public AnimationStateDefinition findState(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (AnimationStateDefinition state : states) {
            if (name.equals(state.name())) {
                return state;
            }
        }
        return null;
    }

    public String clipGuidForState(String stateName) {
        AnimationStateDefinition state = findState(stateName);
        return state == null ? null : state.clipGuid();
    }
}
