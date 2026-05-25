package org.llw.studio.render;

import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.backend.RenderBackend;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.lighting.LightingFrameData;
import org.llw.studio.lighting.LightingSystem;
import org.llw.studio.materials.runtime.MaterialProgramCache;
import org.llw.studio.scene.Scene;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

/**
 * Chooses lit or unlit sprite drawing for a scene view.
 */
public final class SceneRenderPasses {
    private SceneRenderPasses() {
    }

    public static void drawSprites(
            Scene scene,
            OffscreenTarget target,
            AssetDatabase assets,
            MaterialProgramCache materials,
            ShaderGraphProgramCache shaderGraphs,
            RenderBackend backend
    ) {
        LightingFrameData lighting = LightingSystem.gather(scene, assets);
        if (lighting.useLighting && backend != null) {
            LitSceneDrawPass.draw(scene, target, assets, materials, lighting, backend, shaderGraphs);
        } else {
            SceneDrawPass.draw(scene, target, assets, shaderGraphs);
        }
    }
}
