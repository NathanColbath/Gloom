package org.llw.studio.editor;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first entity order matching the hierarchy tree (for Shift+click range selection).
 */
public final class HierarchyEntityOrder {
    private HierarchyEntityOrder() {
    }

    public static List<EntityId> collect(Scene scene) {
        List<EntityId> order = new ArrayList<>();
        if (scene == null) {
            return order;
        }
        for (GameObject root : scene.rootObjects()) {
            collectNode(root, order);
        }
        return order;
    }

    private static void collectNode(GameObject object, List<EntityId> order) {
        order.add(object.entity());
        for (GameObject child : object.children()) {
            collectNode(child, order);
        }
    }
}
