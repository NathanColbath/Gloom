package org.llw.studio.editor.gizmos.builtin;

import org.llw.render.core.Color;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Vertex;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.editor.gizmo.ComponentGizmoHit;
import org.llw.studio.editor.gizmo.ComponentSceneGizmo;
import org.llw.studio.editor.gizmo.GizmoContext;
import org.llw.studio.editor.gizmo.GizmoDrawContext;
import org.llw.studio.editor.gizmos.LightGizmoMath;
import org.llw.studio.editor.render.passes.GizmoDrawHelper;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;

/**
 * Scene gizmo for {@link Light2DComponent}.
 */
public final class Light2DGizmo implements ComponentSceneGizmo<Light2DComponent> {
    public static final String HANDLE_RANGE = "range";
    public static final String HANDLE_INNER_ANGLE = "innerAngle";
    public static final String HANDLE_OUTER_ANGLE = "outerAngle";
    public static final String HANDLE_DIRECTION = "direction";

    private static final Color HANDLE_COLOR = new Color(255, 255, 255, 230);
    private static final float DIRECTION_LENGTH = 80f;
    private static final int CONE_SEGMENTS = 24;

    @Override
    public Class<Light2DComponent> componentType() {
        return Light2DComponent.class;
    }

    @Override
    public void drawAll(GizmoDrawContext context) {
        Scene scene = context.scene();
        ComponentStore<Light2DComponent> lights = scene.world().store(Light2DComponent.class);
        for (int i = 0; i < lights.size(); i++) {
            EntityId entity = lights.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            drawLight(context, entity, lights.componentAt(i), false);
        }
    }

    @Override
    public void drawSelected(GizmoDrawContext context, EntityId entity, Light2DComponent light) {
        drawLight(context, entity, light, true);
    }

    private void drawLight(GizmoDrawContext context, EntityId entity, Light2DComponent light, boolean selected) {
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(context.scene(), entity);
        Color tint = GizmoDrawHelper.lightTint(light.r, light.g, light.b, light.intensity, selected ? 200 : 100);
        String type = light.type == null ? "POINT" : light.type.toUpperCase();
        switch (type) {
            case "GLOBAL" -> drawGlobalIcon(context, pose, tint);
            case "DIRECTIONAL" -> drawDirectional(context, pose, tint);
            case "SPOT" -> drawSpot(context, pose, light, tint);
            default -> GizmoDrawHelper.drawWireCircle(context, pose.x(), pose.y(), light.range, tint);
        }
        // Handles only on selection so unselected lights stay readable in dense scenes.
        if (selected) {
            drawHandles(context, pose, light, type);
        }
    }

    private static void drawGlobalIcon(GizmoDrawContext context, GizmoDrawHelper.WorldPose pose, Color tint) {
        float s = context.lineWorld() * 4f;
        GizmoDrawHelper.drawLine(context, pose.x() - s, pose.y(), pose.x() + s, pose.y(), tint);
        GizmoDrawHelper.drawLine(context, pose.x(), pose.y() - s, pose.x(), pose.y() + s, tint);
    }

    private static void drawDirectional(GizmoDrawContext context, GizmoDrawHelper.WorldPose pose, Color tint) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        float tipX = pose.x() + dir[0] * DIRECTION_LENGTH;
        float tipY = pose.y() + dir[1] * DIRECTION_LENGTH;
        GizmoDrawHelper.drawLine(context, pose.x(), pose.y(), tipX, tipY, tint);
    }

    private static void drawSpot(GizmoDrawContext context, GizmoDrawHelper.WorldPose pose, Light2DComponent light, Color tint) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        Vertex[] outer = LightGizmoMath.coneOutline(
                pose.x(), pose.y(), dir[0], dir[1], light.range, light.outerAngle * 0.5f, CONE_SEGMENTS, tint);
        GizmoDrawHelper.drawVertices(context, outer, PrimitiveType.TRIANGLE_FAN);
        Color innerTint = new Color(tint.r, tint.g, tint.b, Math.min(255, tint.a + 40));
        Vertex[] inner = LightGizmoMath.coneOutline(
                pose.x(), pose.y(), dir[0], dir[1], light.range * 0.85f, light.innerAngle * 0.5f, CONE_SEGMENTS, innerTint);
        GizmoDrawHelper.drawVertices(context, inner, PrimitiveType.LINE_STRIP);
    }

    private void drawHandles(GizmoDrawContext context, GizmoDrawHelper.WorldPose pose, Light2DComponent light, String type) {
        switch (type) {
            case "POINT" -> {
                float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
                GizmoDrawHelper.drawHandleSquare(context, pose.x() + dir[0] * light.range, pose.y() + dir[1] * light.range, HANDLE_COLOR);
            }
            case "SPOT" -> {
                float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
                float baseAngle = (float) Math.atan2(dir[1], dir[0]);
                float outerRad = (float) Math.toRadians(light.outerAngle * 0.5f);
                float innerRad = (float) Math.toRadians(light.innerAngle * 0.5f);
                float range = light.range;
                GizmoDrawHelper.drawHandleSquare(context, pose.x() + dir[0] * range, pose.y() + dir[1] * range, HANDLE_COLOR);
                GizmoDrawHelper.drawHandleSquare(
                        context,
                        pose.x() + (float) Math.cos(baseAngle + outerRad) * range,
                        pose.y() + (float) Math.sin(baseAngle + outerRad) * range,
                        HANDLE_COLOR
                );
                GizmoDrawHelper.drawHandleSquare(
                        context,
                        pose.x() + (float) Math.cos(baseAngle - innerRad) * range * 0.85f,
                        pose.y() + (float) Math.sin(baseAngle - innerRad) * range * 0.85f,
                        HANDLE_COLOR
                );
            }
            case "DIRECTIONAL" -> {
                float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
                GizmoDrawHelper.drawHandleSquare(
                        context,
                        pose.x() + dir[0] * DIRECTION_LENGTH,
                        pose.y() + dir[1] * DIRECTION_LENGTH,
                        HANDLE_COLOR
                );
            }
            default -> {
            }
        }
    }

    @Override
    public ComponentGizmoHit hitTest(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            Light2DComponent light,
            float screenX,
            float screenY
    ) {
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(scene, entity);
        String type = light.type == null ? "POINT" : light.type.toUpperCase();
        String handle = switch (type) {
            case "POINT" -> hitRangeHandle(gizmoContext, pose, light, screenX, screenY);
            case "SPOT" -> hitSpotHandle(gizmoContext, pose, light, screenX, screenY);
            case "DIRECTIONAL" -> hitDirectionHandle(gizmoContext, pose, screenX, screenY);
            default -> null;
        };
        if (handle == null) {
            return ComponentGizmoHit.NONE;
        }
        return new ComponentGizmoHit(Light2DComponent.class, entity, handle);
    }

    private static String hitRangeHandle(
            GizmoContext gizmoContext,
            GizmoDrawHelper.WorldPose pose,
            Light2DComponent light,
            float screenX,
            float screenY
    ) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        return hitWorld(gizmoContext, pose.x() + dir[0] * light.range, pose.y() + dir[1] * light.range, screenX, screenY)
                ? HANDLE_RANGE
                : null;
    }

    private static String hitSpotHandle(
            GizmoContext gizmoContext,
            GizmoDrawHelper.WorldPose pose,
            Light2DComponent light,
            float screenX,
            float screenY
    ) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        float baseAngle = (float) Math.atan2(dir[1], dir[0]);
        float outerRad = (float) Math.toRadians(light.outerAngle * 0.5f);
        float innerRad = (float) Math.toRadians(light.innerAngle * 0.5f);
        float range = light.range;
        if (hitWorld(gizmoContext, pose.x() + dir[0] * range, pose.y() + dir[1] * range, screenX, screenY)) {
            return HANDLE_RANGE;
        }
        if (hitWorld(
                gizmoContext,
                pose.x() + (float) Math.cos(baseAngle + outerRad) * range,
                pose.y() + (float) Math.sin(baseAngle + outerRad) * range,
                screenX,
                screenY
        )) {
            return HANDLE_OUTER_ANGLE;
        }
        if (hitWorld(
                gizmoContext,
                pose.x() + (float) Math.cos(baseAngle - innerRad) * range * 0.85f,
                pose.y() + (float) Math.sin(baseAngle - innerRad) * range * 0.85f,
                screenX,
                screenY
        )) {
            return HANDLE_INNER_ANGLE;
        }
        return null;
    }

    private static String hitDirectionHandle(
            GizmoContext gizmoContext,
            GizmoDrawHelper.WorldPose pose,
            float screenX,
            float screenY
    ) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        return hitWorld(
                gizmoContext,
                pose.x() + dir[0] * DIRECTION_LENGTH,
                pose.y() + dir[1] * DIRECTION_LENGTH,
                screenX,
                screenY
        ) ? HANDLE_DIRECTION : null;
    }

    private static boolean hitWorld(GizmoContext gizmoContext, float wx, float wy, float screenX, float screenY) {
        var screen = gizmoContext.worldToScreen(wx, wy);
        float dx = screen.x - screenX;
        float dy = screen.y - screenY;
        float tolerance = GizmoContext.HIT_TOLERANCE_PX;
        return dx * dx + dy * dy <= tolerance * tolerance;
    }

    @Override
    public void updateDrag(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            Light2DComponent light,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
        var world = gizmoContext.screenToWorld(screenX, screenY);
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(scene, entity);
        switch (hit.handleId()) {
            case HANDLE_RANGE -> {
                float dx = world.x - pose.x();
                float dy = world.y - pose.y();
                light.range = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
            }
            case HANDLE_OUTER_ANGLE -> updateSpotAngle(pose, light, world.x, world.y, false);
            case HANDLE_INNER_ANGLE -> updateSpotAngle(pose, light, world.x, world.y, true);
            case HANDLE_DIRECTION -> {
                Transform2DComponent transform = scene.world().getComponent(entity, Transform2DComponent.class);
                if (transform != null) {
                    float angle = (float) Math.toDegrees(Math.atan2(world.y - pose.y(), world.x - pose.x()));
                    transform.rotation = angle + 90f;
                }
            }
            default -> {
            }
        }
    }

    private static void updateSpotAngle(
            GizmoDrawHelper.WorldPose pose,
            Light2DComponent light,
            float wx,
            float wy,
            boolean inner
    ) {
        float[] dir = LightGizmoMath.directionFromRotation(pose.rotation());
        float base = (float) Math.atan2(dir[1], dir[0]);
        float cursor = (float) Math.atan2(wy - pose.y(), wx - pose.x());
        float delta = (float) Math.abs(Math.toDegrees(cursor - base));
        if (inner) {
            light.innerAngle = Math.max(1f, Math.min(light.outerAngle - 1f, delta * 2f));
        } else {
            light.outerAngle = Math.max(light.innerAngle + 1f, Math.min(179f, delta * 2f));
        }
    }

    @Override
    public Light2DComponent dragStartSnapshot(Light2DComponent component) {
        return component.copy();
    }

    @Override
    public Light2DComponent dragEndSnapshot(Light2DComponent component) {
        return component.copy();
    }
}
