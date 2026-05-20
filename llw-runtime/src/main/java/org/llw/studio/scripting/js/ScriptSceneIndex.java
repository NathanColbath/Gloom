package org.llw.studio.scripting.js;

import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptComponent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects script asset GUIDs referenced by entities in a scene.
 */
public final class ScriptSceneIndex {
    private ScriptSceneIndex() {
    }

    /**
     * @param scene scene whose {@link ScriptComponent} instances are scanned
     * @return unique non-blank script GUIDs in stable iteration order
     */
    public static Set<String> collectGuids(Scene scene) {
        if (scene == null) {
            return Set.of();
        }
        Set<String> guids = new LinkedHashSet<>();
        var scripts = scene.world().store(ScriptComponent.class);
        for (int i = 0; i < scripts.size(); i++) {
            ScriptComponent component = scripts.componentAt(i);
            if (component == null) {
                continue;
            }
            for (var attachment : component.attachments) {
                if (attachment.hasScriptReference()) {
                    guids.add(attachment.scriptGuid);
                }
            }
        }
        return Collections.unmodifiableSet(guids);
    }
}
