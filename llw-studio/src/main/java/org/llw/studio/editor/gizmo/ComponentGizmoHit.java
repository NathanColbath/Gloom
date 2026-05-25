package org.llw.studio.editor.gizmo;

import org.llw.studio.ecs.EntityId;

/**
 * Hit-test result for a component scene gizmo handle.
 */
public record ComponentGizmoHit(Class<?> componentType, EntityId entity, String handleId) {
    public static final ComponentGizmoHit NONE = new ComponentGizmoHit(null, EntityId.none(), "");

    /** @return whether this hit refers to a handle */
    public boolean isHit() {
        return componentType != null && !entity.isNone() && handleId != null && !handleId.isBlank();
    }
}
