package org.llw.render.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamepadMathTest {

    @Test
    void zeroesInsideDeadzone() {
        assertEquals(0f, GamepadMath.applyDeadzone(0.05f, 0.15f));
        assertEquals(0f, GamepadMath.applyDeadzone(-0.1f, 0.15f));
    }

    @Test
    void rescalesOutsideDeadzone() {
        float value = GamepadMath.applyDeadzone(1f, 0.15f);
        assertEquals(1f, value, 0.001f);
    }

    @Test
    void passesThroughWhenDeadzoneZero() {
        assertEquals(0.5f, GamepadMath.applyDeadzone(0.5f, 0f));
    }
}
