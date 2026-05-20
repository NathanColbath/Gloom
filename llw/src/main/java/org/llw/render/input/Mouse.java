package org.llw.render.input;

import java.util.EnumSet;
import java.util.Set;

import org.llw.math.vector.Vector2f;
import org.llw.render.window.MouseButton;
import org.llw.render.window.Window;

/**
 * Polled mouse state with position, delta, scroll, and button edges.
 */
public final class Mouse {
    private final EnumSet<MouseButton> previous = EnumSet.noneOf(MouseButton.class);
    private final EnumSet<MouseButton> pressed = EnumSet.noneOf(MouseButton.class);
    private final EnumSet<MouseButton> released = EnumSet.noneOf(MouseButton.class);
    private final Vector2f position = new Vector2f();
    private final Vector2f delta = new Vector2f();
    private final Vector2f scroll = new Vector2f();

    void beginFrame(Window window) {
        Set<MouseButton> current = window.pressedButtons();
        InputEdges.update(current, previous, pressed, released);

        Vector2f next = window.mousePosition();
        delta.set(next.x - position.x, next.y - position.y);
        position.set(next);

        Vector2f scrollOffset = window.takeScrollOffset();
        scroll.set(scrollOffset);
    }

    /**
     * Returns the cursor position in window coordinates (top-left origin, Y-down).
     */
    public Vector2f position() {
        return position.copy();
    }

    /**
     * Returns cursor movement since the previous {@link Input#beginFrame(Window)} call.
     */
    public Vector2f delta() {
        return delta.copy();
    }

    /**
     * Returns accumulated scroll wheel offset for this frame.
     */
    public Vector2f scroll() {
        return scroll.copy();
    }

    /**
     * Returns whether {@code button} is currently held down.
     */
    public boolean isDown(MouseButton button) {
        return previous.contains(button);
    }

    /**
     * Returns whether {@code button} was pressed this frame.
     */
    public boolean wasPressed(MouseButton button) {
        return pressed.contains(button);
    }

    /**
     * Returns whether {@code button} was released this frame.
     */
    public boolean wasReleased(MouseButton button) {
        return released.contains(button);
    }
}
