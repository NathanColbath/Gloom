package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityTagSerializationTest {
    @Test
    void writeAndReadPreservesNameAndTag() {
        Scene scene = new Scene();
        var a = scene.createGameObject("Player");
        a.setTag("Player");

        ObjectNode node = SceneObjectSerializer.writeObject(scene, a.entity(), 1, -1);
        assertEquals("Player", node.path("name").asText());
        assertEquals("Player", node.path("tag").asText());

        Scene loaded = new Scene();
        var restored = SceneObjectSerializer.readObject(loaded, node, 1);
        NameComponent identity = restored.getComponent(NameComponent.class);
        assertNotNull(identity);
        assertEquals("Player", identity.name());
        assertEquals("Player", identity.tag());
    }

    @Test
    void readWithoutTagFieldDefaultsToEmpty() {
        Scene scene = new Scene();
        ObjectNode node = SceneObjectSerializer.writeObject(scene, scene.createGameObject("Obj").entity(), 0, -1);
        ((ObjectNode) node).remove("tag");

        Scene loaded = new Scene();
        var restored = SceneObjectSerializer.readObject(loaded, node, 0);
        assertEquals("", restored.tag());
    }

    @Test
    void multipleEntitiesCanShareTag() {
        Scene scene = new Scene();
        var first = scene.createGameObject("Bullet");
        first.setTag("Bullet");
        var second = scene.createGameObject("Bullet (1)");
        second.setTag("Bullet");

        var store = scene.world().store(NameComponent.class);
        int matches = 0;
        for (int i = 0; i < store.size(); i++) {
            if ("Bullet".equals(store.componentAt(i).tag())) {
                matches++;
            }
        }
        assertEquals(2, matches);
    }
}
