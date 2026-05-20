package org.llw.studio.memory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Defers native/resource disposal until the end of the studio frame so teardown does not
 * run mid-render or mid-ImGui layout.
 */
public final class StudioMemory {
    private static final Deque<Runnable> deferred = new ArrayDeque<>();

    private StudioMemory() {
    }

    /**
     * Queues an action to run at the next {@link #endFrame()} call (FIFO order).
     *
     * @param action disposal or cleanup runnable
     */
    public static void deferDispose(Runnable action) {
        deferred.addLast(action);
    }

    /** Runs all deferred actions queued since the previous frame. */
    public static void endFrame() {
        while (!deferred.isEmpty()) {
            deferred.removeFirst().run();
        }
    }
}
