package org.llw.studio.editor;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe queue of runnables executed on the GLFW main thread during editor shell render.
 */
public final class MainThreadQueue {
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    /**
     * Schedules a task for the next {@link #flush()}; ignored if {@code task} is null.
     *
     * @param task work to run on the main thread
     */
    public void enqueue(Runnable task) {
        if (task != null) {
            tasks.add(task);
        }
    }

    /**
     * Runs all queued tasks until the queue is empty.
     */
    public void flush() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }
}
