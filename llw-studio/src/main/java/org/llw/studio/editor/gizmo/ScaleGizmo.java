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
 * Scale gizmo: per-axis and uniform handles at the entity pivot.
 */
public final class ScaleGizmo implements GizmoTool {
    private static final Color X_COLOR = new Color(230, 70, 70, 255);
    private static final Color Y_COLOR = new Color(80, 200, 90, 255);
    private static final Color UNIFORM_COLOR = new Color(220, 220, 120, 255);
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.GIZMO);

    private float dragStartWorldX;
    private float dragStartWorldY;
    private float dragStartScaleX;
    private float dragStartScaleY;

    /** {@inheritDoc} */
    @Override
    public GizmoHit hitTest(GizmoContext context, Scene scene, EntityId entity, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float pivotX = bounds.pivotX();
        float pivotY = bounds.pivotY();
        float axis = GizmoContext.AXIS_LENGTH_WORLD;
        float handle = context.handleSizeWorld();
        if (context.nearPoint(screenX, screenY, pivotX + axis, pivotY)) {
            return GizmoHit.SCALE_X;
        }
        if (context.nearPoint(screenX, screenY, pivotX, pivotY + axis)) {
            return GizmoHit.SCALE_Y;
        }
        if (context.nearPoint(screenX, screenY, pivotX + axis, pivotY + axis)) {
            return GizmoHit.SCALE_UNIFORM;
        }
        return GizmoHit.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void beginDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        var world = context.screenToWorld(screenX, screenY);
        dragStartWorldX = world.x;
        dragStartWorldY = world.y;
        dragStartScaleX = TransformMath.worldScaleX(scene.world(), entity);
        dragStartScaleY = TransformMath.worldScaleY(scene.world(), entity);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float pivotX = bounds.pivotX();
        float pivotY = bounds.pivotY();
        var world = context.screenToWorld(screenX, screenY);
        float deltaX = world.x - dragStartWorldX;
        float deltaY = world.y - dragStartWorldY;
        float scaleX = dragStartScaleX;
        float scaleY = dragStartScaleY;
        if (hit == GizmoHit.SCALE_X || hit == GizmoHit.SCALE_UNIFORM) {
            scaleX = Math.max(0.01f, dragStartScaleX + deltaX / GizmoContext.AXIS_LENGTH_WORLD);
        }
        if (hit == GizmoHit.SCALE_Y || hit == GizmoHit.SCALE_UNIFORM) {
            scaleY = Math.max(0.01f, dragStartScaleY + deltaY / GizmoContext.AXIS_LENGTH_WORLD);
        }
        if (hit == GizmoHit.SCALE_UNIFORM) {
            float uniform = Math.max(scaleX, scaleY);
            scaleX = uniform;
            scaleY = uniform;
        }
        TransformMath.writeWorldScale(scene.world(), entity, scaleX, scaleY);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(GizmoContext context, OffscreenTarget target, Scene scene, EntityId entity, GizmoHit hover) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float pivotX = bounds.pivotX();
        float pivotY = bounds.pivotY();
        float axis = GizmoContext.AXIS_LENGTH_WORLD;
        float handle = context.handleSizeWorld();
        drawHandle(target, pivotX + axis - handle * 0.5f, pivotY - handle * 0.5f, handle, hover == GizmoHit.SCALE_X ? brighten(X_COLOR) : X_COLOR);
        drawHandle(target, pivotX - handle * 0.5f, pivotY + axis - handle * 0.5f, handle, hover == GizmoHit.SCALE_Y ? brighten(Y_COLOR) : Y_COLOR);
        drawHandle(target, pivotX + axis - handle * 0.5f, pivotY + axis - handle * 0.5f, handle,
                hover == GizmoHit.SCALE_UNIFORM ? brighten(UNIFORM_COLOR) : UNIFORM_COLOR);
        float thickness = Math.max(2f, context.handleSizeWorld() * 0.25f);
        Rectangle xAxis = new Rectangle();
        xAxis.setPosition(pivotX, pivotY - thickness * 0.5f);
        xAxis.setSize(axis, thickness);
        xAxis.setFillColor(X_COLOR);
        target.draw(xAxis, STATE);
        Rectangle yAxis = new Rectangle();
        yAxis.setPosition(pivotX - thickness * 0.5f, pivotY);
        yAxis.setSize(thickness, axis);
        yAxis.setFillColor(Y_COLOR);
        target.draw(yAxis, STATE);
    }

    private void drawHandle(OffscreenTarget target, float x, float y, float size, Color color) {
        Rectangle handle = new Rectangle();
        handle.setPosition(x, y);
        handle.setSize(size, size);
        handle.setFillColor(color);
        target.draw(handle, STATE);
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
