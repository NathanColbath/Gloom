package org.llw.render.input;

import org.llw.render.window.Window;

/**
 * High-level polled input facade for keyboard, mouse, gamepads, and text entry.
 * <p>
 * Call {@link #beginFrame(Window)} once per frame after {@link Window#pollEvents()}.
 */
public final class Input {
    private final Keyboard keyboard = new Keyboard();
    private final Mouse mouse = new Mouse();
    private final Gamepads gamepads = new Gamepads();
    private final TextInput text = new TextInput();

    /**
     * Updates keyboard, mouse, gamepad, and text state for the current frame.
     *
     * @param window polled window
     */
    public void beginFrame(Window window) {
        keyboard.beginFrame(window);
        mouse.beginFrame(window);
        text.beginFrame(window);
        gamepads.beginFrame();
    }

    public Keyboard keyboard() {
        return keyboard;
    }

    public Mouse mouse() {
        return mouse;
    }

    public Gamepads gamepads() {
        return gamepads;
    }

    public TextInput text() {
        return text;
    }
}
