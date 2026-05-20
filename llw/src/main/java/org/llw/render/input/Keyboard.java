package org.llw.render.input;

import java.util.EnumSet;
import java.util.Set;

import org.llw.render.window.Key;
import org.llw.render.window.KeyModifiers;
import org.llw.render.window.Window;

/**
 * Polled keyboard state with per-frame edge detection.
 */
public final class Keyboard {
    private final EnumSet<Key> previous = EnumSet.noneOf(Key.class);
    private final EnumSet<Key> pressed = EnumSet.noneOf(Key.class);
    private final EnumSet<Key> released = EnumSet.noneOf(Key.class);
    private KeyModifiers modifiers = KeyModifiers.NONE;

    void beginFrame(Window window) {
        Set<Key> current = window.pressedKeys();
        InputEdges.update(current, previous, pressed, released);
        modifiers = window.activeModifiers();
    }

    /**
     * Returns whether {@code key} is currently held down.
     */
    public boolean isDown(Key key) {
        return previous.contains(key);
    }

    /**
     * Returns whether {@code key} transitioned to down this frame.
     */
    public boolean wasPressed(Key key) {
        return pressed.contains(key);
    }

    /**
     * Returns whether {@code key} transitioned to up this frame.
     */
    public boolean wasReleased(Key key) {
        return released.contains(key);
    }

    /**
     * Returns modifier keys from the most recent GLFW input callback.
     */
    public KeyModifiers modifiers() {
        return modifiers;
    }
}
