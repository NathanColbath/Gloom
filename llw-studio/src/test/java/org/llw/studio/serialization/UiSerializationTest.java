package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiSerializationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void uiComponentsRoundTripThroughJson() {
        ObjectNode node = MAPPER.createObjectNode();

        UICanvasComponent canvas = new UICanvasComponent();
        canvas.sortingOrder = 5;
        canvas.enabled = true;
        SceneObjectSerializer.writeUiCanvas(node, canvas);

        UILabelComponent label = new UILabelComponent();
        label.text = "Hello";
        label.width = 90f;
        SceneObjectSerializer.writeUiLabel(node, label);

        UIButtonComponent button = new UIButtonComponent();
        button.label = "Go";
        button.interactable = false;
        SceneObjectSerializer.writeUiButton(node, button);

        UIToggleComponent toggle = new UIToggleComponent();
        toggle.isOn = true;
        SceneObjectSerializer.writeUiToggle(node, toggle);

        UITextFieldComponent field = new UITextFieldComponent();
        field.value = "abc";
        field.maxLength = 32;
        SceneObjectSerializer.writeUiTextField(node, field);

        UICanvasComponent readCanvas = SceneObjectSerializer.readUiCanvas(node.path("uiCanvas"));
        assertEquals(5, readCanvas.sortingOrder);
        assertTrue(readCanvas.enabled);

        UILabelComponent readLabel = SceneObjectSerializer.readUiLabel(node.path("uiLabel"));
        assertEquals("Hello", readLabel.text);
        assertEquals(90f, readLabel.width, 0.001f);

        UIButtonComponent readButton = SceneObjectSerializer.readUiButton(node.path("uiButton"));
        assertEquals("Go", readButton.label);
        assertEquals(false, readButton.interactable);

        UIToggleComponent readToggle = SceneObjectSerializer.readUiToggle(node.path("uiToggle"));
        assertTrue(readToggle.isOn);

        UITextFieldComponent readField = SceneObjectSerializer.readUiTextField(node.path("uiTextField"));
        assertEquals("abc", readField.value);
        assertEquals(32, readField.maxLength);
    }
}
