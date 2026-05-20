package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.physics.PhysicsBodyType;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PhysicsSerializationTest {
    @Test
    void rigidbodyAndBoxColliderRoundTrip() {
        Scene scene = new Scene();
        var object = scene.createGameObject("PhysicsObject");
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.KINEMATIC;
        rb.mass = 2.5f;
        object.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        box.sizeX = 3f;
        box.sizeY = 4f;
        box.isTrigger = true;
        object.addComponent(BoxCollider2DComponent.class, box);

        ObjectNode node = SceneObjectSerializer.writeObject(scene, object.entity(), 1, -1);
        Scene loaded = new Scene();
        var loadedObject = SceneObjectSerializer.readObject(loaded, node, 1);
        Rigidbody2DComponent loadedRb = loadedObject.getComponent(Rigidbody2DComponent.class);
        BoxCollider2DComponent loadedBox = loadedObject.getComponent(BoxCollider2DComponent.class);
        assertNotNull(loadedRb);
        assertNotNull(loadedBox);
        assertEquals(PhysicsBodyType.KINEMATIC, loadedRb.bodyType);
        assertEquals(2.5f, loadedRb.mass, 0.001f);
        assertEquals(3f, loadedBox.sizeX, 0.001f);
        assertEquals(4f, loadedBox.sizeY, 0.001f);
        assertEquals(true, loadedBox.isTrigger);
    }
}
