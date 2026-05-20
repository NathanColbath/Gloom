package org.llw.studio.physics;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhysicsPrefabRegistrationTest {
    @Test
    void registerEntityAddsBodyAfterInitialBuild() {
        Scene scene = new Scene();
        var ground = scene.createGameObject("Ground");
        ground.transform().y = 50f;
        ground.addComponent(BoxCollider2DComponent.class, new BoxCollider2DComponent());

        PhysicsWorld physics = new PhysicsWorld();
        physics.buildFromScene(scene.world());
        assertNotNull(physics.bodyFor(ground.entity()));

        var bullet = scene.createGameObject("Bullet");
        bullet.transform().y = 0f;
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.KINEMATIC;
        bullet.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        box.sizeX = 32f;
        box.sizeY = 32f;
        bullet.addComponent(BoxCollider2DComponent.class, box);

        physics.registerEntity(scene.world(), bullet.entity());
        assertNotNull(physics.bodyFor(bullet.entity()));

        physics.unregisterEntity(bullet.entity());
        assertNull(physics.bodyFor(bullet.entity()));
        physics.destroy();
    }
}
