package org.llw.studio.scene;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.SceneObjectIdComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Authoring container for a level: ECS {@link World}, hierarchy root, and object factory.
 * <p>
 * Used in the editor for scene tabs and cloned or driven during play mode. Object transforms
 * live on {@link org.llw.studio.ecs.components.Transform2DComponent} in Y-down world space.
 */
public final class Scene {
    private final World world = new World();
    private String name = "Untitled";

    /**
     * Creates a scene with a hidden {@code "Scene Root"} entity at the top of the hierarchy.
     */
    public Scene() {
        EntityId root = world.createEntity("Scene Root");
        world.addComponent(root, HierarchyComponent.class, new HierarchyComponent());
    }

    /**
     * @return ECS world backing this scene
     */
    public World world() {
        return world;
    }

    /**
     * @return scene asset display name
     */
    public String name() {
        return name;
    }

    /**
     * @param name new display name; blank values become {@code "Untitled"}
     */
    public void setName(String name) {
        this.name = name == null || name.isBlank() ? "Untitled" : name;
    }

    /**
     * Spawns a new top-level {@link GameObject} with hierarchy, active, and scene id components.
     *
     * @param objectName hierarchy name for the new object
     * @return wrapper around the new entity
     */
    public GameObject createGameObject(String objectName) {
        EntityId id = world.createEntity(objectName);
        world.addComponent(id, HierarchyComponent.class, new HierarchyComponent());
        world.addComponent(id, ActiveComponent.class, new ActiveComponent());
        SceneObjectIdComponent sceneObjectId = new SceneObjectIdComponent();
        sceneObjectId.sceneId = SceneObjectIds.allocate(this);
        world.addComponent(id, SceneObjectIdComponent.class, sceneObjectId);
        return new GameObject(world, id);
    }

    /**
     * Lists hierarchy roots excluding the internal scene root entity.
     *
     * @return game objects with no parent (not including {@code "Scene Root"})
     */
    public List<GameObject> rootObjects() {
        List<GameObject> roots = new ArrayList<>();
        var names = world.store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId id = names.entityAt(i);
            HierarchyComponent hierarchy = world.getComponent(id, HierarchyComponent.class);
            if (hierarchy == null || hierarchy.parentIndex < 0) {
                NameComponent nameComponent = names.componentAt(i);
                if (!"Scene Root".equals(nameComponent.name())) {
                    roots.add(new GameObject(world, id));
                }
            }
        }
        return roots;
    }

    /**
     * @param id entity to resolve
     * @return {@link GameObject} for a live entity, or {@code null} if destroyed or invalid
     */
    public GameObject find(EntityId id) {
        return world.isAlive(id) ? new GameObject(world, id) : null;
    }
}
