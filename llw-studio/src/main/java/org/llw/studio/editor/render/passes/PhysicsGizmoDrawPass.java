package org.llw.studio.editor.render.passes;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.scene.Scene;

/**
 * Draws wireframe collider gizmos in the scene view.
 *
 * <p>Requires {@link org.llw.studio.editor.render.EditorWorldTransforms#ensureUpdated(Scene)}
 * before draw. Uses {@link EditorRenderLayers#COMPONENT_GIZMO} draw layer.
 */
public final class PhysicsGizmoDrawPass {
    private static final Color COLLIDER_COLOR = new Color(76, 175, 80, 180);
    private static final Color TRIGGER_COLOR = new Color(255, 193, 7, 180);
    private static final float LINE_PIXELS = 2f;
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.COMPONENT_GIZMO);

    private PhysicsGizmoDrawPass() {
    }

    public static void draw(Scene scene, OffscreenTarget target, EditorCamera editorCamera, int viewWidth, int viewHeight) {
        float line = EditorViewportMath.pixelsToWorld(editorCamera.zoom(), LINE_PIXELS);

        var boxes = scene.world().store(BoxCollider2DComponent.class);
        for (int i = 0; i < boxes.size(); i++) {
            EntityId entity = boxes.entityAt(i);
            BoxCollider2DComponent box = boxes.componentAt(i);
            float cx;
            float cy;
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            // Prefer baked world pose when transforms were synced; else fall back to local + offset.
            if (world != null) {
                cx = world.worldX + box.offsetX;
                cy = world.worldY + box.offsetY;
            } else if (local != null) {
                cx = local.x + box.offsetX;
                cy = local.y + box.offsetY;
            } else {
                continue;
            }
            float halfW = box.sizeX * 0.5f;
            float halfH = box.sizeY * 0.5f;
            drawBorder(target, cx - halfW, cy - halfH, box.sizeX, box.sizeY, line,
                    box.isTrigger ? TRIGGER_COLOR : COLLIDER_COLOR);
        }

        var circles = scene.world().store(CircleCollider2DComponent.class);
        for (int i = 0; i < circles.size(); i++) {
            EntityId entity = circles.entityAt(i);
            CircleCollider2DComponent circle = circles.componentAt(i);
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            float cx = world != null ? world.worldX + circle.offsetX : local != null ? local.x + circle.offsetX : 0f;
            float cy = world != null ? world.worldY + circle.offsetY : local != null ? local.y + circle.offsetY : 0f;
            float size = circle.radius * 2f;
            drawBorder(target, cx - circle.radius, cy - circle.radius, size, size, line,
                    circle.isTrigger ? TRIGGER_COLOR : COLLIDER_COLOR);
        }

        var edges = scene.world().store(EdgeCollider2DComponent.class);
        for (int i = 0; i < edges.size(); i++) {
            EntityId entity = edges.entityAt(i);
            EdgeCollider2DComponent edge = edges.componentAt(i);
            if (edge.points == null || edge.points.length < 4) {
                continue;
            }
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            float ox = world != null ? world.worldX : local != null ? local.x : 0f;
            float oy = world != null ? world.worldY : local != null ? local.y : 0f;
            Color color = edge.isTrigger ? TRIGGER_COLOR : COLLIDER_COLOR;
            for (int p = 0; p + 3 < edge.points.length; p += 2) {
                float x1 = ox + edge.points[p];
                float y1 = oy + edge.points[p + 1];
                float x2 = ox + edge.points[p + 2];
                float y2 = oy + edge.points[p + 3];
                drawLineBar(target, x1, y1, x2, y2, line, color);
            }
        }
    }

    private static void drawLineBar(OffscreenTarget target, float x1, float y1, float x2, float y2, float line, Color color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return;
        }
        float nx = -dy / len * line * 0.5f;
        float ny = dx / len * line * 0.5f;
        drawBar(target, x1 + nx, y1 + ny, len, line, color);
    }

    private static void drawBorder(OffscreenTarget target, float x, float y, float width, float height, float line, Color color) {
        drawBar(target, x, y, width, line, color);
        drawBar(target, x, y + height - line, width, line, color);
        drawBar(target, x, y, line, height, color);
        drawBar(target, x + width - line, y, line, height, color);
    }

    private static void drawBar(OffscreenTarget target, float x, float y, float width, float height, Color color) {
        Rectangle bar = new Rectangle();
        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setFillColor(color);
        target.draw(bar, STATE);
    }
}

