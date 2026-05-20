package org.llw.studio.ecs;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps component Java types to their {@link ComponentStore} instances for a {@link World}.
 * <p>
 * Shared by editor scene authoring and play mode; stores are created on first
 * {@link #register(Class)} or lookup via {@link #get(Class)}.
 */
public final class ComponentRegistry {
    private final Map<Class<?>, ComponentStore<?>> stores = new HashMap<>();

    /**
     * Returns the store for {@code type}, creating and registering one if absent.
     *
     * @param <T>   component type
     * @param type  component class key
     * @return existing or newly created store for {@code type}
     */
    public <T> ComponentStore<T> register(Class<T> type) {
        @SuppressWarnings("unchecked")
        ComponentStore<T> existing = (ComponentStore<T>) stores.get(type);
        if (existing != null) {
            return existing;
        }
        ComponentStore<T> store = new ComponentStore<>(type);
        stores.put(type, store);
        return store;
    }

    /**
     * Looks up a store without registering.
     *
     * @param <T>   component type
     * @param type  component class key
     * @return store for {@code type}, or {@code null} if never registered
     */
    @SuppressWarnings("unchecked")
    public <T> ComponentStore<T> get(Class<T> type) {
        return (ComponentStore<T>) stores.get(type);
    }

    /**
     * @return all registered stores (iteration order undefined)
     */
    public Iterable<ComponentStore<?>> stores() {
        return stores.values();
    }

    /**
     * @return all component types that have been registered
     */
    public Iterable<Class<?>> types() {
        return stores.keySet();
    }

    /**
     * Removes every component from every store without unregistering types.
     */
    public void clear() {
        for (ComponentStore<?> store : stores.values()) {
            store.clear();
        }
    }
}
