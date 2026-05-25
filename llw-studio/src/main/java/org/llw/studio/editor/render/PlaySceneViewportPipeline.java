package org.llw.studio.editor.render;

import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.particles.render.ParticleDrawPass;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.render.SceneDrawPass;
import org.llw.studio.render.TilemapDrawPass;
import org.llw.studio.render.UiDrawPass;
import org.llw.studio.scene.Scene;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

/**
 * Game-view play preview using the same world pass order as shipped play mode (unlit editor path).
 *
 * @see org.llw.studio.render.PlaySceneRenderPasses
 */
public final class PlaySceneViewportPipeline {
    private PlaySceneViewportPipeline() {
    }

    /**
     * Draws tilemap, sprites, particles, then screen-space UI into {@code target}.
     */
    public static void draw(
            Scene scene,
            OffscreenTarget target,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs,
            ParticleWorld particles,
            int viewWidth,
            int viewHeight
    ) {
        // Match shipped play order: world passes, flush, then screen-space UI in the FBO.
        TilemapDrawPass.draw(scene, target, assets);
        SceneDrawPass.draw(scene, target, assets, shaderGraphs);
        if (particles != null) {
            ParticleDrawPass.draw(particles, target, assets, shaderGraphs);
        }
        target.flush();
        UiDrawPass.draw(scene, target, assets.uiFontCache(), viewWidth, viewHeight);
        target.flush();
    }
}
