package org.llw.studio.physics;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsWorldTest {
    @Test
    void dynamicBodyFallsUnderGravity() {
        Scene scene = new Scene();
        var player = scene.createGameObject("Player");
        player.transform().y = 0f;
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.DYNAMIC;
        player.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        box.sizeX = 1f;
        box.sizeY = 1f;
        player.addComponent(BoxCollider2DComponent.class, box);

        var ground = scene.createGameObject("Ground");
        ground.transform().y = 100f;
        BoxCollider2DComponent groundBox = new BoxCollider2DComponent();
        groundBox.sizeX = 200f;
        groundBox.sizeY = 1f;
        ground.addComponent(BoxCollider2DComponent.class, groundBox);

        PhysicsWorld physics = new PhysicsWorld();
        physics.setGravity(0f, PhysicsWorld.DEFAULT_GRAVITY_Y);
        physics.buildFromScene(scene.world());

        for (int i = 0; i < 120; i++) {
            physics.step(1f / 50f);
            physics.syncBodiesToTransforms(scene.world());
        }

        Transform2DComponent transform = player.transform();
        assertTrue(transform.y > 5f, "Expected player to fall downward (y increases in Y-down space)");
        physics.destroy();
    }

    @Test
    void fallingBodyGeneratesCollisionEnterEvents() {
        Scene scene = new Scene();
        var player = scene.createGameObject("Player");
        player.transform().y = 0f;
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.DYNAMIC;
        player.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        box.sizeX = 1f;
        box.sizeY = 1f;
        player.addComponent(BoxCollider2DComponent.class, box);

        var ground = scene.createGameObject("Ground");
        ground.transform().y = 12f;
        BoxCollider2DComponent groundBox = new BoxCollider2DComponent();
        groundBox.sizeX = 200f;
        groundBox.sizeY = 1f;
        ground.addComponent(BoxCollider2DComponent.class, groundBox);

        PhysicsWorld physics = new PhysicsWorld();
        physics.setGravity(0f, PhysicsWorld.DEFAULT_GRAVITY_Y);
        physics.buildFromScene(scene.world());

        for (int i = 0; i < 200; i++) {
            physics.step(1f / 50f);
            physics.syncBodiesToTransforms(scene.world());
        }

        var events = physics.drainContactEvents();
        assertFalse(
                events.stream().noneMatch(e -> e.type() == PhysicsMessageType.COLLISION_ENTER),
                "Expected collision enter after landing (contacts=" + physics.box2dWorld().getContactCount()
                        + ", events=" + events.size() + ")"
        );
        physics.destroy();
    }
}
