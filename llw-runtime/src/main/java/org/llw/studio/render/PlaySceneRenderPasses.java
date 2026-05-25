package org.llw.studio.render;

import org.llw.render.backend.RenderBackend;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.materials.runtime.MaterialProgramCache;
import org.llw.studio.particles.render.ParticleDrawPass;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.scene.Scene;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
import org.llw.studio.ui.UiFontCache;
import org.llw.studio.ui.UiLayoutContext;

/**
 * Shared play-mode scene draw sequence used by the game view, standalone player, and builds.
 *
 * <p>Order: tilemaps → sprites (lit or unlit) → particles → flush → UI → flush.
 */
public final class PlaySceneRenderPasses {
    private PlaySceneRenderPasses() {
    }

    /**
     * Draws the playable scene into an offscreen target (world passes, then screen-space UI).
     *
     * @param scene         play scene
     * @param target        offscreen target with play camera already applied
     * @param assets        project assets
     * @param materials     material program cache
     * @param shaderGraphs  shader graph cache
     * @param backend       render backend
     * @param particles     active particle world, or null
     * @param uiFonts       UI font cache for {@link UiDrawPass}
     * @param uiLayout      layout context (typically {@link UiLayoutContext#forPlay()})
     */
    public static void draw(
            Scene scene,
            OffscreenTarget target,
            AssetDatabase assets,
            MaterialProgramCache materials,
            ShaderGraphProgramCache shaderGraphs,
            RenderBackend backend,
            ParticleWorld particles,
            UiFontCache uiFonts,
            UiLayoutContext uiLayout
    ) {
        TilemapDrawPass.draw(scene, target, assets);
        SceneRenderPasses.drawSprites(scene, target, assets, materials, shaderGraphs, backend);
        if (particles != null) {
            ParticleDrawPass.draw(particles, target, assets, shaderGraphs);
        }
        target.flush();
        UiDrawPass.draw(scene, target, uiFonts, uiLayout);
        target.flush();
    }
}
