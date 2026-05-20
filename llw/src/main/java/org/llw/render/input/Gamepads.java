package org.llw.render.input;

import org.lwjgl.glfw.GLFW;

/**
 * Polls up to four GLFW joystick/gamepad slots each frame.
 */
public final class Gamepads {
    public static final int MAX_SLOTS = 4;
    public static final float DEFAULT_DEADZONE = 0.15f;

    private final Gamepad[] slots = new Gamepad[MAX_SLOTS];

    public Gamepads() {
        this(DEFAULT_DEADZONE);
    }

    public Gamepads(float deadzone) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new Gamepad(GLFW.GLFW_JOYSTICK_1 + i, deadzone);
        }
    }

    void beginFrame() {
        for (Gamepad slot : slots) {
            slot.poll();
        }
    }

    /**
     * Returns the gamepad at {@code index} (0 = first joystick).
     */
    public Gamepad get(int index) {
        if (index < 0 || index >= MAX_SLOTS) {
            throw new IllegalArgumentException("Gamepad index out of range: " + index);
        }
        return slots[index];
    }

    /**
     * Returns how many slots are currently reporting a connected controller.
     */
    public int connectedCount() {
        int count = 0;
        for (Gamepad slot : slots) {
            if (slot.isConnected()) {
                count++;
            }
        }
        return count;
    }
}
