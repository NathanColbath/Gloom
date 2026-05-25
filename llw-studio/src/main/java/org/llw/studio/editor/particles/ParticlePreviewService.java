package org.llw.studio.editor.particles;

import org.llw.render.core.Color;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.particles.render.ParticleDrawPass;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

/**
 * Renders particle preview into an offscreen target.
 */
public final class ParticlePreviewService {
    /**
     * @param target       offscreen target
     * @param world        particle world containing preview emitter
     * @param previewGuid  active particle asset GUID
     * @param assets       asset database
     * @param shaderGraphs optional shader programs
     */
    public void render(
            OffscreenTarget target,
            ParticleWorld world,
            String previewGuid,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs
    ) {
        if (target == null || world == null || previewGuid == null || previewGuid.isBlank()) {
            return;
        }
        EmitterState preview = world.previewEmitter(previewGuid);
        if (preview == null) {
            return;
        }
        target.clear(Color.BLACK);
        ParticleDrawPass.drawPreviewEmitter(preview, target, assets, shaderGraphs); // Isolated emitter, not scene ParticleWorld sim.
        target.flush();
    }
}
