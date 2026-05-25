package org.llw.studio.materials.runtime;

import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.materials.model.MaterialShaderSource;

import java.util.HashMap;
import java.util.Map;

/**
 * GPU-ready material resolved from an asset GUID.
 */
public final class ResolvedMaterial {
    public final MaterialShaderSource shaderSource;
    public final ShaderProgram program;
    public final Texture2d normalMap;
    public final boolean useNormalMap;
    public final Map<String, Float> floats = new HashMap<>();
    public final Map<String, float[]> colors = new HashMap<>();

    public ResolvedMaterial(
            MaterialShaderSource shaderSource,
            ShaderProgram program,
            Texture2d normalMap,
            boolean useNormalMap
    ) {
        this.shaderSource = shaderSource;
        this.program = program;
        this.normalMap = normalMap;
        this.useNormalMap = useNormalMap;
    }

    public boolean isLit() {
        return shaderSource == MaterialShaderSource.BUILTIN_LIT;
    }
}
