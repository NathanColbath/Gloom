package org.llw.studio.ui;

import org.junit.jupiter.api.Test;
import org.llw.studio.editor.widgets.TextFieldEditState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the edit-preservation logic in {@link org.llw.studio.editor.widgets.TextField}
 * and {@link TextFieldEditState}. Ensures that active user edits are not overwritten
 * by committed/display values while the field is being edited.
 */
class TextFieldEditTest {

    @Test
    void initialStateIsInactive() {
        TextFieldEditState state = new TextFieldEditState();
        assertFalse(state.wasActive());
    }

    @Test
    void tracksActiveStateAcrossFrames() {
        TextFieldEditState state = new TextFieldEditState();

        // Simulate frame 1: user activates the field
        state.setActive(true);
        assertTrue(state.wasActive());

        // Frame 2: still active
        state.setActive(true);
        assertTrue(state.wasActive());

        // Frame 3: deactivated
        state.setActive(false);
        assertFalse(state.wasActive());
    }

    @Test
    void shouldSyncFromCommittedWhenInactive() {
        // When the field was NOT active last frame, the buffer should sync
        assertTrue(TextFieldEditState.shouldSyncFromCommitted(false, "buffer", "value"));
    }

    @Test
    void shouldNotSyncFromCommittedWhenActive() {
        // When the field WAS active last frame, the buffer should NOT be overwritten
        assertFalse(TextFieldEditState.shouldSyncFromCommitted(true, "user typing", "stored value"));
    }

    @Test
    void shouldNotSyncWhenInactiveAndBufferAlreadyMatches() {
        // When buffer already matches committed, no sync needed (avoids unnecessary buffer.set)
        assertFalse(TextFieldEditState.shouldSyncFromCommitted(false, "hello", "hello"));
    }

    @Test
    void shouldNotSyncWhenActiveAndBufferAlreadyMatches() {
        // When active, never sync regardless of match status
        assertFalse(TextFieldEditState.shouldSyncFromCommitted(true, "hello", "hello"));
    }

    @Test
    void shouldSyncWhenInactiveAndCommittedValueChanges() {
        // When inactive and the committed value changes externally, buffer should pick it up
        assertTrue(TextFieldEditState.shouldSyncFromCommitted(false, "old", "new value"));
    }

    @Test
    void shouldNotSyncWhenActiveEvenIfCommittedValueChanges() {
        // Active edit must be preserved even when underlying committed value changes
        assertFalse(TextFieldEditState.shouldSyncFromCommitted(true, "my edit", "external change"));
    }

    @Test
    void shouldHandleNullCommittedValue() {
        // Null committed value is treated as empty string
        assertTrue(TextFieldEditState.shouldSyncFromCommitted(false, "something", null));
    }

    @Test
    void shouldHandleNullCommittedValueWhenBufferIsAlsoEmpty() {
        // Both buffer and null committed are conceptually empty → no sync needed
        assertFalse(TextFieldEditState.shouldSyncFromCommitted(false, "", null));
    }

    @Test
    void shouldHandleBufferChangeDuringInactivePeriod() {
        // Full lifecycle: simulate multi-frame draw cycle
        TextFieldEditState state = new TextFieldEditState();

        // Frame 1: inactive, buffer is empty, committed is "hello" → sync needed
        assertTrue(state.shouldSync("", "hello"));

        // Frame 2: user activates, buffer has their edit "hello world"
        state.setActive(true);
        // Even if committed still says "hello" (out of date), active → no sync
        assertFalse(state.shouldSync("hello world", "hello"));

        // Frame 3: still typing
        state.setActive(true);
        assertFalse(state.shouldSync("hello world!!", "hello"));

        // Frame 4: user finished editing (deactivated), committed is "hello"
        state.setActive(false);
        // Buffer "hello world!!" != committed "hello" → should sync
        assertTrue(state.shouldSync("hello world!!", "hello"));

        // Frame 5: still inactive, buffer was synced to "hello", committed is still "hello" → no sync
        state.setActive(false);
        assertFalse(state.shouldSync("hello", "hello"));
    }
}
