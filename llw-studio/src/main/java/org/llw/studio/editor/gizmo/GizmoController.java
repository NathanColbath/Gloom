package org.llw.studio.editor.gizmo;

import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.editor.SceneToolMode;
import org.llw.studio.editor.SceneToolMode;
import org.llw.studio.editor.SceneToolState;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.commands.TransformEditCommand;
import org.llw.studio.editor.commands.TransformSnapshot;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

/**
 * Routes scene-tool mode to translate/rotate/scale gizmos and records undo on drag end.
 */
public final class GizmoController {
    private final TranslateGizmo translate = new TranslateGizmo();
    private final RotateGizmo rotate = new RotateGizmo();
    private final ScaleGizmo scale = new ScaleGizmo();

    private EntityId dragEntity = EntityId.none();
    private GizmoHit activeHit = GizmoHit.NONE;
    private GizmoHit hoverHit = GizmoHit.NONE;
    private TransformSnapshot dragStartSnapshot;
    private boolean dragging;

    /** @return gizmo part under the cursor from the last hover update */
    public GizmoHit hoverHit() {
        return hoverHit;
    }

    /** @return whether a gizmo drag is in progress */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * Hit-tests the active gizmo for the selected entity.
     *
     * @param screenX mouse X relative to the scene image (screen pixels)
     * @param screenY mouse Y relative to the scene image (screen pixels)
     */
    public GizmoHit hitTest(Scene scene, SceneToolState toolState, SelectionService selection, GizmoContext context, float screenX, float screenY) {
        if (toolState.mode() == SceneToolMode.HAND) {
            return GizmoHit.NONE;
        }
        EntityId entity = selection.selected();
        if (entity.isNone() || !scene.world().isAlive(entity)) {
            return GizmoHit.NONE;
        }
        return activeTool(toolState).hitTest(context, scene, entity, screenX, screenY);
    }

    /**
     * Updates {@link #hoverHit()} when not dragging.
     *
     * @param screenX mouse X in screen pixels
     * @param screenY mouse Y in screen pixels
     */
    public void updateHover(Scene scene, SceneToolState toolState, SelectionService selection, GizmoContext context, float screenX, float screenY) {
        if (dragging) {
            return;
        }
        hoverHit = hitTest(scene, toolState, selection, context, screenX, screenY);
    }

    /**
     * Starts a gizmo drag if the cursor hits a handle.
     *
     * @return true if a drag started (caller should skip picking)
     */
    public boolean beginDrag(
            Scene scene,
            SceneToolState toolState,
            SelectionService selection,
            GizmoContext context,
            float screenX,
            float screenY
    ) {
        if (toolState.mode() == SceneToolMode.HAND) {
            return false;
        }
        EntityId entity = selection.selected();
        if (entity.isNone()) {
            return false;
        }
        GizmoHit hit = activeTool(toolState).hitTest(context, scene, entity, screenX, screenY);
        if (hit == GizmoHit.NONE) {
            return false;
        }
        Transform2DComponent transform = scene.world().getComponent(entity, Transform2DComponent.class);
        if (transform == null) {
            return false;
        }
        dragEntity = entity;
        activeHit = hit;
        dragStartSnapshot = TransformSnapshot.from(transform);
        dragging = true;
        activeTool(toolState).beginDrag(context, scene, entity, hit, screenX, screenY);
        return true;
    }

    /** Applies drag delta while {@link #isDragging()}. */
    public void updateDrag(Scene scene, SceneToolState toolState, GizmoContext context, float screenX, float screenY) {
        if (!dragging || dragEntity.isNone()) {
            return;
        }
        new TransformSystem().onUpdate(scene.world(), 0f);
        activeTool(toolState).updateDrag(context, scene, dragEntity, activeHit, screenX, screenY);
    }

    /**
     * Ends the drag and pushes a {@link TransformEditCommand} if the transform changed.
     *
     * @param undoStack stack to receive the command
     */
    public void endDrag(Scene scene, UndoStack undoStack) {
        if (!dragging || dragEntity.isNone()) {
            dragging = false;
            return;
        }
        Transform2DComponent transform = scene.world().getComponent(dragEntity, Transform2DComponent.class);
        if (transform != null && dragStartSnapshot != null) {
            TransformSnapshot after = TransformSnapshot.from(transform);
            if (!dragStartSnapshot.equalsApprox(after, 0.0001f)) {
                undoStack.execute(new TransformEditCommand(scene, dragEntity, dragStartSnapshot, after));
            }
        }
        dragging = false;
        dragEntity = EntityId.none();
        activeHit = GizmoHit.NONE;
        dragStartSnapshot = null;
    }

    /** Draws the active tool gizmo for the selected entity. */
    public void draw(Scene scene, SceneToolState toolState, SelectionService selection, GizmoContext context, OffscreenTarget target) {
        if (toolState.mode() == SceneToolMode.HAND) {
            return;
        }
        EntityId entity = selection.selected();
        if (entity.isNone() || !scene.world().isAlive(entity)) {
            return;
        }
        new TransformSystem().onUpdate(scene.world(), 0f);
        activeTool(toolState).draw(context, target, scene, entity, hoverHit);
    }

    private GizmoTool activeTool(SceneToolState toolState) {
        return switch (toolState.mode()) {
            case ROTATE -> rotate;
            case SCALE -> scale;
            case MOVE -> translate;
            case HAND, TILE_PAINT, TILE_ERASE -> translate;
        };
    }
}
