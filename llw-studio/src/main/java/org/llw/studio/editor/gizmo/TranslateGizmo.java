package org.llw.studio.editor.gizmo;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.render.EntityBounds;
import org.llw.studio.scene.Scene;

/**
 * Move gizmo: X/Y axes and XY plane handle at the entity pivot.
 */
public final class TranslateGizmo implements GizmoTool {
    private static final Color X_COLOR = new Color(230, 70, 70, 255);
    private static final Color Y_COLOR = new Color(80, 200, 90, 255);
    private static final Color XY_COLOR = new Color(220, 220, 220, 200);
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.GIZMO);

    private float dragStartWorldX;
    private float dragStartWorldY;
    private float dragStartPivotX;
    private float dragStartPivotY;
    private float dragScreenStartX;
    private float dragScreenStartY;

    /** {@inheritDoc} */
    @Override
    public GizmoHit hitTest(GizmoContext context, Scene scene, EntityId entity, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float pivotX = bounds.pivotX();
        float pivotY = bounds.pivotY();
        float axis = GizmoContext.AXIS_LENGTH_WORLD;
        if (context.nearPoint(screenX, screenY, pivotX, pivotY)) {
            return GizmoHit.XY_PLANE;
        }
        if (context.nearSegment(screenX, screenY, pivotX, pivotY, pivotX + axis, pivotY)) {
            return GizmoHit.X_AXIS;
        }
        if (context.nearSegment(screenX, screenY, pivotX, pivotY, pivotX, pivotY + axis)) {
            return GizmoHit.Y_AXIS;
        }
        return GizmoHit.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void beginDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        dragStartPivotX = bounds.pivotX();
        dragStartPivotY = bounds.pivotY();
        var world = context.screenToWorld(screenX, screenY);
        dragStartWorldX = world.x;
        dragStartWorldY = world.y;
        dragScreenStartX = screenX;
        dragScreenStartY = screenY;
    }

    /** {@inheritDoc} */
    @Override
    public void updateDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        var world = context.screenToWorld(screenX, screenY);
        float deltaX = world.x - dragStartWorldX;
        float deltaY = world.y - dragStartWorldY;
        float targetX = dragStartPivotX;
        float targetY = dragStartPivotY;
        if (hit == GizmoHit.X_AXIS || hit == GizmoHit.XY_PLANE) {
            targetX += deltaX;
        }
        if (hit == GizmoHit.Y_AXIS || hit == GizmoHit.XY_PLANE) {
            targetY += deltaY;
        }
        TransformMath.writeWorldTranslation(scene.world(), entity, targetX, targetY);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(GizmoContext context, OffscreenTarget target, Scene scene, EntityId entity, GizmoHit hover) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float pivotX = bounds.pivotX();
        float pivotY = bounds.pivotY();
        float axis = GizmoContext.AXIS_LENGTH_WORLD;
        float thickness = Math.max(2f, context.handleSizeWorld() * 0.35f);
        drawAxis(target, pivotX, pivotY, axis, 0f, thickness, hover == GizmoHit.X_AXIS ? brighten(X_COLOR) : X_COLOR);
        drawAxis(target, pivotX, pivotY, 0f, axis, thickness, hover == GizmoHit.Y_AXIS ? brighten(Y_COLOR) : Y_COLOR);
        float handle = context.handleSizeWorld();
        Rectangle center = new Rectangle();
        center.setPosition(pivotX - handle * 0.5f, pivotY - handle * 0.5f);
        center.setSize(handle, handle);
        center.setFillColor(hover == GizmoHit.XY_PLANE ? brighten(XY_COLOR) : XY_COLOR);
        target.draw(center, STATE);
    }

    private void drawAxis(OffscreenTarget target, float x, float y, float dx, float dy, float thickness, Color color) {
        Rectangle axis = new Rectangle();
        if (Math.abs(dx) >= Math.abs(dy)) {
            axis.setPosition(Math.min(x, x + dx), y - thickness * 0.5f);
            axis.setSize(Math.abs(dx), thickness);
        } else {
            axis.setPosition(x - thickness * 0.5f, Math.min(y, y + dy));
            axis.setSize(thickness, Math.abs(dy));
        }
        axis.setFillColor(color);
        target.draw(axis, STATE);
    }

    private static Color brighten(Color color) {
        return new Color(
                Math.min(255, color.r + 40),
                Math.min(255, color.g + 40),
                Math.min(255, color.b + 40),
                color.a
        );
    }
}
