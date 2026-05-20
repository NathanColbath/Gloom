package org.llw.studio.ecs;

import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Transform2DComponent;

/**
 * Central ECS container: entities, components, deferred commands, and systems.
 * <p>
 * Owned by {@link org.llw.studio.scene.Scene} for editor authoring and play mode simulation.
 * Default entities created via {@link #createEntity(String)} include {@link Transform2DComponent}
 * with positions in Y-down world space.
 */
public final class World {
    private final EntityRegistry entities = new EntityRegistry();
    private final ComponentRegistry components = new ComponentRegistry();
    private final CommandBuffer commandBuffer = new CommandBuffer();
    private final SystemScheduler scheduler = new SystemScheduler();

    /**
     * Creates a bare entity with no components.
     *
     * @return new live entity id
     */
    public EntityId createEntity() {
        return entities.create();
    }

    /**
     * Creates an entity with {@link NameComponent} and default {@link Transform2DComponent}.
     *
     * @param name display name; blank values become {@code "GameObject"}
     * @return new live entity id
     */
    public EntityId createEntity(String name) {
        EntityId id = entities.create();
        addComponent(id, NameComponent.class, new NameComponent(name));
        addComponent(id, Transform2DComponent.class, new Transform2DComponent());
        return id;
    }

    /**
     * Removes all components for {@code id} and recycles its entity slot.
     *
     * @param id entity to destroy; no-op if not alive
     */
    public void destroyEntity(EntityId id) {
        if (!entities.isAlive(id)) {
            return;
        }
        for (ComponentStore<?> store : components.stores()) {
            store.remove(id);
        }
        entities.destroy(id);
    }

    /**
     * @param id entity to test
     * @return {@code true} if {@code id} refers to a live entity
     */
    public boolean isAlive(EntityId id) {
        return entities.isAlive(id);
    }

    /**
     * Attaches or replaces a component on {@code id}, registering the store if needed.
     *
     * @param <T>       component type
     * @param id        target entity
     * @param type      component class
     * @param component component instance
     */
    public <T> void addComponent(EntityId id, Class<T> type, T component) {
        components.register(type).add(id, component);
    }

    /**
     * @param <T>   component type
     * @param id    target entity
     * @param type  component class
     * @return component for {@code id}, or {@code null} if missing or type not registered
     */
    public <T> T getComponent(EntityId id, Class<T> type) {
        ComponentStore<T> store = components.get(type);
        return store == null ? null : store.get(id);
    }

    /**
     * @param <T>   component type
     * @param id    target entity
     * @param type  component class
     * @return {@code true} if {@code id} has a component of {@code type}
     */
    public <T> boolean hasComponent(EntityId id, Class<T> type) {
        ComponentStore<T> store = components.get(type);
        return store != null && store.has(id);
    }

    /**
     * Detaches a component from {@code id} if present.
     *
     * @param <T>   component type
     * @param id    target entity
     * @param type  component class
     */
    public <T> void removeComponent(EntityId id, Class<T> type) {
        ComponentStore<T> store = components.get(type);
        if (store != null) {
            store.remove(id);
        }
    }

    /**
     * Returns the store for {@code type}, registering it if absent.
     *
     * @param <T>   component type
     * @param type  component class
     * @return dense store for iteration and bulk access
     */
    public <T> ComponentStore<T> store(Class<T> type) {
        return components.register(type);
    }

    /**
     * @return component type registry for this world
     */
    public ComponentRegistry componentRegistry() {
        return components;
    }

    /**
     * @return deferred mutation queue for systems
     */
    public CommandBuffer commandBuffer() {
        return commandBuffer;
    }

    /**
     * @return system execution scheduler
     */
    public SystemScheduler scheduler() {
        return scheduler;
    }

    /**
     * Clears commands, systems, all component data, and all entities.
     */
    public void clear() {
        commandBuffer.clear();
        scheduler.clear();
        components.clear();
        entities.clear();
    }
}
