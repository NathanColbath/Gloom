package org.llw.studio.physics;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsLayerTest {
    @Test
    void maskedBodiesDoNotCollide() {
        Scene scene = new Scene();
        var falling = scene.createGameObject("Falling");
        falling.transform().y = 0f;
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.DYNAMIC;
        falling.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent fallBox = new BoxCollider2DComponent();
        fallBox.layer = 2;
        fallBox.layerMask = 0;
        falling.addComponent(BoxCollider2DComponent.class, fallBox);

        var ground = scene.createGameObject("Ground");
        ground.transform().y = 100f;
        BoxCollider2DComponent groundBox = new BoxCollider2DComponent();
        groundBox.sizeX = 200f;
        groundBox.sizeY = 1f;
        groundBox.layer = 1;
        groundBox.layerMask = 0xFFFF_FFFF;
        ground.addComponent(BoxCollider2DComponent.class, groundBox);

        PhysicsWorld physics = new PhysicsWorld();
        physics.setGravity(0f, PhysicsWorld.DEFAULT_GRAVITY_Y);
        physics.buildFromScene(scene.world());

        float startY = falling.transform().y;
        for (int i = 0; i < 120; i++) {
            physics.step(1f / 50f);
            physics.syncBodiesToTransforms(scene.world());
        }

        assertTrue(
                falling.transform().y > startY + 20f,
                "With layer mask 0 the dynamic body should fall through the ground"
        );
        physics.destroy();
    }
}
