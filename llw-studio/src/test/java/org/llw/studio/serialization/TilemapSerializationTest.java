package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TilemapSerializationTest {
    @Test
    void writeAndReadTilemap() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Tilemap");
        TilemapComponent tilemap = new TilemapComponent();
        tilemap.tilesetTextureGuid = "tex-guid";
        tilemap.cellWidth = 16f;
        tilemap.cellHeight = 16f;
        var layer = tilemap.layers.get(0);
        layer.name = "Ground";
        layer.sortingOrder = 2;
        TilemapCell cell = new TilemapCell();
        cell.spriteGuid = "sprite-1";
        layer.setCell(3, 4, cell);
        object.addComponent(TilemapComponent.class, tilemap);

        ObjectNode node = SceneObjectSerializer.writeObject(scene, object.entity(), 1, -1);
        Scene scene2 = new Scene();
        var restored = SceneObjectSerializer.readObject(scene2, node, 1);

        TilemapComponent read = restored.getComponent(TilemapComponent.class);
        assertNotNull(read);
        assertEquals("tex-guid", read.tilesetTextureGuid);
        assertEquals(16f, read.cellWidth);
        assertEquals(16f, read.cellHeight);
        assertEquals("Ground", read.layers.get(0).name);
        assertEquals(2, read.layers.get(0).sortingOrder);
        TilemapCell readCell = read.layers.get(0).getCell(3, 4);
        assertNotNull(readCell);
        assertEquals("sprite-1", readCell.spriteGuid);
    }
}
