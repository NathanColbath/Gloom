package org.llw.studio.editor.commands;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;

/**
 * Undoable replacement of a single ECS component snapshot on an entity.
 *
 * @param <T> component type
 */
public final class ComponentFieldEditCommand<T> implements EditorCommand {
    private final Scene scene;
    private final EntityId entity;
    private final Class<T> componentType;
    private final T before;
    private final T after;

    /**
     * @param scene         edit scene
     * @param entity        target entity
     * @param componentType component class
     * @param before        state before edit (from {@code copy()})
     * @param after         state after edit (from {@code copy()})
     */
    public ComponentFieldEditCommand(Scene scene, EntityId entity, Class<T> componentType, T before, T after) {
        this.scene = scene;
        this.entity = entity;
        this.componentType = componentType;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        apply(after);
    }

    @Override
    public void undo() {
        apply(before);
    }

    private void apply(T snapshot) {
        if (snapshot != null) {
            // ECS has no partial-field API; replace the whole component snapshot.
            scene.world().addComponent(entity, componentType, snapshot);
        }
    }
}
