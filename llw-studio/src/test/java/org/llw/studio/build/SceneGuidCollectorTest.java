package org.llw.studio.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneGuidCollectorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void collectFromPrefabObjects_readsSceneSerializationFormat() throws Exception {
        var root = MAPPER.readTree("""
                [{
                  "spriteRenderer" : {
                    "spriteGuid" : "sprite-guid",
                    "textureGuid" : "texture-guid"
                  },
                  "scripts" : [ {
                    "scriptGuid" : "script-guid",
                    "fields" : {
                      "nestedPrefab" : { "prefab" : "nested-prefab-guid" }
                    }
                  } ]
                }]
                """);

        Set<String> guids = SceneGuidCollector.collectFromPrefabObjects(List.of(root.get(0)));

        assertTrue(guids.contains("sprite-guid"));
        assertTrue(guids.contains("texture-guid"));
        assertTrue(guids.contains("script-guid"));
        assertTrue(guids.contains("nested-prefab-guid"));
    }
}
