package org.llw.studio.editor.gizmo;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Circle;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.render.EntityBounds;
import org.llw.studio.scene.Scene;

/**
 * Rotate gizmo: circular ring around the entity bounds center.
 */
public final class RotateGizmo implements GizmoTool {
    private static final Color RING_COLOR = new Color(66, 150, 250, 255);
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.GIZMO);

    private float dragStartAngle;
    private float dragStartRotation;

    /** {@inheritDoc} */
    @Override
    public GizmoHit hitTest(GizmoContext context, Scene scene, EntityId entity, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        float radius = Math.max(bounds.width(), bounds.height()) * 0.5f + GizmoContext.AXIS_LENGTH_WORLD * 0.35f;
        var screen = context.worldToScreen(centerX, centerY);
        float screenRadius = radius * context.zoom();
        float dist = (float) Math.hypot(screenX - screen.x, screenY - screen.y);
        if (Math.abs(dist - screenRadius) <= GizmoContext.HIT_TOLERANCE_PX) {
            return GizmoHit.ROTATE;
        }
        return GizmoHit.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void beginDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        dragStartAngle = angle(screenX, screenY, context, centerX, centerY);
        dragStartRotation = TransformMath.worldRotation(scene.world(), entity);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDrag(GizmoContext context, Scene scene, EntityId entity, GizmoHit hit, float screenX, float screenY) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        float angle = angle(screenX, screenY, context, centerX, centerY);
        float deltaDegrees = (float) Math.toDegrees(angle - dragStartAngle);
        TransformMath.writeWorldRotation(scene.world(), entity, dragStartRotation + deltaDegrees);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(GizmoContext context, OffscreenTarget target, Scene scene, EntityId entity, GizmoHit hover) {
        EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, null);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        float radius = Math.max(bounds.width(), bounds.height()) * 0.5f + GizmoContext.AXIS_LENGTH_WORLD * 0.35f;
        Circle ring = new Circle();
        ring.setPosition(centerX, centerY);
        ring.setRadius(radius);
        ring.setFilled(false);
        ring.setOutlined(true);
        ring.setOutlineColor(hover == GizmoHit.ROTATE ? new Color(120, 190, 255, 255) : RING_COLOR);
        ring.setOutlineThickness(Math.max(2f, context.handleSizeWorld() * 0.25f));
        target.draw(ring, STATE);
    }

    private float angle(float screenX, float screenY, GizmoContext context, float centerX, float centerY) {
        var center = context.worldToScreen(centerX, centerY);
        return (float) Math.atan2(screenY - center.y, screenX - center.x);
    }
}
