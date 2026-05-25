package org.llw.studio.editor.commands;

import java.util.ArrayDeque;
import java.util.Deque;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;

/**
 * Undo/redo stack of {@link EditorCommand} instances for the edit scene.
 */
public final class UndoStack {
    private final Deque<EditorCommand> undo = new ArrayDeque<>();
    private final Deque<EditorCommand> redo = new ArrayDeque<>();
    private final Scene scene;

    /**
     * @param scene scene commands mutate (typically {@link org.llw.studio.editor.StudioContext#editScene()})
     */
    public UndoStack(Scene scene) {
        this.scene = scene;
    }

    /**
     * Runs {@code command} and pushes it onto the undo stack, clearing redo.
     *
     * @param command command to apply
     */
    public void execute(EditorCommand command) {
        command.execute();
        undo.push(command);
        redo.clear(); // New edit branch invalidates redo history.
    }

    /**
     * Records a full transform edit (position, rotation, scale).
     *
     * @param entity target entity
     * @param before state before the edit
     * @param after  state after the edit
     */
    public void recordTransformEdit(EntityId entity, TransformSnapshot before, TransformSnapshot after) {
        execute(new TransformEditCommand(scene, entity, before, after));
    }

    /**
     * Records a position-only transform change.
     *
     * @param entity target entity
     * @param oldX   previous X
     * @param oldY   previous Y
     * @param newX   new X
     * @param newY   new Y
     */
    public void recordTransform(EntityId entity, float oldX, float oldY, float newX, float newY) {
        execute(new TransformCommand(scene, entity, oldX, oldY, newX, newY));
    }

    /** Pops and undoes the latest command, pushing it to redo. */
    public void undo() {
        if (undo.isEmpty()) {
            return;
        }
        EditorCommand command = undo.pop();
        command.undo();
        redo.push(command);
    }

    /** Re-applies the latest redo command. */
    public void redo() {
        if (redo.isEmpty()) {
            return;
        }
        EditorCommand command = redo.pop();
        command.execute();
        undo.push(command);
    }

    /**
     * Clears undo/redo history (e.g. after loading a new scene).
     *
     * @param newScene unused; reserved for future scene rebinding
     */
    public void reset(Scene newScene) {
        // Undo history is tied to the loaded edit scene; discard on scene load/switch.
        undo.clear();
        redo.clear();
    }

    /** @return whether {@link #undo()} would change state */
    public boolean canUndo() {
        return !undo.isEmpty();
    }

    /** @return whether {@link #redo()} would change state */
    public boolean canRedo() {
        return !redo.isEmpty();
    }
}
