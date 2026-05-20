package org.llw.studio.ui;

import org.llw.math.geometry.RectF;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Collects screen-space UI widgets under active {@link UICanvasComponent} roots.
 */
public final class UiLayout {
    private UiLayout() {
    }

    /**
     * @param scene play or edit scene
     * @return widgets sorted for draw and hit-test (later items draw on top)
     */
    public static List<UiDrawItem> collect(Scene scene) {
        World world = scene.world();
        ComponentStore<UICanvasComponent> canvases = world.store(UICanvasComponent.class);
        List<CanvasRoot> roots = new ArrayList<>();
        for (int i = 0; i < canvases.size(); i++) {
            EntityId entity = canvases.entityAt(i);
            UICanvasComponent canvas = canvases.componentAt(i);
            if (!canvas.enabled || !ActiveUtility.isEffectivelyActive(world, entity)) {
                continue;
            }
            roots.add(new CanvasRoot(entity, canvas.sortingOrder));
        }
        roots.sort(Comparator.comparingInt(root -> root.sortingOrder));

        List<UiDrawItem> items = new ArrayList<>();
        int treeOrder = 0;
        for (CanvasRoot root : roots) {
            Transform2DComponent transform = world.getComponent(root.entity, Transform2DComponent.class);
            float originX = transform == null ? 0f : transform.x;
            float originY = transform == null ? 0f : transform.y;
            collectChildren(world, root.entity, root.sortingOrder, originX, originY, items, new int[]{treeOrder});
            treeOrder = items.size();
        }
        return items;
    }

    private static void collectChildren(
            World world,
            EntityId entity,
            int canvasSortingOrder,
            float parentX,
            float parentY,
            List<UiDrawItem> items,
            int[] treeOrder
    ) {
        HierarchyComponent hierarchy = world.getComponent(entity, HierarchyComponent.class);
        if (hierarchy == null) {
            return;
        }
        for (HierarchyComponent.ChildRef childRef : hierarchy.children) {
            EntityId child = childRef.toEntityId();
            if (!world.isAlive(child) || !ActiveUtility.isEffectivelyActive(world, child)) {
                continue;
            }
            Transform2DComponent transform = world.getComponent(child, Transform2DComponent.class);
            float x = parentX + (transform == null ? 0f : transform.x);
            float y = parentY + (transform == null ? 0f : transform.y);

            UILabelComponent label = world.getComponent(child, UILabelComponent.class);
            if (label != null) {
                items.add(new UiDrawItem(
                        child,
                        UiWidgetKind.LABEL,
                        canvasSortingOrder,
                        treeOrder[0]++,
                        new RectF(x, y, label.width, label.height),
                        label,
                        null,
                        null,
                        null
                ));
            }
            UIButtonComponent button = world.getComponent(child, UIButtonComponent.class);
            if (button != null) {
                items.add(new UiDrawItem(
                        child,
                        UiWidgetKind.BUTTON,
                        canvasSortingOrder,
                        treeOrder[0]++,
                        new RectF(x, y, button.width, button.height),
                        null,
                        button,
                        null,
                        null
                ));
            }
            UIToggleComponent toggle = world.getComponent(child, UIToggleComponent.class);
            if (toggle != null) {
                items.add(new UiDrawItem(
                        child,
                        UiWidgetKind.TOGGLE,
                        canvasSortingOrder,
                        treeOrder[0]++,
                        new RectF(x, y, toggle.width, toggle.height),
                        null,
                        null,
                        toggle,
                        null
                ));
            }
            UITextFieldComponent field = world.getComponent(child, UITextFieldComponent.class);
            if (field != null) {
                items.add(new UiDrawItem(
                        child,
                        UiWidgetKind.TEXT_FIELD,
                        canvasSortingOrder,
                        treeOrder[0]++,
                        new RectF(x, y, field.width, field.height),
                        null,
                        null,
                        null,
                        field
                ));
            }
            collectChildren(world, child, canvasSortingOrder, x, y, items, treeOrder);
        }
    }

    private record CanvasRoot(EntityId entity, int sortingOrder) {
    }
}
