package org.llw.studio.editor.render;

import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SceneToolState;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.gizmo.GizmoController;
import org.llw.studio.editor.render.passes.CameraGizmoDrawPass;
import org.llw.studio.editor.render.passes.GizmoDrawPass;
import org.llw.studio.editor.render.passes.GridDrawPass;
import org.llw.studio.editor.render.passes.PhysicsGizmoDrawPass;
import org.llw.studio.editor.render.passes.SelectionOutlinePass;
import org.llw.studio.editor.render.passes.TilemapGridDrawPass;
import org.llw.studio.particles.render.ParticleDrawPass;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.render.SceneDrawPass;
import org.llw.studio.render.TilemapDrawPass;
import org.llw.studio.scene.Scene;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

/**
 * Ordered draw passes for the editor scene view offscreen target (world + editor overlays, no UI).
 *
 * @see org.llw.studio.editor.panels.SceneViewPanel
 */
public final class EditorSceneViewportPipeline {
    private EditorSceneViewportPipeline() {
    }

    /**
     * Draws grid through transform gizmo layers. Caller flushes and draws {@code UiDrawPass} if needed.
     */
    public static void draw(
            Scene sceneToRender,
            Scene editScene,
            OffscreenTarget target,
            EditorCamera camera,
            int viewWidth,
            int viewHeight,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs,
            SelectionService selection,
            EditorSession session,
            boolean isPlaying,
            EntityId activeTilemap,
            SceneToolState toolState,
            GizmoController gizmoController
    ) {
        // Pass order: docs/studio/editor-architecture.md § Scene view (world content first).
        GridDrawPass.draw(target, camera, viewWidth, viewHeight);
        if (!activeTilemap.isNone()) {
            EditorWorldTransforms.ensureUpdated(sceneToRender);
            TilemapGridDrawPass.draw(sceneToRender, target, camera, viewWidth, viewHeight, activeTilemap);
        }
        TilemapDrawPass.draw(sceneToRender, target, assets);
        SceneDrawPass.draw(sceneToRender, target, assets, shaderGraphs);
        // Play-in-scene uses the cloned play particle world; edit mode uses the editor preview sim.
        ParticleWorld particles = session.playParticleWorld() != null && isPlaying
                ? session.playParticleWorld()
                : session.particleWorld();
        ParticleDrawPass.draw(particles, target, assets, shaderGraphs);

        if (!isPlaying) {
            // One transform sync before all edit overlays that read WorldTransformComponent.
            EditorWorldTransforms.ensureUpdated(editScene);
            CameraGizmoDrawPass.draw(
                    editScene,
                    target,
                    selection,
                    camera,
                    viewWidth,
                    viewHeight,
                    session.gameViewWidth(),
                    session.gameViewHeight()
            );
            PhysicsGizmoDrawPass.draw(editScene, target, camera, viewWidth, viewHeight);
            EditorWorldTransforms.ensureUpdated(sceneToRender);
            SelectionOutlinePass.draw(sceneToRender, target, assets, selection, camera, viewWidth, viewHeight);
            GizmoDrawPass.draw(sceneToRender, target, camera, toolState, selection, gizmoController, viewWidth, viewHeight);
        } else {
            // Play-in-scene: world + selection only; edit gizmos would disagree with play transforms.
            EditorWorldTransforms.ensureUpdated(sceneToRender);
            SelectionOutlinePass.draw(sceneToRender, target, assets, selection, camera, viewWidth, viewHeight);
        }
    }
}
