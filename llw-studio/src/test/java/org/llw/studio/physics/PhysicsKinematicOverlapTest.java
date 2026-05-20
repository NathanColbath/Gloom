package org.llw.studio.physics;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.PhysicsSystem;
import org.llw.studio.systems.TransformSystem;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PhysicsKinematicOverlapTest {
    @Test
    void overlappingKinematicPairProducesCollisionEnter() {
        Scene scene = new Scene();
        var a = scene.createGameObject("A");
        a.transform().x = 0f;
        a.transform().y = 0f;
        Rigidbody2DComponent rbA = new Rigidbody2DComponent();
        rbA.bodyType = PhysicsBodyType.KINEMATIC;
        a.addComponent(Rigidbody2DComponent.class, rbA);
        BoxCollider2DComponent boxA = new BoxCollider2DComponent();
        boxA.sizeX = 50f;
        boxA.sizeY = 50f;
        a.addComponent(BoxCollider2DComponent.class, boxA);

        var b = scene.createGameObject("B");
        b.transform().x = 10f;
        b.transform().y = 0f;
        Rigidbody2DComponent rbB = new Rigidbody2DComponent();
        rbB.bodyType = PhysicsBodyType.KINEMATIC;
        b.addComponent(Rigidbody2DComponent.class, rbB);
        BoxCollider2DComponent boxB = new BoxCollider2DComponent();
        boxB.sizeX = 50f;
        boxB.sizeY = 50f;
        b.addComponent(BoxCollider2DComponent.class, boxB);

        PhysicsWorld physics = new PhysicsWorld();
        physics.setGravity(0f, 0f);
        physics.buildFromScene(scene.world());

        PhysicsContactBridge bridge = new PhysicsContactBridge();
        List<PhysicsContactEvent> received = new ArrayList<>();
        bridge.setDispatcher(received::add);
        PhysicsSystem system = new PhysicsSystem(physics, bridge);

        new TransformSystem().onUpdate(scene.world(), 0f);
        system.onUpdate(scene.world(), 1f / 50f);

        assertFalse(
                received.stream().noneMatch(e -> e.type() == PhysicsMessageType.COLLISION_ENTER),
                "Expected collision enter for overlapping kinematic-kinematic pair"
        );
        physics.destroy();
    }
}
