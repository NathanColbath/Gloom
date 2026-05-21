package org.llw.studio.editor.widgets;

import java.util.Objects;

/**
 * Tracks per-field edit state across frames to prevent committed values from
 * overwriting active user edits in ImGui input fields.
 *
 * <p>Usage pattern in a {@code ComponentDrawer}:
 * <pre>{@code
 * private final TextFieldEditState valueState = new TextFieldEditState();
 *
 * void draw(...) {
 *     if (valueState.shouldSync(valueBuffer, component.value)) {
 *         valueBuffer.set(component.value == null ? "" : component.value);
 *     }
 *     if (ImGui.inputText("Value", valueBuffer)) {
 *         component.value = valueBuffer.get();
 *     }
 *     valueState.setActive(ImGui.isItemActive());
 * }
 * }</pre>
 */
public final class TextFieldEditState {

    private boolean wasActive;

    /** Returns whether the field was active during the previous frame. */
    public boolean wasActive() {
        return wasActive;
    }

    /** Set after drawing the field, from {@code ImGui.isItemActive()}. */
    public void setActive(boolean active) {
        this.wasActive = active;
    }

    /**
     * Returns {@code true} when the buffer should be synced from the committed
     * value. The buffer is synced only when the field was NOT active last frame
     * (i.e. the user is not mid-edit). When the field is actively being edited,
     * the buffer must be left alone to preserve the user's typed text.
     *
     * @param wasActive whether the field was active last frame
     * @param bufferContent the current ImString buffer content
     * @param committedValue the committed component value
     * @return {@code true} if the buffer should be re-synced from committedValue
     */
    public static boolean shouldSyncFromCommitted(boolean wasActive, String bufferContent, String committedValue) {
        if (wasActive) {
            return false;
        }
        String expected = committedValue == null ? "" : committedValue;
        return !Objects.equals(bufferContent, expected);
    }

    /**
     * Convenience instance method wrapping {@link #shouldSyncFromCommitted}.
     *
     * @param bufferContent the current ImString buffer content
     * @param committedValue the committed component value
     * @return {@code true} if the buffer should be re-synced
     */
    public boolean shouldSync(String bufferContent, String committedValue) {
        return shouldSyncFromCommitted(wasActive, bufferContent, committedValue);
    }
}
