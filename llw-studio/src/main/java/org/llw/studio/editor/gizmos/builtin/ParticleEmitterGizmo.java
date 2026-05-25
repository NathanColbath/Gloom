package org.llw.studio.editor.gizmos.builtin;

import org.llw.render.core.Color;
import org.llw.render.graphics.PrimitiveType;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.editor.gizmo.ComponentGizmoHit;
import org.llw.studio.editor.gizmo.ComponentSceneGizmo;
import org.llw.studio.editor.gizmo.GizmoContext;
import org.llw.studio.editor.gizmo.GizmoDrawContext;
import org.llw.studio.editor.gizmos.LightGizmoMath;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.editor.render.passes.GizmoDrawHelper;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;

/**
 * Scene gizmo for particle emitter shape (reads/writes the particle asset shape module).
 */
public final class ParticleEmitterGizmo implements ComponentSceneGizmo<ParticleEmitterComponent> {
    public static final String HANDLE_RADIUS = "radius";
    public static final String HANDLE_ARC = "arc";
    public static final String HANDLE_BOX_X = "boxX";
    public static final String HANDLE_BOX_Y = "boxY";

    private static final Color EMITTER_COLOR = new Color(180, 220, 255, 120);
    private static final Color EMITTER_SELECTED = new Color(180, 220, 255, 200);
    private static final Color HANDLE_COLOR = new Color(255, 255, 255, 230);
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @Override
    public Class<ParticleEmitterComponent> componentType() {
        return ParticleEmitterComponent.class;
    }

    @Override
    public void drawAll(GizmoDrawContext context) {
        Scene scene = context.scene();
        ComponentStore<ParticleEmitterComponent> emitters = scene.world().store(ParticleEmitterComponent.class);
        for (int i = 0; i < emitters.size(); i++) {
            EntityId entity = emitters.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            drawEmitter(context, entity, emitters.componentAt(i), false);
        }
    }

    @Override
    public void drawSelected(GizmoDrawContext context, EntityId entity, ParticleEmitterComponent emitter) {
        drawEmitter(context, entity, emitter, true);
    }

    private void drawEmitter(GizmoDrawContext context, EntityId entity, ParticleEmitterComponent emitter, boolean selected) {
        // Shape handles edit the particle asset document; emitter component only holds the GUID reference.
        ParticleSystemDocument doc = loadDocument(context.assets(), emitter.particleSystemGuid);
        if (doc == null) {
            return;
        }
        ParticleSystemDocument.ShapeModule shape = doc.modules.shape;
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(context.scene(), entity);
        Color color = selected ? EMITTER_SELECTED : EMITTER_COLOR;
        String type = shape.type == null ? "point" : shape.type.toLowerCase();
        switch (type) {
            case "circle" -> drawCircleShape(context, pose, shape, color, selected);
            case "box" -> drawBoxShape(context, pose, shape, color, selected);
            default -> drawPointCross(context, pose, color);
        }
    }

    private static void drawPointCross(GizmoDrawContext context, GizmoDrawHelper.WorldPose pose, Color color) {
        float s = context.lineWorld() * 3f;
        GizmoDrawHelper.drawLine(context, pose.x() - s, pose.y(), pose.x() + s, pose.y(), color);
        GizmoDrawHelper.drawLine(context, pose.x(), pose.y() - s, pose.x(), pose.y() + s, color);
    }

    private static void drawCircleShape(
            GizmoDrawContext context,
            GizmoDrawHelper.WorldPose pose,
            ParticleSystemDocument.ShapeModule shape,
            Color color,
            boolean selected
    ) {
        float rx = shape.radius * Math.abs(pose.scaleX());
        float ry = shape.radius * Math.abs(pose.scaleY());
        float arcRad = shape.arc * DEG_TO_RAD;
        if (shape.arc >= 360f - 0.01f) {
            var ring = LightGizmoMath.arcLineStrip(pose.x(), pose.y(), rx, ry, 0f, (float) (Math.PI * 2.0), 48, color);
            GizmoDrawHelper.drawVertices(context, ring, PrimitiveType.LINE_STRIP);
        } else {
            var vertices = LightGizmoMath.arcLineStrip(pose.x(), pose.y(), rx, ry, 0f, arcRad, 32, color);
            GizmoDrawHelper.drawVertices(context, vertices, PrimitiveType.LINE_STRIP);
            GizmoDrawHelper.drawLine(context, pose.x(), pose.y(), pose.x() + rx, pose.y(), color);
        }
        if (selected) {
            GizmoDrawHelper.drawHandleSquare(context, pose.x() + rx, pose.y(), HANDLE_COLOR);
            if (shape.arc < 360f - 0.01f) {
                float hx = pose.x() + (float) Math.cos(arcRad) * rx;
                float hy = pose.y() + (float) Math.sin(arcRad) * ry;
                GizmoDrawHelper.drawHandleSquare(context, hx, hy, HANDLE_COLOR);
            }
        }
    }

    private static void drawBoxShape(
            GizmoDrawContext context,
            GizmoDrawHelper.WorldPose pose,
            ParticleSystemDocument.ShapeModule shape,
            Color color,
            boolean selected
    ) {
        float hw = shape.width * 0.5f * Math.abs(pose.scaleX());
        float hh = shape.height * 0.5f * Math.abs(pose.scaleY());
        GizmoDrawHelper.drawBorder(context, pose.x() - hw, pose.y() - hh, hw * 2f, hh * 2f, color);
        if (selected) {
            GizmoDrawHelper.drawHandleSquare(context, pose.x() + hw, pose.y(), HANDLE_COLOR);
            GizmoDrawHelper.drawHandleSquare(context, pose.x(), pose.y() + hh, HANDLE_COLOR);
        }
    }

    @Override
    public ComponentGizmoHit hitTest(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            ParticleEmitterComponent emitter,
            float screenX,
            float screenY
    ) {
        ParticleSystemDocument doc = loadDocument(gizmoContext.assets(), emitter.particleSystemGuid);
        if (doc == null) {
            return ComponentGizmoHit.NONE;
        }
        ParticleSystemDocument.ShapeModule shape = doc.modules.shape;
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(scene, entity);
        String type = shape.type == null ? "point" : shape.type.toLowerCase();
        return switch (type) {
            case "circle" -> hitCircle(gizmoContext, pose, shape, entity, screenX, screenY);
            case "box" -> hitBox(gizmoContext, pose, shape, entity, screenX, screenY);
            default -> ComponentGizmoHit.NONE;
        };
    }

    private ComponentGizmoHit hitCircle(
            GizmoContext gizmoContext,
            GizmoDrawHelper.WorldPose pose,
            ParticleSystemDocument.ShapeModule shape,
            EntityId entity,
            float screenX,
            float screenY
    ) {
        float rx = shape.radius * Math.abs(pose.scaleX());
        if (gizmoContext.nearPoint(screenX, screenY, pose.x() + rx, pose.y())) {
            return new ComponentGizmoHit(ParticleEmitterComponent.class, entity, HANDLE_RADIUS);
        }
        if (shape.arc < 360f - 0.01f) {
            float arcRad = shape.arc * DEG_TO_RAD;
            float hx = pose.x() + (float) Math.cos(arcRad) * rx;
            float hy = pose.y() + (float) Math.sin(arcRad) * shape.radius * Math.abs(pose.scaleY());
            if (gizmoContext.nearPoint(screenX, screenY, hx, hy)) {
                return new ComponentGizmoHit(ParticleEmitterComponent.class, entity, HANDLE_ARC);
            }
        }
        return ComponentGizmoHit.NONE;
    }

    private ComponentGizmoHit hitBox(
            GizmoContext gizmoContext,
            GizmoDrawHelper.WorldPose pose,
            ParticleSystemDocument.ShapeModule shape,
            EntityId entity,
            float screenX,
            float screenY
    ) {
        float hw = shape.width * 0.5f * Math.abs(pose.scaleX());
        float hh = shape.height * 0.5f * Math.abs(pose.scaleY());
        if (gizmoContext.nearPoint(screenX, screenY, pose.x() + hw, pose.y())) {
            return new ComponentGizmoHit(ParticleEmitterComponent.class, entity, HANDLE_BOX_X);
        }
        if (gizmoContext.nearPoint(screenX, screenY, pose.x(), pose.y() + hh)) {
            return new ComponentGizmoHit(ParticleEmitterComponent.class, entity, HANDLE_BOX_Y);
        }
        return ComponentGizmoHit.NONE;
    }

    @Override
    public void updateDrag(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            ParticleEmitterComponent emitter,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
        ParticleSystemDocument doc = loadDocument(gizmoContext.assets(), emitter.particleSystemGuid);
        if (doc == null) {
            return;
        }
        ParticleSystemDocument.ShapeModule shape = doc.modules.shape;
        var world = gizmoContext.screenToWorld(screenX, screenY);
        GizmoDrawHelper.WorldPose pose = GizmoDrawHelper.worldPose(scene, entity);
        switch (hit.handleId()) {
            case HANDLE_RADIUS -> {
                float dx = world.x - pose.x();
                float dy = world.y - pose.y();
                float scale = Math.max(Math.abs(pose.scaleX()), Math.abs(pose.scaleY()));
                shape.radius = Math.max(0.01f, (float) Math.sqrt(dx * dx + dy * dy) / Math.max(scale, 1e-4f));
            }
            case HANDLE_ARC -> {
                float angle = (float) Math.toDegrees(Math.atan2(world.y - pose.y(), world.x - pose.x()));
                shape.arc = Math.max(1f, Math.min(360f, angle < 0f ? angle + 360f : angle));
            }
            case HANDLE_BOX_X -> {
                float scaleX = Math.max(Math.abs(pose.scaleX()), 1e-4f);
                shape.width = Math.max(0.01f, Math.abs(world.x - pose.x()) * 2f / scaleX);
            }
            case HANDLE_BOX_Y -> {
                float scaleY = Math.max(Math.abs(pose.scaleY()), 1e-4f);
                shape.height = Math.max(0.01f, Math.abs(world.y - pose.y()) * 2f / scaleY);
            }
            default -> {
            }
        }
    }

    /**
     * @param scene  edit scene
     * @param entity emitter entity
     * @return particle system GUID on the emitter
     */
    public static String particleGuidFor(Scene scene, EntityId entity) {
        ParticleEmitterComponent emitter = scene.world().getComponent(entity, ParticleEmitterComponent.class);
        return emitter == null ? null : emitter.particleSystemGuid;
    }

    /**
     * @param scene  edit scene
     * @param entity emitter entity
     * @param assets project assets
     * @return copy of the shape module after a drag, or null
     */
    public static ParticleSystemDocument.ShapeModule shapeSnapshot(Scene scene, EntityId entity, AssetDatabase assets) {
        ParticleEmitterComponent emitter = scene.world().getComponent(entity, ParticleEmitterComponent.class);
        if (emitter == null) {
            return null;
        }
        ParticleSystemDocument doc = loadDocument(assets, emitter.particleSystemGuid);
        return doc == null ? null : doc.modules.shape.copy();
    }

    private static ParticleSystemDocument loadDocument(AssetDatabase assets, String guid) {
        if (assets == null || guid == null || guid.isBlank()) {
            return null;
        }
        StudioAsset asset = assets.get(guid);
        if (asset == null) {
            return null;
        }
        return assets.loadParticleSystem(asset.path());
    }
}
