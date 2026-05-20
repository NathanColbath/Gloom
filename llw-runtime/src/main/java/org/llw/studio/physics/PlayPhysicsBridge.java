package org.llw.studio.physics;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.scene.GameObject;

/**
 * Play-mode access to the active {@link PhysicsWorld}.
 */
public final class PlayPhysicsBridge {
    private static PhysicsWorld active;
    private static org.llw.studio.scripting.js.bindings.ScriptHostApi hostApi;

    private PlayPhysicsBridge() {
    }

    public static void setActive(PhysicsWorld world) {
        active = world;
    }

    public static PhysicsWorld active() {
        return active;
    }

    public static void setHostApi(org.llw.studio.scripting.js.bindings.ScriptHostApi hostApi) {
        PlayPhysicsBridge.hostApi = hostApi;
    }

    public static org.llw.studio.scripting.js.bindings.ScriptHostApi hostApi() {
        return hostApi;
    }

    public static void clear() {
        active = null;
        hostApi = null;
    }

    /**
     * Registers Box2D bodies for a prefab or clone spawned during play mode.
     */
    public static void registerSpawnedObject(World world, GameObject root) {
        if (active == null || world == null || root == null) {
            return;
        }
        active.registerSubtree(world, root);
    }

    /**
     * Destroys the Box2D body for an entity removed from the play scene.
     */
    public static void unregisterEntity(EntityId entity) {
        if (active == null || entity == null || entity.isNone()) {
            return;
        }
        active.unregisterEntity(entity);
    }
}
