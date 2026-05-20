package org.llw.studio.ecs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Dense, swap-remove storage of one component type keyed by {@link EntityId}.
 * <p>
 * Backing store for a {@link ComponentRegistry} entry; used in editor and play mode.
 * Spatial component fields (for example {@link org.llw.studio.ecs.components.Transform2DComponent})
 * use Y-down world coordinates when documented on the component type.
 */
public final class ComponentStore<T> {
    private final Class<T> type;
    private EntityId[] entities = new EntityId[16];
    private Object[] components = new Object[16];
    private int size;
    private final Map<EntityId, Integer> indexByEntity = new HashMap<>();

    /**
     * @param type component class this store holds
     */
    public ComponentStore(Class<T> type) {
        this.type = type;
    }

    /**
     * @return component class key for this store
     */
    public Class<T> type() {
        return type;
    }

    /**
     * Attaches or replaces the component for {@code entity}.
     *
     * @param entity    target entity
     * @param component component instance to store
     */
    public void add(EntityId entity, T component) {
        Integer existing = indexByEntity.get(entity);
        if (existing != null) {
            components[existing] = component;
            return;
        }
        ensureCapacity(size + 1);
        entities[size] = entity;
        components[size] = component;
        indexByEntity.put(entity, size);
        size++;
    }

    /**
     * @param entity queried entity
     * @return component for {@code entity}, or {@code null} if none
     */
    public T get(EntityId entity) {
        Integer index = indexByEntity.get(entity);
        if (index == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T value = (T) components[index];
        return value;
    }

    /**
     * @param entity queried entity
     * @return {@code true} if {@code entity} has a component in this store
     */
    public boolean has(EntityId entity) {
        return indexByEntity.containsKey(entity);
    }

    /**
     * Removes the component for {@code entity} using swap-with-last for O(1) removal.
     *
     * @param entity entity to detach from this store
     */
    public void remove(EntityId entity) {
        Integer index = indexByEntity.remove(entity);
        if (index == null) {
            return;
        }
        int last = size - 1;
        if (index != last) {
            EntityId movedEntity = entities[last];
            entities[index] = movedEntity;
            components[index] = components[last];
            indexByEntity.put(movedEntity, index);
        }
        entities[last] = null;
        components[last] = null;
        size--;
    }

    /**
     * @return number of entities with a component in this store
     */
    public int size() {
        return size;
    }

    /**
     * @param index dense index in {@code [0, size())}
     * @return entity at {@code index}
     */
    public EntityId entityAt(int index) {
        return entities[index];
    }

    /**
     * @param index dense index in {@code [0, size())}
     * @return component at {@code index}
     */
    public T componentAt(int index) {
        @SuppressWarnings("unchecked")
        T value = (T) components[index];
        return value;
    }

    /**
     * Removes all components and resets dense storage.
     */
    public void clear() {
        Arrays.fill(entities, 0, size, null);
        Arrays.fill(components, 0, size, null);
        indexByEntity.clear();
        size = 0;
    }

    private void ensureCapacity(int required) {
        if (required <= entities.length) {
            return;
        }
        int capacity = Math.max(required, entities.length * 2);
        entities = Arrays.copyOf(entities, capacity);
        components = Arrays.copyOf(components, capacity);
    }
}
