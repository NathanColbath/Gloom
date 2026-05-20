package org.llw.render.window;

import org.lwjgl.glfw.GLFW;

/**
 * Keyboard modifier keys active during an input event (Shift, Control, Alt, Super).
 */
public final class KeyModifiers {
    public static final KeyModifiers NONE = new KeyModifiers(0);

    private final int flags;

    private KeyModifiers(int flags) {
        this.flags = flags;
    }

    /**
     * Parses GLFW modifier bitmask from a key or mouse callback.
     */
    public static KeyModifiers fromGlfw(int mods) {
        return mods == 0 ? NONE : new KeyModifiers(mods);
    }

    public boolean shift() {
        return (flags & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    public boolean control() {
        return (flags & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    public boolean alt() {
        return (flags & GLFW.GLFW_MOD_ALT) != 0;
    }

    public boolean superKey() {
        return (flags & GLFW.GLFW_MOD_SUPER) != 0;
    }

    public boolean capsLock() {
        return (flags & GLFW.GLFW_MOD_CAPS_LOCK) != 0;
    }

    public boolean numLock() {
        return (flags & GLFW.GLFW_MOD_NUM_LOCK) != 0;
    }

    int glfwFlags() {
        return flags;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KeyModifiers other && flags == other.flags;
    }

    @Override
    public int hashCode() {
        return flags;
    }
}
