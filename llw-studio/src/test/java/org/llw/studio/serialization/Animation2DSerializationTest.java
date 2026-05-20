package org.llw.studio.serialization;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.*;

class Animation2DSerializationTest {
    @Test
    void roundTrip() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Anim");
        Animation2DComponent anim = new Animation2DComponent();
        anim.animationGuid = "anim-1";
        anim.defaultState = "Idle";
        anim.currentState = "Walk";
        anim.clipGuid = "clip-1";
        anim.speed = 2f;
        anim.loop = false;
        object.addComponent(Animation2DComponent.class, anim);

        var node = SceneObjectSerializer.writeObject(scene, object.entity(), 1, -1);
        Scene loaded = new Scene();
        var restored = SceneObjectSerializer.readObject(loaded, node, 1);
        Animation2DComponent copy = restored.getComponent(Animation2DComponent.class);
        assertNotNull(copy);
        assertEquals("anim-1", copy.animationGuid);
        assertEquals("Idle", copy.defaultState);
        assertEquals("Walk", copy.currentState);
        assertEquals("clip-1", copy.clipGuid);
        assertEquals(2f, copy.speed, 0.001f);
        assertFalse(copy.loop);
    }
}
