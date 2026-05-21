package org.llw.studio.particles;

import org.llw.studio.particles.model.ParticleSystemDocument;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects asset GUID references from a {@link ParticleSystemDocument}.
 */
public final class ParticleSystemRefs {
    private ParticleSystemRefs() {
    }

    /**
     * @param document particle system document
     * @return transitive GUID dependencies (sprites, shader graphs, sub-emitters)
     */
    public static Set<String> collectGuids(ParticleSystemDocument document) {
        Set<String> guids = new LinkedHashSet<>();
        if (document == null || document.modules == null) {
            return guids;
        }
        add(guids, document.modules.renderer.spriteGuid);
        add(guids, document.modules.renderer.shaderGraphGuid);
        for (ParticleSystemDocument.SubEmitterModule sub : document.modules.subEmitters) {
            add(guids, sub.systemGuid);
        }
        return guids;
    }

    private static void add(Set<String> guids, String guid) {
        if (guid != null && !guid.isBlank()) {
            guids.add(guid);
        }
    }
}
