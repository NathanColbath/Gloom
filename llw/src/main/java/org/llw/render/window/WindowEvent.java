package org.llw.render.window;

import org.llw.math.vector.Vector2f;

/**
 * Sealed hierarchy of window lifecycle, input, and resize notifications produced by {@link Window}.
 * <p>
 * Events are queued internally when GLFW callbacks fire during {@link Window#pollEvents()} and
 * are retrieved one at a time via {@link Window#pollEvent()}.
 */
public sealed interface WindowEvent permits
        WindowEvent.Closed,
        WindowEvent.Resized,
        WindowEvent.FocusGained,
        WindowEvent.FocusLost,
        WindowEvent.KeyPressed,
        WindowEvent.KeyReleased,
        WindowEvent.TextEntered,
        WindowEvent.MouseMoved,
        WindowEvent.MouseButtonPressed,
        WindowEvent.MouseButtonReleased,
        WindowEvent.MouseScrolled {

    /**
     * The user or system requested that the window close.
     */
    record Closed() implements WindowEvent {}

    /**
     * The window framebuffer size changed.
     *
     * @param width  new framebuffer width in pixels
     * @param height new framebuffer height in pixels
     */
    record Resized(int width, int height) implements WindowEvent {}

    /**
     * The window gained keyboard focus.
     */
    record FocusGained() implements WindowEvent {}

    /**
     * The window lost keyboard focus.
     */
    record FocusLost() implements WindowEvent {}

    /**
     * A keyboard key was pressed.
     *
     * @param key      the key that was pressed
     * @param mods     modifier keys held during the press
     * @param repeated {@code true} when GLFW reports key repeat
     */
    record KeyPressed(Key key, KeyModifiers mods, boolean repeated) implements WindowEvent {
        /** @param key the key that was pressed with no modifiers and not repeated */
        public KeyPressed(Key key) {
            this(key, KeyModifiers.NONE, false);
        }
    }

    /**
     * A keyboard key was released.
     *
     * @param key  the key that was released
     * @param mods modifier keys held during the release
     */
    record KeyReleased(Key key, KeyModifiers mods) implements WindowEvent {
        /** @param key the key that was released with no modifiers */
        public KeyReleased(Key key) {
            this(key, KeyModifiers.NONE);
        }
    }

    /**
     * Unicode text was entered (character callback).
     *
     * @param codepoint UTF-32 codepoint
     */
    record TextEntered(int codepoint) implements WindowEvent {}

    /**
     * The cursor moved within the window client area.
     *
     * @param position cursor position in window coordinates, with origin at the top-left corner
     */
    record MouseMoved(Vector2f position) implements WindowEvent {}

    /**
     * A mouse button was pressed.
     *
     * @param button   the button that was pressed
     * @param position cursor position in window coordinates at the time of the press
     * @param mods     modifier keys held during the press
     */
    record MouseButtonPressed(MouseButton button, Vector2f position, KeyModifiers mods) implements WindowEvent {
        /** @param button the button pressed at {@code position} with no modifiers */
        public MouseButtonPressed(MouseButton button, Vector2f position) {
            this(button, position, KeyModifiers.NONE);
        }
    }

    /**
     * A mouse button was released.
     *
     * @param button   the button that was released
     * @param position cursor position in window coordinates at the time of the release
     * @param mods     modifier keys held during the release
     */
    record MouseButtonReleased(MouseButton button, Vector2f position, KeyModifiers mods) implements WindowEvent {
        /** @param button the button released at {@code position} with no modifiers */
        public MouseButtonReleased(MouseButton button, Vector2f position) {
            this(button, position, KeyModifiers.NONE);
        }
    }

    /**
     * The mouse scroll wheel was rotated.
     *
     * @param xOffset horizontal scroll offset (positive is right)
     * @param yOffset vertical scroll offset (positive is up)
     */
    record MouseScrolled(float xOffset, float yOffset) implements WindowEvent {}
}
