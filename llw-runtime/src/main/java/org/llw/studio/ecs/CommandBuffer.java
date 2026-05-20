package org.llw.studio.ecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Deferred command queue for structural {@link World} changes during system updates.
 * <p>
 * Used in both editor and play mode so systems can schedule entity or component mutations
 * without modifying stores while iterating them; commands run when {@link #flush(World)} is called.
 */
public final class CommandBuffer {
    private final List<Runnable> commands = new ArrayList<>();

    /**
     * Schedules a command to run on the next flush.
     *
     * @param command side effect to apply against a {@link World}; must not be {@code null}
     */
    public void enqueue(Runnable command) {
        commands.add(command);
    }

    /**
     * Runs all queued commands in order, then clears the buffer.
     *
     * @param world target world passed to each command (currently unused by the buffer itself)
     */
    public void flush(World world) {
        for (Runnable command : commands) {
            command.run();
        }
        commands.clear();
    }

    /**
     * Discards queued commands without running them.
     */
    public void clear() {
        commands.clear();
    }
}
