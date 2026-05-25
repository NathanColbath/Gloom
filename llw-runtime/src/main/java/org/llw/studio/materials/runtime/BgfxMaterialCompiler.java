package org.llw.studio.materials.runtime;

import org.llw.render.graphics.ShaderProgram;
import org.llw.studio.materials.model.MaterialDocument;

/**
 * Future bgfx shader compilation target for material and shader-graph assets.
 *
 * <p>Custom materials currently fall back to built-in lit shaders when
 * {@link org.llw.render.backend.MaterialShaderTarget#supportsCustomGlsl()} is false.
 */
public final class BgfxMaterialCompiler {
    private BgfxMaterialCompiler() {
    }

    public static ShaderProgram compileOrNull(MaterialDocument document, String vertexGlsl, String fragmentGlsl) {
        return null;
    }
}
