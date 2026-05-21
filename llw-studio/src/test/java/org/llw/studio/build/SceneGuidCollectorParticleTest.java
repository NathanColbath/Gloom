package org.llw.studio.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneGuidCollectorParticleTest {
    @Test
    void collectFromPrefabObjects_readsParticleEmitter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode emitter = root.putObject("particleEmitter");
        emitter.put("particleSystemGuid", "particle-guid-1");

        Set<String> guids = SceneGuidCollector.collectFromPrefabObjects(java.util.List.of(root));
        assertTrue(guids.contains("particle-guid-1"));
    }
}
