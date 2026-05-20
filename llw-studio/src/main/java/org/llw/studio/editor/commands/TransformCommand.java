package org.llw.studio.editor.commands;



import org.llw.studio.ecs.EntityId;

import org.llw.studio.ecs.components.Transform2DComponent;

import org.llw.studio.scene.Scene;



/**

 * Undoable change of {@link Transform2DComponent} local X/Y only.

 */

public final class TransformCommand implements EditorCommand {

    private final Scene scene;

    private final EntityId entity;

    private final float oldX;

    private final float oldY;

    private final float newX;

    private final float newY;



    /**

     * @param scene  edit scene

     * @param entity target entity

     * @param oldX   previous local X

     * @param oldY   previous local Y

     * @param newX   new local X

     * @param newY   new local Y

     */

    public TransformCommand(Scene scene, EntityId entity, float oldX, float oldY, float newX, float newY) {

        this.scene = scene;

        this.entity = entity;

        this.oldX = oldX;

        this.oldY = oldY;

        this.newX = newX;

        this.newY = newY;

    }



    /** {@inheritDoc} */

    @Override

    public void execute() {

        apply(newX, newY);

    }



    /** {@inheritDoc} */

    @Override

    public void undo() {

        apply(oldX, oldY);

    }



    private void apply(float x, float y) {

        Transform2DComponent transform = scene.world().getComponent(entity, Transform2DComponent.class);

        if (transform != null) {

            transform.x = x;

            transform.y = y;

        }

    }

}

