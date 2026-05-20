package org.llw.studio.scene;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.ActiveComponent;

import static org.junit.jupiter.api.Assertions.*;

class GameObjectHierarchyTest {
    @Test
    void setParentLinksChild() {
        Scene scene = new Scene();
        GameObject parent = scene.createGameObject("Parent");
        GameObject child = scene.createGameObject("Child");
        child.setParent(parent, false);
        assertEquals(1, parent.children().size());
        assertEquals(parent.entity(), child.parent().entity());
    }

    @Test
    void activeDefaultsTrue() {
        Scene scene = new Scene();
        GameObject object = scene.createGameObject("Obj");
        ActiveComponent active = object.getComponent(ActiveComponent.class);
        assertNotNull(active);
        assertTrue(active.selfActive);
    }
}
