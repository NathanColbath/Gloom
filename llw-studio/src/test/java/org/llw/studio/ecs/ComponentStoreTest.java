package org.llw.studio.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentStoreTest {
    @Test
    void addGetRemove() {
        ComponentStore<String> store = new ComponentStore<>(String.class);
        EntityId entity = new EntityId(0, 0);
        store.add(entity, "hello");
        assertEquals("hello", store.get(entity));
        assertTrue(store.has(entity));
        store.remove(entity);
        assertNull(store.get(entity));
        assertFalse(store.has(entity));
    }
}
