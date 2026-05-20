package org.llw.studio.ecs;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Transform2DComponent;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {
    @Test
    void createEntityWithComponents() {
        World world = new World();
        EntityId id = world.createEntity("Hero");
        assertTrue(world.isAlive(id));
        assertEquals("Hero", world.getComponent(id, NameComponent.class).name());
        assertNotNull(world.getComponent(id, Transform2DComponent.class));
        world.destroyEntity(id);
        assertFalse(world.isAlive(id));
    }
}
