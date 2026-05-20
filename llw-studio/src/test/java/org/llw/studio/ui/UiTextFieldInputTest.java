package org.llw.studio.ui;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.UITextFieldComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiTextFieldInputTest {
    @Test
    void appendAndBackspaceRespectMaxLength() {
        UITextFieldComponent field = new UITextFieldComponent();
        field.maxLength = 5;
        field.value = "ab";
        field.value += "cd";
        assertEquals("abcd", field.value);
        if (field.value.length() < field.maxLength) {
            field.value += "e";
        }
        assertEquals("abcde", field.value);
        if (field.value.length() < field.maxLength) {
            field.value += "f";
        }
        assertEquals("abcde", field.value);
        field.value = field.value.substring(0, field.value.length() - 1);
        assertEquals("abcd", field.value);
    }
}
