package org.llw.studio.editor.gizmos;

import org.llw.studio.editor.gizmo.ComponentGizmoRegistry;
import org.llw.studio.editor.gizmos.builtin.Light2DGizmo;
import org.llw.studio.editor.gizmos.builtin.ParticleEmitterGizmo;
import org.llw.studio.editor.gizmos.builtin.SceneLightingGizmo;

/**
 * Registers built-in component scene gizmos.
 */
public final class ComponentGizmos {
    private ComponentGizmos() {
    }

    /**
     * @param registry target registry
     */
    public static void registerBuiltins(ComponentGizmoRegistry registry) {
        registry.register(new Light2DGizmo());
        registry.register(new SceneLightingGizmo());
        registry.register(new ParticleEmitterGizmo());
    }
}
