package org.llw.studio.scene;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.SceneObjectIdComponent;

/**
 * Allocates and resolves stable per-scene integer ids on {@link SceneObjectIdComponent}.
 * <p>
 * Editor selection and serialization use these ids; play mode resolves the same entities
 * through the backing {@link World}.
 */
public final class SceneObjectIds {
    private SceneObjectIds() {
    }

    /**
     * Returns the next unused scene object id for {@code scene}.
     *
     * @param scene scene whose existing ids are scanned
     * @return one greater than the maximum existing {@link SceneObjectIdComponent#sceneId},
     *         or {@code 0} when no ids exist yet
     */
    public static int allocate(Scene scene) {
        int max = -1;
        var store = scene.world().store(SceneObjectIdComponent.class);
        for (int i = 0; i < store.size(); i++) {
            max = Math.max(max, store.componentAt(i).sceneId);
        }
        return max + 1;
    }

    /**
     * Attaches or updates the scene object id on {@code entity}.
     *
     * @param scene    scene owning the entity
     * @param entity   target entity
     * @param sceneId  stable id to store
     */
    public static void assign(Scene scene, EntityId entity, int sceneId) {
        SceneObjectIdComponent id = scene.world().getComponent(entity, SceneObjectIdComponent.class);
        if (id == null) {
            id = new SceneObjectIdComponent();
            scene.world().addComponent(entity, SceneObjectIdComponent.class, id);
        }
        id.sceneId = sceneId;
    }

    /**
     * @param world   world to query
     * @param entity  entity to read
     * @return scene object id, or {@code -1} if none assigned
     */
    public static int get(World world, EntityId entity) {
        SceneObjectIdComponent id = world.getComponent(entity, SceneObjectIdComponent.class);
        return id == null ? -1 : id.sceneId;
    }

    /**
     * @param world    world to search
     * @param sceneId  id to locate
     * @return entity with matching scene id, or {@link EntityId#none()} if not found
     */
    public static EntityId findBySceneId(World world, int sceneId) {
        if (sceneId < 0) {
            return EntityId.none();
        }
        var store = world.store(SceneObjectIdComponent.class);
        for (int i = 0; i < store.size(); i++) {
            if (store.componentAt(i).sceneId == sceneId) {
                return store.entityAt(i);
            }
        }
        return EntityId.none();
    }

    /**
     * @param world   world to query
     * @param entity  entity to test
     * @return {@code true} if the entity is the internal {@code "Scene Root"} object
     */
    public static boolean isSceneRoot(World world, EntityId entity) {
        NameComponent name = world.getComponent(entity, NameComponent.class);
        return name != null && "Scene Root".equals(name.name());
    }
}
