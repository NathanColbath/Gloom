package org.llw.render.gl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.llw.render.backend.RenderBackend;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Renderable;
import org.llw.util.log.FrameDiagnostics;

/**
 * Deferred draw list that sorts {@link Renderable} commands before submission to
 * {@link RenderBackend}.
 *
 * <p>Commands are ordered by {@link DrawState#sortKey(int)} (layer, then submission order)
 * when {@link #flush(RenderBackend)} is called.
 */
public final class DrawQueue {
    private final List<DrawCommand> commands = new ArrayList<>();
    private int submissionOrder;

    /**
     * Appends a renderable and its draw state to the queue without executing it.
     *
     * @param renderable object invoked during {@link #flush(RenderBackend)}
     * @param state      per-draw blend, shader, texture, transform, and layer
     */
    public void enqueue(Renderable renderable, DrawState state) {
        commands.add(new DrawCommand(renderable, state, submissionOrder++));
    }

    /**
     * Sorts queued commands, renders each through the backend, ends the frame, and clears the queue.
     *
     * @param backend OpenGL backend passed to each renderable's {@link Renderable#render}
     */
    public void flush(RenderBackend backend) {
        FrameDiagnostics.recordDrawItems(commands.size());
        commands.sort(Comparator.comparingLong(DrawCommand::sortKey));
        for (DrawCommand command : commands) {
            command.renderable().render(backend, command.state());
        }
        backend.endFrame();
        commands.clear();
        submissionOrder = 0;
    }

    void clear() {
        commands.clear();
        submissionOrder = 0;
    }

    record DrawCommand(Renderable renderable, DrawState state, int order) {
        long sortKey() {
            return state.sortKey(order);
        }
    }
}
