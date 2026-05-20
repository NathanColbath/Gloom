package org.llw.render.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

/**
 * Polled state for one GLFW joystick/gamepad slot.
 */
public final class Gamepad {
    private final int joystickId;
    private final float deadzone;
    private boolean connected;
    private String name = "";
    private final boolean[] buttons = new boolean[GamepadButton.values().length];
    private final float[] axes = new float[GamepadAxis.values().length];

    Gamepad(int joystickId, float deadzone) {
        this.joystickId = joystickId;
        this.deadzone = deadzone;
    }

    void poll() {
        connected = GLFW.glfwJoystickPresent(joystickId);
        if (!connected) {
            name = "";
            clearState();
            return;
        }
        name = GLFW.glfwGetJoystickName(joystickId);
        if (GLFW.glfwJoystickIsGamepad(joystickId)) {
            pollGamepadMapping();
        } else {
            pollRawJoystick();
        }
    }

    /**
     * Returns whether a controller is connected in this slot.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns the GLFW joystick name, or an empty string when disconnected.
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether a mapped gamepad button is held.
     */
    public boolean isButtonDown(GamepadButton button) {
        return buttons[button.ordinal()];
    }

    /**
     * Returns a deadzone-adjusted axis value in [-1, 1].
     */
    public float getAxis(GamepadAxis axis) {
        return axes[axis.ordinal()];
    }

    private void pollGamepadMapping() {
        GLFWGamepadState state = GLFWGamepadState.create();
        if (!GLFW.glfwGetGamepadState(joystickId, state)) {
            clearState();
            return;
        }
        buttons[GamepadButton.A.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.B.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.X.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.Y.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.LEFT_BUMPER.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.RIGHT_BUMPER.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.BACK.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.START.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.GUIDE.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.LEFT_THUMB.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.RIGHT_THUMB.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.DPAD_UP.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.DPAD_RIGHT.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.DPAD_DOWN.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS;
        buttons[GamepadButton.DPAD_LEFT.ordinal()] = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS;

        axes[GamepadAxis.LEFT_X.ordinal()] = GamepadMath.applyDeadzone(state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X), deadzone);
        axes[GamepadAxis.LEFT_Y.ordinal()] = GamepadMath.applyDeadzone(state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y), deadzone);
        axes[GamepadAxis.RIGHT_X.ordinal()] = GamepadMath.applyDeadzone(state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X), deadzone);
        axes[GamepadAxis.RIGHT_Y.ordinal()] = GamepadMath.applyDeadzone(state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y), deadzone);
        axes[GamepadAxis.LEFT_TRIGGER.ordinal()] = clamp01(state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER));
        axes[GamepadAxis.RIGHT_TRIGGER.ordinal()] = clamp01(state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER));
        state.free();
    }

    private void pollRawJoystick() {
        clearState();
        var buttonBuffer = GLFW.glfwGetJoystickButtons(joystickId);
        if (buttonBuffer != null && buttonBuffer.limit() > 0) {
            buttons[GamepadButton.A.ordinal()] = buttonBuffer.get(0) == GLFW.GLFW_PRESS;
        }
        var axisBuffer = GLFW.glfwGetJoystickAxes(joystickId);
        if (axisBuffer != null && axisBuffer.limit() > 0) {
            axes[GamepadAxis.LEFT_X.ordinal()] = GamepadMath.applyDeadzone(axisBuffer.get(0), deadzone);
        }
        if (axisBuffer != null && axisBuffer.limit() > 1) {
            axes[GamepadAxis.LEFT_Y.ordinal()] = GamepadMath.applyDeadzone(axisBuffer.get(1), deadzone);
        }
    }

    private void clearState() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = false;
        }
        for (int i = 0; i < axes.length; i++) {
            axes[i] = 0f;
        }
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, (value + 1f) * 0.5f));
    }
}
