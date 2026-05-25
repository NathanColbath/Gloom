/**
 * Editor-only offscreen draw passes (grid, selection, physics/camera/component gizmos).
 *
 * <p>Must not depend on ImGui. Orchestration and pass order live in
 * {@link org.llw.studio.editor.render.EditorSceneViewportPipeline}.
 * Shippable world/UI drawing stays in {@code org.llw.studio.render} (llw-runtime).
 */
package org.llw.studio.editor.render.passes;
