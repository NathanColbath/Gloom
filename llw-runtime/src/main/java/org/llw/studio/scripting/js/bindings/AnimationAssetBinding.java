package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only animation set metadata for scripts.
 */
public final class AnimationAssetBinding {
    private final String guid;
    private final AnimationSetDefinition set;

    public AnimationAssetBinding(AssetDatabase assets, String guid) {
        this.guid = guid == null ? "" : guid;
        this.set = assets == null ? null : assets.animationSet(guid);
    }

    @HostAccess.Export
    public String getGuid() {
        return guid;
    }

    @HostAccess.Export
    public String getDefaultState() {
        return set == null || set.defaultState == null ? "" : set.defaultState;
    }

    @HostAccess.Export
    public String[] getStates() {
        if (set == null) {
            return new String[0];
        }
        List<String> names = new ArrayList<>();
        for (AnimationStateDefinition state : set.states) {
            names.add(state.name());
        }
        return names.toArray(String[]::new);
    }

    @HostAccess.Export
    public AnimationStateInfoBinding getState(String name) {
        if (set == null || name == null) {
            return null;
        }
        AnimationStateDefinition state = set.findState(name);
        if (state == null) {
            return null;
        }
        return new AnimationStateInfoBinding(state.name(), state.clipGuid());
    }

    public static final class AnimationStateInfoBinding {
        private final String name;
        private final String clipGuid;

        public AnimationStateInfoBinding(String name, String clipGuid) {
            this.name = name;
            this.clipGuid = clipGuid;
        }

        @HostAccess.Export
        public String getName() {
            return name;
        }

        @HostAccess.Export
        public String getClipGuid() {
            return clipGuid;
        }
    }
}
