package org.llw.studio.serialization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SceneSerializerTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTrip() throws Exception {
        Scene scene = new Scene();
        scene.setName("Test");
        var player = scene.createGameObject("Player");
        player.transform().x = 10f;
        player.transform().y = 20f;
        SpriteRendererComponent sprite = new SpriteRendererComponent();
        sprite.textureGuid = "abc-123";
        player.addComponent(SpriteRendererComponent.class, sprite);

        Path file = tempDir.resolve("scene.json");
        SceneSerializer.save(scene, file);
        Scene loaded = SceneSerializer.load(file);

        assertEquals("Test", loaded.name());
        assertFalse(loaded.rootObjects().isEmpty());
        var object = loaded.rootObjects().get(0);
        assertEquals("Player", object.name());
        assertEquals(10f, object.transform().x);
        assertEquals(20f, object.transform().y);
        assertEquals("abc-123", object.getComponent(SpriteRendererComponent.class).textureGuid);
    }
}
