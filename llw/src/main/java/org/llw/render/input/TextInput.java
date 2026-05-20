package org.llw.render.input;

import org.llw.render.window.Window;

/**
 * Per-frame typed text collected from the window char callback.
 */
public final class TextInput {
    private String frameText = "";

    void beginFrame(Window window) {
        frameText = window.takeEnteredText();
    }

    /**
     * Returns whether any text was entered this frame.
     */
    public boolean hasText() {
        return !frameText.isEmpty();
    }

    /**
     * Returns text entered this frame (empty string when none).
     */
    public String text() {
        return frameText;
    }

    /**
     * Returns text entered this frame and clears the stored value until the next frame.
     */
    public String consume() {
        String value = frameText;
        frameText = "";
        return value;
    }
}
