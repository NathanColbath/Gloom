/**
 * Component-specific scene overlays (lights, particles, scene lighting) drawn in the scene view.
 *
 * <p>Distinct from {@link org.llw.studio.editor.gizmo}: transform tools (move/rotate/scale handles)
 * live there; this package registers per-component {@link org.llw.studio.editor.gizmo.ComponentSceneGizmo}
 * implementations under {@code builtin}.
 *
 * @see org.llw.studio.editor.render.passes.ComponentGizmoDrawPass
 */
package org.llw.studio.editor.gizmos;
