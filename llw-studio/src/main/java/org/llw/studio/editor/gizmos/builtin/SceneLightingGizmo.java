package org.llw.studio.editor.gizmos.builtin;

import org.llw.render.core.Color;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.editor.gizmo.ComponentGizmoHit;
import org.llw.studio.editor.gizmo.ComponentSceneGizmo;
import org.llw.studio.editor.gizmo.GizmoContext;
import org.llw.studio.editor.gizmo.GizmoDrawContext;
import org.llw.studio.editor.render.passes.GizmoDrawHelper;
import org.llw.studio.scene.Scene;

/**
 * Scene gizmo for baked lightmap bounds on {@link SceneLightingComponent}.
 */
public final class SceneLightingGizmo implements ComponentSceneGizmo<SceneLightingComponent> {
    public static final String HANDLE_MIN_X = "minX";
    public static final String HANDLE_MIN_Y = "minY";
    public static final String HANDLE_MAX_X = "maxX";
    public static final String HANDLE_MAX_Y = "maxY";

    private static final Color BOUNDS_COLOR = new Color(255, 235, 120, 140);
    private static final Color BOUNDS_SELECTED = new Color(255, 235, 120, 220);
    private static final Color HANDLE_COLOR = new Color(255, 255, 200, 240);

    @Override
    public Class<SceneLightingComponent> componentType() {
        return SceneLightingComponent.class;
    }

    @Override
    public void drawAll(GizmoDrawContext context) {
        Scene scene = context.scene();
        ComponentStore<SceneLightingComponent> store = scene.world().store(SceneLightingComponent.class);
        for (int i = 0; i < store.size(); i++) {
            EntityId entity = store.entityAt(i);
            SceneLightingComponent lighting = store.componentAt(i);
            if (lighting.lightmapEnabled && hasValidBounds(lighting)) {
                boolean selected = context.selection().isSelected(entity);
                drawBounds(context, lighting, selected ? BOUNDS_SELECTED : BOUNDS_COLOR);
            }
        }
    }

    @Override
    public void drawSelected(GizmoDrawContext context, EntityId entity, SceneLightingComponent lighting) {
        if (!lighting.lightmapEnabled || !hasValidBounds(lighting)) {
            return;
        }
        drawBounds(context, lighting, BOUNDS_SELECTED);
        // Eight handles resize the baked lightmap AABB in world space (see LightBakeService).
        float minX = lighting.lightmapMinX;
        float minY = lighting.lightmapMinY;
        float maxX = lighting.lightmapMaxX;
        float maxY = lighting.lightmapMaxY;
        float midX = (minX + maxX) * 0.5f;
        float midY = (minY + maxY) * 0.5f;
        GizmoDrawHelper.drawHandleSquare(context, minX, minY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, maxX, minY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, maxX, maxY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, minX, maxY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, midX, minY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, maxX, midY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, midX, maxY, HANDLE_COLOR);
        GizmoDrawHelper.drawHandleSquare(context, minX, midY, HANDLE_COLOR);
    }

    private static void drawBounds(GizmoDrawContext context, SceneLightingComponent lighting, Color color) {
        float w = lighting.lightmapMaxX - lighting.lightmapMinX;
        float h = lighting.lightmapMaxY - lighting.lightmapMinY;
        GizmoDrawHelper.drawBorder(context, lighting.lightmapMinX, lighting.lightmapMinY, w, h, color);
    }

    private static boolean hasValidBounds(SceneLightingComponent lighting) {
        return lighting.lightmapMaxX > lighting.lightmapMinX && lighting.lightmapMaxY > lighting.lightmapMinY;
    }

    @Override
    public ComponentGizmoHit hitTest(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            SceneLightingComponent lighting,
            float screenX,
            float screenY
    ) {
        if (!lighting.lightmapEnabled || !hasValidBounds(lighting)) {
            return ComponentGizmoHit.NONE;
        }
        float minX = lighting.lightmapMinX;
        float minY = lighting.lightmapMinY;
        float maxX = lighting.lightmapMaxX;
        float maxY = lighting.lightmapMaxY;
        float midX = (minX + maxX) * 0.5f;
        float midY = (minY + maxY) * 0.5f;
        if (hit(gizmoContext, minX, minY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MIN_X);
        }
        if (hit(gizmoContext, maxX, minY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MAX_X);
        }
        if (hit(gizmoContext, maxX, maxY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MAX_Y);
        }
        if (hit(gizmoContext, minX, maxY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MIN_Y);
        }
        if (hit(gizmoContext, midX, minY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MIN_Y);
        }
        if (hit(gizmoContext, maxX, midY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MAX_X);
        }
        if (hit(gizmoContext, midX, maxY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MAX_Y);
        }
        if (hit(gizmoContext, minX, midY, screenX, screenY)) {
            return new ComponentGizmoHit(SceneLightingComponent.class, entity, HANDLE_MIN_X);
        }
        return ComponentGizmoHit.NONE;
    }

    private static boolean hit(GizmoContext gizmoContext, float wx, float wy, float screenX, float screenY) {
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
            SceneLightingComponent lighting,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
        var world = gizmoContext.screenToWorld(screenX, screenY);
        switch (hit.handleId()) {
            case HANDLE_MIN_X -> lighting.lightmapMinX = Math.min(world.x, lighting.lightmapMaxX - 1f);
            case HANDLE_MAX_X -> lighting.lightmapMaxX = Math.max(world.x, lighting.lightmapMinX + 1f);
            case HANDLE_MIN_Y -> lighting.lightmapMinY = Math.min(world.y, lighting.lightmapMaxY - 1f);
            case HANDLE_MAX_Y -> lighting.lightmapMaxY = Math.max(world.y, lighting.lightmapMinY + 1f);
            default -> {
            }
        }
    }

    @Override
    public SceneLightingComponent dragStartSnapshot(SceneLightingComponent component) {
        return component.copy();
    }

    @Override
    public SceneLightingComponent dragEndSnapshot(SceneLightingComponent component) {
        return component.copy();
    }
}
