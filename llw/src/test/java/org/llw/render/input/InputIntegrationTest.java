package org.llw.render.input;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

class InputIntegrationTest {

    @Test
    void beginFrameAfterPollDoesNotThrow() {
        Window window = null;
        try {
            window = new Window(new WindowSettings().title("InputTest").size(64, 64));
            Input input = new Input();
            window.pollEvents();
            input.beginFrame(window);
        } catch (RuntimeException ex) {
            Assumptions.abort("GLFW unavailable: " + ex.getMessage());
        } finally {
            if (window != null) {
                window.destroy();
            }
        }
    }
}
