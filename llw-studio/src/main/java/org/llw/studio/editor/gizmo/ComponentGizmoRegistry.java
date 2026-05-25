package org.llw.studio.editor.gizmo;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of {@link ComponentSceneGizmo} implementations for the scene view.
 */
public final class ComponentGizmoRegistry {
    private final Map<Class<?>, ComponentSceneGizmo<?>> gizmos = new LinkedHashMap<>();

    /**
     * @param gizmo gizmo to register
     */
    public void register(ComponentSceneGizmo<?> gizmo) {
        gizmos.put(gizmo.componentType(), gizmo);
    }

    /** @return registered gizmos in registration order */
    public List<ComponentSceneGizmo<?>> all() {
        return new ArrayList<>(gizmos.values());
    }

    /**
     * @param type component class
     * @return gizmo for the type, or null
     */
    @SuppressWarnings("unchecked")
    public <T> ComponentSceneGizmo<T> get(Class<T> type) {
        return (ComponentSceneGizmo<T>) gizmos.get(type);
    }

    public void drawAll(GizmoDrawContext context) {
        org.llw.studio.editor.render.EditorWorldTransforms.ensureUpdated(context.scene());
        for (ComponentSceneGizmo<?> gizmo : gizmos.values()) {
            gizmo.drawAll(context);
        }
    }

    public void drawSelected(GizmoDrawContext context) {
        org.llw.studio.editor.render.EditorWorldTransforms.ensureUpdated(context.scene());
        Scene scene = context.scene();
        for (EntityId entity : context.selection().allSelected()) {
            if (!scene.world().isAlive(entity)) {
                continue;
            }
            for (ComponentSceneGizmo<?> gizmo : gizmos.values()) {
                Object component = scene.world().getComponent(entity, gizmo.componentType());
                if (component != null) {
                    drawSelectedUnchecked(gizmo, context, entity, component);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void drawSelectedUnchecked(
            ComponentSceneGizmo<?> gizmo,
            GizmoDrawContext context,
            EntityId entity,
            Object component
    ) {
        ((ComponentSceneGizmo<T>) gizmo).drawSelected(context, entity, (T) component);
    }

    public ComponentGizmoHit hitTest(
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            float screenX,
            float screenY
    ) {
        for (ComponentSceneGizmo<?> gizmo : gizmos.values()) {
            Object component = scene.world().getComponent(entity, gizmo.componentType());
            if (component == null) {
                continue;
            }
            ComponentGizmoHit hit = hitTestUnchecked(gizmo, gizmoContext, scene, entity, component, screenX, screenY);
            if (hit.isHit()) {
                return hit;
            }
        }
        return ComponentGizmoHit.NONE;
    }

    @SuppressWarnings("unchecked")
    private static <T> ComponentGizmoHit hitTestUnchecked(
            ComponentSceneGizmo<?> gizmo,
            GizmoContext gizmoContext,
            Scene scene,
            EntityId entity,
            Object component,
            float screenX,
            float screenY
    ) {
        return ((ComponentSceneGizmo<T>) gizmo).hitTest(
                gizmoContext,
                scene,
                entity,
                (T) component,
                screenX,
                screenY
        );
    }

    @SuppressWarnings("unchecked")
    public <T> void beginDrag(
            GizmoContext gizmoContext,
            Scene scene,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
        ComponentSceneGizmo<T> gizmo = get((Class<T>) hit.componentType());
        if (gizmo == null) {
            return;
        }
        T component = scene.world().getComponent(hit.entity(), (Class<T>) hit.componentType());
        if (component != null) {
            gizmo.beginDrag(gizmoContext, scene, hit.entity(), component, hit, screenX, screenY);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void updateDrag(
            GizmoContext gizmoContext,
            Scene scene,
            ComponentGizmoHit hit,
            float screenX,
            float screenY
    ) {
        ComponentSceneGizmo<T> gizmo = get((Class<T>) hit.componentType());
        if (gizmo == null) {
            return;
        }
        T component = scene.world().getComponent(hit.entity(), (Class<T>) hit.componentType());
        if (component != null) {
            gizmo.updateDrag(gizmoContext, scene, hit.entity(), component, hit, screenX, screenY);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Object dragStartSnapshot(ComponentGizmoHit hit, T component) {
        ComponentSceneGizmo<T> gizmo = get((Class<T>) hit.componentType());
        return gizmo == null ? null : gizmo.dragStartSnapshot(component);
    }

    @SuppressWarnings("unchecked")
    public <T> Object dragEndSnapshot(ComponentGizmoHit hit, T component) {
        ComponentSceneGizmo<T> gizmo = get((Class<T>) hit.componentType());
        return gizmo == null ? null : gizmo.dragEndSnapshot(component);
    }
}
