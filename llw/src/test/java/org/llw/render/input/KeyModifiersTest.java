package org.llw.render.input;

import org.junit.jupiter.api.Test;
import org.llw.render.window.KeyModifiers;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyModifiersTest {

    @Test
    void parsesGlfwFlags() {
        KeyModifiers mods = KeyModifiers.fromGlfw(
                GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER
        );
        assertTrue(mods.shift());
        assertTrue(mods.control());
        assertTrue(mods.alt());
        assertTrue(mods.superKey());
    }

    @Test
    void noneHasNoFlags() {
        KeyModifiers mods = KeyModifiers.fromGlfw(0);
        assertFalse(mods.shift());
        assertFalse(mods.control());
        assertFalse(mods.alt());
        assertFalse(mods.superKey());
    }
}
