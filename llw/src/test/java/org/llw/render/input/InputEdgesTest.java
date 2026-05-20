package org.llw.render.input;

import org.junit.jupiter.api.Test;
import org.llw.render.window.Key;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputEdgesTest {

    @Test
    void detectsPressedAndReleased() {
        EnumSet<Key> previous = EnumSet.noneOf(Key.class);
        EnumSet<Key> pressed = EnumSet.noneOf(Key.class);
        EnumSet<Key> released = EnumSet.noneOf(Key.class);

        InputEdges.update(Set.of(Key.A, Key.B), previous, pressed, released);
        assertTrue(pressed.contains(Key.A));
        assertTrue(pressed.contains(Key.B));
        assertTrue(released.isEmpty());

        InputEdges.update(Set.of(Key.B, Key.C), previous, pressed, released);
        assertTrue(pressed.contains(Key.C));
        assertFalse(pressed.contains(Key.A));
        assertTrue(released.contains(Key.A));
    }
}
