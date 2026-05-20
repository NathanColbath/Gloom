package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayInputBridgeTest {
    @Test
    void resolvesGlfwKeyCodes() {
        assertEquals(GLFW.GLFW_KEY_SPACE, PlayInputBridge.resolveKeyCode(GLFW.GLFW_KEY_SPACE));
        assertEquals(GLFW.GLFW_KEY_A, PlayInputBridge.resolveKeyCode(65));
    }

    @Test
    void resolvesLegacyStringNames() {
        assertEquals(GLFW.GLFW_KEY_SPACE, PlayInputBridge.resolveKeyCode("SPACE"));
        assertEquals(GLFW.GLFW_KEY_W, PlayInputBridge.resolveKeyCode("W"));
    }
}
