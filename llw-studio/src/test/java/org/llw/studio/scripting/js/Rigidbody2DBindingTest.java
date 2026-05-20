package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.physics.PhysicsBodyType;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.physics.PlayPhysicsBridge;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.Rigidbody2DBinding;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Rigidbody2DBindingTest {
    @Test
    void addForceChangesVelocity() {
        Scene scene = new Scene();
        var body = scene.createGameObject("Body");
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = PhysicsBodyType.DYNAMIC;
        body.addComponent(Rigidbody2DComponent.class, rb);
        BoxCollider2DComponent box = new BoxCollider2DComponent();
        body.addComponent(BoxCollider2DComponent.class, box);

        PhysicsWorld physics = new PhysicsWorld();
        physics.setGravity(0f, 0f);
        physics.buildFromScene(scene.world());
        PlayPhysicsBridge.setActive(physics);

        try (var runtime = new GraalScriptRuntime(null, scene, null, java.nio.file.Path.of("."))) {
            ScriptHostApi hostApi = runtime.hostApi();
            Rigidbody2DBinding binding = new Rigidbody2DBinding(hostApi.scriptContext(), body.entity());
            binding.addForce(500d, 0d);
            physics.step(1f / 50f);
            physics.syncBodiesToTransforms(scene.world());

            double vx = binding.getVelocityX();
            assertTrue(Math.abs(vx) > 0.01d, "Expected horizontal velocity after addForce");
        } finally {
            PlayPhysicsBridge.setActive(null);
            physics.destroy();
        }
    }
}
