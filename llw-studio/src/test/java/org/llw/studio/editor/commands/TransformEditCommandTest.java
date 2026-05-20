package org.llw.studio.editor.commands;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformEditCommandTest {
    @Test
    void undoRedoRestoresAllFields() {
        Scene scene = new Scene();
        var object = scene.createGameObject("Player");
        Transform2DComponent transform = object.transform();
        transform.x = 1f;
        transform.y = 2f;
        transform.rotation = 0.5f;
        transform.scaleX = 2f;
        transform.scaleY = 3f;

        TransformSnapshot before = TransformSnapshot.from(transform);
        transform.x = 10f;
        transform.y = 20f;
        transform.rotation = 1.5f;
        transform.scaleX = 4f;
        transform.scaleY = 5f;
        TransformSnapshot after = TransformSnapshot.from(transform);

        UndoStack stack = new UndoStack(scene);
        stack.execute(new TransformEditCommand(scene, object.entity(), before, after));
        assertEquals(10f, transform.x);
        stack.undo();
        assertEquals(1f, transform.x);
        assertEquals(2f, transform.y);
        assertEquals(0.5f, transform.rotation);
        assertEquals(2f, transform.scaleX);
        assertEquals(3f, transform.scaleY);
        stack.redo();
        assertEquals(10f, transform.x);
        assertEquals(5f, transform.scaleY);
    }
}
