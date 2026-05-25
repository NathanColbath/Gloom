package org.llw.studio.editor.render.passes;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Vertex;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.VertexGeometry;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.editor.gizmo.GizmoDrawContext;
import org.llw.studio.scene.Scene;

/**
 * Shared drawing helpers for editor component gizmos.
 */
public final class GizmoDrawHelper {
    private static final float HANDLE_PIXELS = 8f;

    private GizmoDrawHelper() {
    }

    /** World-space pose for gizmo drawing. */
    public record WorldPose(float x, float y, float rotation, float scaleX, float scaleY) {
    }

    /**
     * @param scene  scene containing the entity
     * @param entity target entity
     * @return world position, rotation, and scale
     */
    public static WorldPose worldPose(Scene scene, EntityId entity) {
        WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
        Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
        if (world != null) {
            return new WorldPose(world.worldX, world.worldY, world.worldRotation, world.worldScaleX, world.worldScaleY);
        }
        if (local != null) {
            return new WorldPose(local.x, local.y, local.rotation, local.scaleX, local.scaleY);
        }
        return new WorldPose(0f, 0f, 0f, 1f, 1f);
    }

    /**
     * @param r           red 0–1
     * @param g           green 0–1
     * @param b           blue 0–1
     * @param intensity   light intensity multiplier
     * @param alpha       overlay alpha 0–255
     * @return tint color for light gizmos
     */
    public static Color lightTint(float r, float g, float b, float intensity, int alpha) {
        float scale = Math.max(0f, intensity);
        int ri = Math.min(255, Math.round(r * scale * 255f));
        int gi = Math.min(255, Math.round(g * scale * 255f));
        int bi = Math.min(255, Math.round(b * scale * 255f));
        return new Color(ri, gi, bi, alpha);
    }

    public static void drawWireCircle(
            GizmoDrawContext context,
            float centerX,
            float centerY,
            float radius,
            Color color
    ) {
        Circle circle = new Circle();
        circle.setPosition(centerX, centerY);
        circle.setRadius(Math.max(1f, radius));
        circle.setFilled(false);
        circle.setOutlined(true);
        circle.setOutlineColor(color);
        circle.setOutlineThickness(Math.max(1f, context.lineWorld()));
        context.target().draw(circle, context.drawState());
    }

    public static void drawWireEllipse(
            GizmoDrawContext context,
            float centerX,
            float centerY,
            float radiusX,
            float radiusY,
            Color color
    ) {
        Matrix3x2 transform = new Matrix3x2();
        transform.translate(centerX, centerY);
        transform.scale(radiusX / Math.max(radiusX, radiusY), radiusY / Math.max(radiusX, radiusY));
        Circle circle = new Circle();
        circle.setRadius(Math.max(radiusX, radiusY));
        circle.setFilled(false);
        circle.setOutlined(true);
        circle.setOutlineColor(color);
        circle.setOutlineThickness(Math.max(1f, context.lineWorld()));
        DrawState state = context.drawState().withTransform(transform);
        context.target().draw(circle, state);
    }

    public static void drawVertices(GizmoDrawContext context, Vertex[] vertices, PrimitiveType type) {
        if (vertices == null || vertices.length == 0) {
            return;
        }
        VertexGeometry geometry = new VertexGeometry();
        geometry.setVertices(vertices);
        geometry.setPrimitiveType(type);
        context.target().draw(geometry, context.drawState());
    }

    public static void drawBorder(
            GizmoDrawContext context,
            float x,
            float y,
            float width,
            float height,
            Color color
    ) {
        float line = context.lineWorld();
        drawBar(context, x, y, width, line, color);
        drawBar(context, x, y + height - line, width, line, color);
        drawBar(context, x, y, line, height, color);
        drawBar(context, x + width - line, y, line, height, color);
    }

    public static void drawLine(
            GizmoDrawContext context,
            float x1,
            float y1,
            float x2,
            float y2,
            Color color
    ) {
        float line = context.lineWorld();
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return;
        }
        float nx = -dy / len * line * 0.5f;
        float ny = dx / len * line * 0.5f;
        drawBar(context, x1 + nx, y1 + ny, len, line, color);
    }

    public static void drawHandleSquare(GizmoDrawContext context, float worldX, float worldY, Color color) {
        float half = context.lineWorld() * (HANDLE_PIXELS / 2f);
        drawBar(context, worldX - half, worldY - half, half * 2f, half * 2f, color);
    }

    private static void drawBar(GizmoDrawContext context, float x, float y, float width, float height, Color color) {
        Rectangle bar = new Rectangle();
        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setFillColor(color);
        context.target().draw(bar, context.drawState());
    }

    /**
     * @param context   draw context
     * @param gizmo     gizmo context for screen projection
     * @param worldX    handle world X
     * @param worldY    handle world Y
     * @param screenX   cursor X in viewport pixels
     * @param screenY   cursor Y in viewport pixels
     * @return whether the cursor is within handle tolerance
     */
    public static boolean hitHandle(
            GizmoDrawContext context,
            org.llw.studio.editor.gizmo.GizmoContext gizmo,
            float worldX,
            float worldY,
            float screenX,
            float screenY
    ) {
        var screen = gizmo.worldToScreen(worldX, worldY);
        float tolerance = org.llw.studio.editor.gizmo.GizmoContext.HIT_TOLERANCE_PX;
        float dx = screen.x - screenX;
        float dy = screen.y - screenY;
        return dx * dx + dy * dy <= tolerance * tolerance;
    }
}

