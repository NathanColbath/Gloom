package org.llw.studio.editor.gizmo;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.commands.ComponentFieldEditCommand;
import org.llw.studio.editor.commands.ParticleShapeEditCommand;
import org.llw.studio.editor.commands.TransformEditCommand;
import org.llw.studio.editor.commands.TransformSnapshot;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.gizmos.builtin.Light2DGizmo;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.editor.gizmos.builtin.ParticleEmitterGizmo;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.scene.Scene;

/**
 * Routes pointer input to component gizmo handles before the transform gizmo.
 */
public final class GizmoHandleController {
    private final ComponentGizmoRegistry registry;
    private final SelectionService selection;
    private final AssetDatabase assets;

    private ComponentGizmoHit hoverHit = ComponentGizmoHit.NONE;
    private ComponentGizmoHit activeHit = ComponentGizmoHit.NONE;
    private Object dragStartSnapshot;
    private String particleDragGuid;
    private ParticleSystemDocument.ShapeModule particleDragStartShape;
    private TransformSnapshot transformDragStart;
    private boolean dragging;

    /**
     * @param registry  component gizmo registry
     * @param selection entity selection
     */
    public GizmoHandleController(ComponentGizmoRegistry registry, SelectionService selection, AssetDatabase assets) {
        this.registry = registry;
        this.selection = selection;
        this.assets = assets;
    }

    /** @return handle under the cursor from the last hover update */
    public ComponentGizmoHit hoverHit() {
        return hoverHit;
    }

    /** @return whether a component handle drag is active */
    public boolean isDragging() {
        return dragging;
    }

    public void updateHover(Scene scene, GizmoContext context, float screenX, float screenY) {
        if (dragging) {
            return;
        }
        hoverHit = ComponentGizmoHit.NONE;
        for (var entity : selection.allSelected()) {
            if (!scene.world().isAlive(entity)) {
                continue;
            }
            ComponentGizmoHit hit = registry.hitTest(context, scene, entity, screenX, screenY);
            if (hit.isHit()) {
                hoverHit = hit;
                return;
            }
        }
    }

    /**
     * @return true if a component handle drag started
     */
    public boolean beginDrag(Scene scene, GizmoContext context, float screenX, float screenY) {
        // Consumes clicks before transform gizmo; snapshot type depends on handle (light/particle/field).
        if (!hoverHit.isHit()) {
            return false;
        }
        activeHit = hoverHit;
        dragging = true;
        particleDragGuid = null;
        particleDragStartShape = null;
        transformDragStart = null;
        if (Light2DGizmo.HANDLE_DIRECTION.equals(activeHit.handleId())) {
            Transform2DComponent transform = scene.world().getComponent(activeHit.entity(), Transform2DComponent.class);
            if (transform != null) {
                transformDragStart = TransformSnapshot.from(transform);
            }
        } else if (ParticleEmitterGizmo.HANDLE_RADIUS.equals(activeHit.handleId())
                || ParticleEmitterGizmo.HANDLE_ARC.equals(activeHit.handleId())
                || ParticleEmitterGizmo.HANDLE_BOX_X.equals(activeHit.handleId())
                || ParticleEmitterGizmo.HANDLE_BOX_Y.equals(activeHit.handleId())) {
            particleDragGuid = ParticleEmitterGizmo.particleGuidFor(scene, activeHit.entity());
            particleDragStartShape = ParticleEmitterGizmo.shapeSnapshot(scene, activeHit.entity(), assets);
        } else {
            Object component = scene.world().getComponent(activeHit.entity(), activeHit.componentType());
            dragStartSnapshot = registry.dragStartSnapshot(activeHit, component);
        }
        registry.beginDrag(context, scene, activeHit, screenX, screenY);
        return true;
    }

    public void updateDrag(Scene scene, GizmoContext context, float screenX, float screenY) {
        if (!dragging || !activeHit.isHit()) {
            return;
        }
        registry.updateDrag(context, scene, activeHit, screenX, screenY);
    }

    public void endDrag(Scene scene, UndoStack undoStack) {
        if (!dragging || !activeHit.isHit()) {
            dragging = false;
            activeHit = ComponentGizmoHit.NONE;
            return;
        }
        if (transformDragStart != null) {
            Transform2DComponent transform = scene.world().getComponent(activeHit.entity(), Transform2DComponent.class);
            if (transform != null) {
                TransformSnapshot after = TransformSnapshot.from(transform);
                if (!transformDragStart.equalsApprox(after, 0.0001f)) {
                    undoStack.execute(new TransformEditCommand(scene, activeHit.entity(), transformDragStart, after));
                }
            }
        } else if (particleDragGuid != null && particleDragStartShape != null) {
            ParticleSystemDocument.ShapeModule after = ParticleEmitterGizmo.shapeSnapshot(scene, activeHit.entity(), assets);
            if (after != null && !shapesEqual(particleDragStartShape, after)) {
                undoStack.execute(new ParticleShapeEditCommand(assets, particleDragGuid, particleDragStartShape, after));
            }
        } else {
            Object component = scene.world().getComponent(activeHit.entity(), activeHit.componentType());
            Object after = registry.dragEndSnapshot(activeHit, component);
            if (dragStartSnapshot != null && after != null && snapshotsDiffer(dragStartSnapshot, after)) {
                @SuppressWarnings("unchecked")
                var command = new ComponentFieldEditCommand<>(
                        scene,
                        activeHit.entity(),
                        (Class<Object>) activeHit.componentType(),
                        dragStartSnapshot,
                        after
                );
                undoStack.execute(command);
            }
        }
        dragging = false;
        activeHit = ComponentGizmoHit.NONE;
        dragStartSnapshot = null;
        particleDragGuid = null;
        particleDragStartShape = null;
        transformDragStart = null;
    }

    private static boolean snapshotsDiffer(Object before, Object after) {
        if (before instanceof Light2DComponent a && after instanceof Light2DComponent b) {
            return Float.compare(a.range, b.range) != 0
                    || Float.compare(a.innerAngle, b.innerAngle) != 0
                    || Float.compare(a.outerAngle, b.outerAngle) != 0;
        }
        if (before instanceof SceneLightingComponent a && after instanceof SceneLightingComponent b) {
            return Float.compare(a.lightmapMinX, b.lightmapMinX) != 0
                    || Float.compare(a.lightmapMinY, b.lightmapMinY) != 0
                    || Float.compare(a.lightmapMaxX, b.lightmapMaxX) != 0
                    || Float.compare(a.lightmapMaxY, b.lightmapMaxY) != 0;
        }
        return !before.equals(after);
    }

    private static boolean shapesEqual(
            ParticleSystemDocument.ShapeModule a,
            ParticleSystemDocument.ShapeModule b
    ) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Float.compare(a.radius, b.radius) == 0
                && Float.compare(a.width, b.width) == 0
                && Float.compare(a.height, b.height) == 0
                && Float.compare(a.arc, b.arc) == 0
                && java.util.Objects.equals(a.type, b.type);
    }
}
