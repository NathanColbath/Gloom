package org.llw.render.window;

/**
 * Identifies a physical mouse button reported by GLFW.
 * <p>
 * Used in {@link WindowEvent.MouseButtonPressed} and {@link WindowEvent.MouseButtonReleased}.
 */
public enum MouseButton {
    /** Primary (left) mouse button. */
    LEFT,
    /** Secondary (right) mouse button. */
    RIGHT,
    /** Middle mouse button (scroll wheel click). */
    MIDDLE,
    /** Fourth mouse button (often "back"). */
    BUTTON_4,
    /** Fifth mouse button (often "forward"). */
    BUTTON_5,
    /** Sixth mouse button. */
    BUTTON_6,
    /** Seventh mouse button. */
    BUTTON_7,
    /** Eighth mouse button. */
    BUTTON_8
}
