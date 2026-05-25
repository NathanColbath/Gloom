package org.llw.studio.ui;

import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;
import org.llw.render.graphics.Camera2d;
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
import org.llw.studio.ecs.components.WorldTransformComponent;
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
        return collect(scene, UiLayoutContext.forPlay());
    }

    /**
     * @param scene play or edit scene
     * @param ctx   camera, viewport, optional canvas filter, authoring flag
     * @return widgets sorted for draw and hit-test (later items draw on top)
     */
    public static List<UiDrawItem> collect(Scene scene, UiLayoutContext ctx) {
        if (scene == null || ctx == null) {
            return List.of();
        }
        World world = scene.world();
        ComponentStore<UICanvasComponent> canvases = world.store(UICanvasComponent.class);
        List<CanvasRoot> roots = new ArrayList<>();
        for (int i = 0; i < canvases.size(); i++) {
            EntityId entity = canvases.entityAt(i);
            if (!ctx.filterCanvas.isNone() && !entity.equals(ctx.filterCanvas)) {
                continue;
            }
            UICanvasComponent canvas = canvases.componentAt(i);
            if (!canvas.enabled || !ActiveUtility.isEffectivelyActive(world, entity)) {
                continue;
            }
            roots.add(new CanvasRoot(entity, canvas));
        }
        roots.sort(Comparator.comparingInt(root -> root.canvas.sortingOrder));

        List<UiDrawItem> items = new ArrayList<>();
        int treeOrder = 0;
        for (CanvasRoot root : roots) {
            UiCanvasRenderMode mode = ctx.authoringSpace
                    ? UiCanvasRenderMode.SCREEN_SPACE
                    : root.canvas.renderMode;
            float screenScaleX = 1f;
            float screenScaleY = 1f;
            if (mode == UiCanvasRenderMode.SCREEN_SPACE && !ctx.authoringSpace) {
                screenScaleX = ctx.viewportWidth / (float) Math.max(1, root.canvas.referenceWidth);
                screenScaleY = ctx.viewportHeight / (float) Math.max(1, root.canvas.referenceHeight);
            }
            float originX = 0f;
            float originY = 0f;
            if (mode == UiCanvasRenderMode.SCREEN_SPACE) {
                Transform2DComponent canvasTransform = world.getComponent(root.entity, Transform2DComponent.class);
                if (canvasTransform != null) {
                    originX = canvasTransform.x;
                    originY = canvasTransform.y;
                }
                originX *= screenScaleX;
                originY *= screenScaleY;
            } else {
                WorldTransformComponent worldTransform = world.getComponent(root.entity, WorldTransformComponent.class);
                if (worldTransform != null) {
                    originX = worldTransform.worldX;
                    originY = worldTransform.worldY;
                } else {
                    Transform2DComponent local = world.getComponent(root.entity, Transform2DComponent.class);
                    if (local != null) {
                        originX = local.x;
                        originY = local.y;
                    }
                }
            }
            collectChildren(
                    world,
                    root.entity,
                    root.canvas.sortingOrder,
                    mode,
                    ctx,
                    screenScaleX,
                    screenScaleY,
                    originX,
                    originY,
                    items,
                    new int[]{treeOrder}
            );
            treeOrder = items.size();
        }
        return items;
    }

    private static void collectChildren(
            World world,
            EntityId entity,
            int canvasSortingOrder,
            UiCanvasRenderMode mode,
            UiLayoutContext ctx,
            float screenScaleX,
            float screenScaleY,
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
            float localX = transform == null ? 0f : transform.x;
            float localY = transform == null ? 0f : transform.y;
            if (mode == UiCanvasRenderMode.SCREEN_SPACE && !ctx.authoringSpace) {
                localX *= screenScaleX;
                localY *= screenScaleY;
            }
            float x = parentX + localX;
            float y = parentY + localY;

            UILabelComponent label = world.getComponent(child, UILabelComponent.class);
            if (label != null) {
                addItem(items, child, UiWidgetKind.LABEL, canvasSortingOrder, treeOrder, mode, ctx,
                        screenScaleX, screenScaleY, x, y, label.width, label.height, label, null, null, null);
            }
            UIButtonComponent button = world.getComponent(child, UIButtonComponent.class);
            if (button != null) {
                addItem(items, child, UiWidgetKind.BUTTON, canvasSortingOrder, treeOrder, mode, ctx,
                        screenScaleX, screenScaleY, x, y, button.width, button.height, null, button, null, null);
            }
            UIToggleComponent toggle = world.getComponent(child, UIToggleComponent.class);
            if (toggle != null) {
                addItem(items, child, UiWidgetKind.TOGGLE, canvasSortingOrder, treeOrder, mode, ctx,
                        screenScaleX, screenScaleY, x, y, toggle.width, toggle.height, null, null, toggle, null);
            }
            UITextFieldComponent field = world.getComponent(child, UITextFieldComponent.class);
            if (field != null) {
                addItem(items, child, UiWidgetKind.TEXT_FIELD, canvasSortingOrder, treeOrder, mode, ctx,
                        screenScaleX, screenScaleY, x, y, field.width, field.height, null, null, null, field);
            }
            collectChildren(world, child, canvasSortingOrder, mode, ctx, screenScaleX, screenScaleY, x, y, items, treeOrder);
        }
    }

    private static void addItem(
            List<UiDrawItem> items,
            EntityId child,
            UiWidgetKind kind,
            int canvasSortingOrder,
            int[] treeOrder,
            UiCanvasRenderMode mode,
            UiLayoutContext ctx,
            float screenScaleX,
            float screenScaleY,
            float x,
            float y,
            float width,
            float height,
            UILabelComponent label,
            UIButtonComponent button,
            UIToggleComponent toggle,
            UITextFieldComponent field
    ) {
        float w = width;
        float h = height;
        if (mode == UiCanvasRenderMode.SCREEN_SPACE && !ctx.authoringSpace) {
            w *= screenScaleX;
            h *= screenScaleY;
        }
        RectF rect = layoutRect(mode, ctx, x, y, w, h);
        items.add(new UiDrawItem(
                child,
                kind,
                canvasSortingOrder,
                treeOrder[0]++,
                rect,
                label,
                button,
                toggle,
                field
        ));
    }

    private static RectF layoutRect(UiCanvasRenderMode mode, UiLayoutContext ctx, float x, float y, float width, float height) {
        if (mode == UiCanvasRenderMode.SCREEN_SPACE || ctx.authoringSpace) {
            return new RectF(x, y, width, height);
        }
        return worldRectToScreen(new RectF(x, y, width, height), ctx.camera, ctx.viewportWidth, ctx.viewportHeight);
    }

    private static RectF worldRectToScreen(RectF world, Camera2d camera, int viewportWidth, int viewportHeight) {
        var viewport = new org.llw.render.core.IntSize(viewportWidth, viewportHeight);
        Vector2f topLeft = camera.worldToScreen(new Vector2f(world.left, world.top), viewport);
        Vector2f topRight = camera.worldToScreen(new Vector2f(world.right(), world.top), viewport);
        Vector2f bottomLeft = camera.worldToScreen(new Vector2f(world.left, world.bottom()), viewport);
        Vector2f bottomRight = camera.worldToScreen(new Vector2f(world.right(), world.bottom()), viewport);
        float minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        float maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        float minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        float maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
        return new RectF(minX, minY, Math.max(1f, maxX - minX), Math.max(1f, maxY - minY));
    }

    private record CanvasRoot(EntityId entity, UICanvasComponent canvas) {
    }
}
