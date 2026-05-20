package org.llw.studio.physics;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PhysicsRaycastTest {
    @Test
    void raycastHitsPlacedCollider() {
        Scene scene = new Scene();
        var wall = scene.createGameObject("Wall");
        wall.transform().x = 100f;
        wall.transform().y = 50f;
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        box.sizeX = 10f;
        box.sizeY = 50f;
        wall.addComponent(BoxCollider2DComponent.class, box);

        PhysicsWorld physics = new PhysicsWorld();
        physics.buildFromScene(scene.world());

        PhysicsRaycastHit hit = physics.raycast(0f, 50f, 1f, 0f, 200f, 0xFFFF_FFFF);
        assertNotNull(hit);
        assertEquals(wall.entity(), hit.entity());

        physics.destroy();
    }
}
