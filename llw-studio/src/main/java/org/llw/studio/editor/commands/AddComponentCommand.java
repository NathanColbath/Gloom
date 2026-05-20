package org.llw.studio.editor.commands;



import org.llw.studio.ecs.EntityId;

import org.llw.studio.scene.GameObject;

import org.llw.studio.scene.Scene;



/**

 * Undoable add of a component instance to a game object.

 */

public final class AddComponentCommand implements EditorCommand {

    private final Scene scene;

    private final EntityId entity;

    private final Class<?> type;

    private final Object component;



    /**

     * @param scene     edit scene

     * @param entity    target entity

     * @param type      component class

     * @param component component data to attach

     */

    public AddComponentCommand(Scene scene, EntityId entity, Class<?> type, Object component) {

        this.scene = scene;

        this.entity = entity;

        this.type = type;

        this.component = component;

    }



    /** {@inheritDoc} */

    @Override

    @SuppressWarnings("unchecked")

    public void execute() {

        GameObject object = scene.find(entity);

        if (object != null) {

            object.addComponent((Class<Object>) type, component);

        }

    }



    /** {@inheritDoc} */

    @Override

    @SuppressWarnings("unchecked")

    public void undo() {

        GameObject object = scene.find(entity);

        if (object != null) {

            object.removeComponent((Class<Object>) type);

        }

    }

}

