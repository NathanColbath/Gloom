package org.llw.studio.editor.gizmo;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;

/**
 * Scene-view overlay and optional handles for a single ECS component type.
 *
 * @param <T> component class
 */
public interface ComponentSceneGizmo<T> {
    /** @return component type this gizmo draws */
    Class<T> componentType();

    /** Draws overlays for every entity with this component. */
    void drawAll(GizmoDrawContext context);

    /**
     * Draws selection-only handles for {@code entity}.
     *
     * @param context  draw context
     * @param entity   selected entity
     * @param component live component instance
     */
    default void drawSelected(GizmoDrawContext context, EntityId entity, T component) {
    }

    /**
     * @return handle under the cursor for the selected entity, or {@link ComponentGizmoHit#NONE}
     */
    default ComponentGizmoHit hitTest(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            T component,
            float screenX,
            float screenY
    ) {
        return ComponentGizmoHit.NONE;
    }

    /** Called when a handle drag begins. */
    default void beginDrag(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            T component,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
    }

    /** Called while a handle drag is active. */
    default void updateDrag(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            T component,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
    }

    /**
     * @return snapshot before drag for undo, or {@code null} if unchanged
     */
    default T dragStartSnapshot(T component) {
        return null;
    }

    /**
     * @return snapshot after drag for undo comparison
     */
    default T dragEndSnapshot(T component) {
        return null;
    }
}
