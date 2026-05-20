package org.llw.render.input;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tracks per-frame pressed/released edges for enum input identifiers.
 */
final class InputEdges {
    private InputEdges() {
    }

    static <T extends Enum<T>> void update(
            Set<T> current,
            EnumSet<T> previous,
            EnumSet<T> pressed,
            EnumSet<T> released
    ) {
        pressed.clear();
        released.clear();
        for (T value : current) {
            if (!previous.contains(value)) {
                pressed.add(value);
            }
        }
        for (T value : previous) {
            if (!current.contains(value)) {
                released.add(value);
            }
        }
        previous.clear();
        previous.addAll(current);
    }
}
