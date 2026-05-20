package org.llw.studio.scene;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Transform2DComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene-facing facade over a single {@link EntityId} in a {@link Scene}'s {@link World}.
 * <p>
 * Used for hierarchy editing in the editor and gameplay code in play mode. Local positions
 * from {@link #transform()} are in Y-down space relative to the parent.
 */
public final class GameObject {
    private final EntityId entity;
    private final World world;

    GameObject(World world, EntityId entity) {
        this.world = world;
        this.entity = entity;
    }

    /**
     * @return underlying ECS entity handle
     */
    public EntityId entity() {
        return entity;
    }

    /**
     * @return display name from {@link NameComponent}, or {@code "GameObject"} if absent
     */
    public String name() {
        NameComponent name = world.getComponent(entity, NameComponent.class);
        return name == null ? "GameObject" : name.name();
    }

    /**
     * @param name new display name stored as {@link NameComponent}
     */
    public void setName(String name) {
        NameComponent current = world.getComponent(entity, NameComponent.class);
        String tag = current == null ? "" : current.tag();
        world.addComponent(entity, NameComponent.class, new NameComponent(name, tag));
    }

    /**
     * @return gameplay tag from {@link NameComponent}, or {@code ""} if absent
     */
    public String tag() {
        NameComponent identity = world.getComponent(entity, NameComponent.class);
        return identity == null ? "" : identity.tag();
    }

    /**
     * @param tag new gameplay tag; blank values are stored as {@code ""}
     */
    public void setTag(String tag) {
        NameComponent current = world.getComponent(entity, NameComponent.class);
        String name = current == null ? "GameObject" : current.name();
        world.addComponent(entity, NameComponent.class, new NameComponent(name, tag));
    }

    /**
     * Ensures a {@link Transform2DComponent} exists and returns it.
     *
     * @return local transform (Y-down relative to parent)
     */
    public Transform2DComponent transform() {
        Transform2DComponent transform = world.getComponent(entity, Transform2DComponent.class);
        if (transform == null) {
            transform = new Transform2DComponent();
            world.addComponent(entity, Transform2DComponent.class, transform);
        }
        return transform;
    }

    /**
     * @param <T>  component type
     * @param type component class
     * @return component on this entity, or {@code null} if missing
     */
    public <T> T getComponent(Class<T> type) {
        return world.getComponent(entity, type);
    }

    /**
     * @param <T>       component type
     * @param type      component class
     * @param component component instance to attach or replace
     */
    public <T> void addComponent(Class<T> type, T component) {
        world.addComponent(entity, type, component);
    }

    /**
     * @param <T>  component type
     * @param type component class
     * @return {@code true} if this entity has {@code type}
     */
    public <T> boolean hasComponent(Class<T> type) {
        return world.hasComponent(entity, type);
    }

    /**
     * @param <T>  component type
     * @param type component class to detach
     */
    public <T> void removeComponent(Class<T> type) {
        world.removeComponent(entity, type);
    }

    /**
     * @return direct child game objects with live entity ids
     */
    public List<GameObject> children() {
        HierarchyComponent hierarchy = ensureHierarchy();
        List<GameObject> result = new ArrayList<>();
        for (HierarchyComponent.ChildRef child : hierarchy.children) {
            EntityId childId = child.toEntityId();
            if (world.isAlive(childId)) {
                result.add(new GameObject(world, childId));
            }
        }
        return result;
    }

    /**
     * @return parent game object, or {@code null} if at scene root or parent was destroyed
     */
    public GameObject parent() {
        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);
        if (hierarchy == null || hierarchy.parentIndex < 0) {
            return null;
        }
        EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
        return world.isAlive(parentId) ? new GameObject(world, parentId) : null;
    }

    /**
     * Reparents this object, updating {@link HierarchyComponent} links on both sides.
     * <p>
     * When {@code parent} is {@code null}, this object becomes a root. Reparenting to self or
     * a descendant is ignored. When {@code worldPositionStays} is {@code true}, world position
     * preservation is reserved for a future implementation; local transform is kept as-is today.
     *
     * @param parent              new parent, or {@code null} to detach to root
     * @param worldPositionStays  when {@code true}, intended to preserve world position (not yet applied)
     */
    public void setParent(GameObject parent, boolean worldPositionStays) {
        HierarchyComponent self = ensureHierarchy();
        GameObject oldParent = parent();
        if (oldParent != null) {
            removeChildLink(oldParent.entity(), entity);
        }
        if (parent == null) {
            self.parentIndex = -1;
            self.parentGeneration = 0;
            return;
        }
        if (parent.entity().equals(entity) || parent.isDescendantOf(this)) {
            return;
        }
        if (!worldPositionStays) {
            // keep local transform as-is
        }
        self.parentIndex = parent.entity().index();
        self.parentGeneration = parent.entity().generation();
        addChildLink(parent.entity(), entity);
    }

    /**
     * @param ancestor potential ancestor in the hierarchy
     * @return {@code true} if {@code ancestor} appears on the parent chain above this object
     */
    public boolean isDescendantOf(GameObject ancestor) {
        GameObject current = parent();
        while (current != null) {
            if (current.entity().equals(ancestor.entity())) {
                return true;
            }
            current = current.parent();
        }
        return false;
    }

    private HierarchyComponent ensureHierarchy() {
        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);
        if (hierarchy == null) {
            hierarchy = new HierarchyComponent();
            world.addComponent(entity, HierarchyComponent.class, hierarchy);
        }
        return hierarchy;
    }

    private void addChildLink(EntityId parentId, EntityId childId) {
        HierarchyComponent parentHierarchy = world.getComponent(parentId, HierarchyComponent.class);
        if (parentHierarchy == null) {
            return;
        }
        for (HierarchyComponent.ChildRef child : parentHierarchy.children) {
            if (child.index == childId.index() && child.generation == childId.generation()) {
                return;
            }
        }
        parentHierarchy.children.add(new HierarchyComponent.ChildRef(childId));
    }

    private void removeChildLink(EntityId parentId, EntityId childId) {
        HierarchyComponent parentHierarchy = world.getComponent(parentId, HierarchyComponent.class);
        if (parentHierarchy == null) {
            return;
        }
        parentHierarchy.children.removeIf(child ->
                child.index == childId.index() && child.generation == childId.generation());
    }
}
