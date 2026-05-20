package org.llw.studio.scripting;



import org.llw.studio.ecs.EntityId;

import org.llw.studio.ecs.World;



/**

 * Per-instance runtime context for legacy Java {@link ScriptBehaviour} scripts.

 */

public final class ScriptContext {

    private final World world;

    private final EntityId entity;



    /**

     * @param world  play-mode ECS world

     * @param entity entity this script is attached to

     */

    public ScriptContext(World world, EntityId entity) {

        this.world = world;

        this.entity = entity;

    }



    /**

     * @return the play-mode ECS world

     */

    public World world() {

        return world;

    }



    /**

     * @return the entity this script is attached to

     */

    public EntityId entity() {

        return entity;

    }

}

