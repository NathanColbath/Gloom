package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpriteRendererSerializationTest {
    @Test
    void writeAndReadSpriteGuid() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Sprite");
        SpriteRendererComponent sprite = new SpriteRendererComponent();
        sprite.spriteGuid = "slice-guid";
        object.addComponent(SpriteRendererComponent.class, sprite);

        ObjectNode node = SceneObjectSerializer.writeObject(scene, object.entity(), 1, -1);
        Scene loaded = new Scene();
        var restored = SceneObjectSerializer.readObject(loaded, node, 1);
        SpriteRendererComponent loadedSprite = restored.getComponent(SpriteRendererComponent.class);
        assertEquals("slice-guid", loadedSprite.spriteGuid);
    }

    @Test
    void readLegacyTextureGuidField() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Sprite");
        object.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());
        ObjectNode node = SceneObjectSerializer.writeObject(scene, object.entity(), 0, -1);
        ObjectNode sr = (ObjectNode) node.get("spriteRenderer");
        sr.put("textureGuid", "tex-guid");
        sr.remove("spriteGuid");

        Scene loaded = new Scene();
        var restored = SceneObjectSerializer.readObject(loaded, node, 0);
        assertEquals("tex-guid", restored.getComponent(SpriteRendererComponent.class).textureGuid);
    }
}
