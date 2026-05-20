package org.llw.render.window;

/**
 * Keyboard key identifiers mapped from GLFW key codes.
 * <p>
 * Letter keys ({@code A}–{@code Z}), arrow keys, functionally named keys
 * ({@link #ESCAPE}, {@link #ENTER}, and similar), and modifier keys are named after
 * their usual labels. Unmapped or unrecognized GLFW keys resolve to {@link #UNKNOWN}.
 */
public enum Key {
    /** Key code that could not be mapped to a known {@link Key} constant. */
    UNKNOWN,
    SPACE,
    /** Apostrophe ({@code '}) key. */
    APOSTROPHE,
    /** Comma ({@code ,}) key. */
    COMMA,
    /** Minus/hyphen ({@code -}) key. */
    MINUS,
    /** Period/dot ({@code .}) key. */
    PERIOD,
    /** Forward slash ({@code /}) key. */
    SLASH,
    /** Digit {@code 0} on the main keyboard row. */
    NUM_0,
    /** Digit {@code 1} on the main keyboard row. */
    NUM_1,
    /** Digit {@code 2} on the main keyboard row. */
    NUM_2,
    /** Digit {@code 3} on the main keyboard row. */
    NUM_3,
    /** Digit {@code 4} on the main keyboard row. */
    NUM_4,
    /** Digit {@code 5} on the main keyboard row. */
    NUM_5,
    /** Digit {@code 6} on the main keyboard row. */
    NUM_6,
    /** Digit {@code 7} on the main keyboard row. */
    NUM_7,
    /** Digit {@code 8} on the main keyboard row. */
    NUM_8,
    /** Digit {@code 9} on the main keyboard row. */
    NUM_9,
    /** Semicolon ({@code ;}) key. */
    SEMICOLON,
    /** Equals ({@code =}) key. */
    EQUAL,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J,
    K,
    L,
    M,
    N,
    O,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X,
    Y,
    Z,
    /** Left square bracket ({@code [}) key. */
    LEFT_BRACKET,
    /** Backslash ({@code \}) key. */
    BACKSLASH,
    /** Right square bracket ({@code ]}) key. */
    RIGHT_BRACKET,
    /** Grave accent/backtick ({@code `}) key. */
    GRAVE_ACCENT,
    ESCAPE,
    ENTER,
    TAB,
    BACKSPACE,
    INSERT,
    DELETE,
    RIGHT,
    LEFT,
    DOWN,
    UP,
    PAGE_UP,
    PAGE_DOWN,
    HOME,
    END,
    LEFT_SHIFT,
    LEFT_CONTROL,
    LEFT_ALT,
    RIGHT_SHIFT,
    RIGHT_CONTROL,
    RIGHT_ALT,
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
    KP_0,
    KP_1,
    KP_2,
    KP_3,
    KP_4,
    KP_5,
    KP_6,
    KP_7,
    KP_8,
    KP_9,
    KP_DECIMAL,
    KP_DIVIDE,
    KP_MULTIPLY,
    KP_SUBTRACT,
    KP_ADD,
    KP_ENTER,
    KP_EQUAL
}
