package org.llw.studio.editor.commands;



import org.llw.studio.ecs.EntityId;

import org.llw.studio.ecs.components.Transform2DComponent;

import org.llw.studio.scene.Scene;



/**

 * Undoable full {@link Transform2DComponent} snapshot (position, rotation, scale).

 */

public final class TransformEditCommand implements EditorCommand {

    private final Scene scene;

    private final EntityId entity;

    private final TransformSnapshot before;

    private final TransformSnapshot after;



    /**

     * @param scene  edit scene

     * @param entity target entity

     * @param before state before the edit

     * @param after  state after the edit

     */

    public TransformEditCommand(Scene scene, EntityId entity, TransformSnapshot before, TransformSnapshot after) {

        this.scene = scene;

        this.entity = entity;

        this.before = before;

        this.after = after;

    }



    /** {@inheritDoc} */

    @Override

    public void execute() {

        apply(after);

    }



    /** {@inheritDoc} */

    @Override

    public void undo() {

        apply(before);

    }



    private void apply(TransformSnapshot snapshot) {

        Transform2DComponent transform = scene.world().getComponent(entity, Transform2DComponent.class);

        if (transform != null) {

            snapshot.applyTo(transform);

        }

    }

}

