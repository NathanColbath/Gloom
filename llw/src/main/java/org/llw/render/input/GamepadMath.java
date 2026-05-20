package org.llw.render.input;

/**
 * Axis deadzone helper shared by gamepad polling.
 */
public final class GamepadMath {
    private GamepadMath() {
    }

    /**
     * Applies a symmetric deadzone and rescales the result to [-1, 1].
     *
     * @param value    raw axis in [-1, 1]
     * @param deadzone deadzone threshold in [0, 1)
     * @return adjusted axis value
     */
    public static float applyDeadzone(float value, float deadzone) {
        if (deadzone <= 0f) {
            return clamp(value);
        }
        if (Math.abs(value) < deadzone) {
            return 0f;
        }
        float sign = Math.signum(value);
        float scaled = (Math.abs(value) - deadzone) / (1f - deadzone);
        return clamp(sign * scaled);
    }

    private static float clamp(float value) {
        return Math.max(-1f, Math.min(1f, value));
    }
}
