package org.llw.studio.render;

import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.materials.runtime.ResolvedMaterial;

/**
 * Packed key for lit sprite batch breaks (shader, albedo, normal map, rotation).
 */
public final class LitBatchKey {
    private static final long INVALID = -1L;

    private LitBatchKey() {
    }

    public static long invalid() {
        return INVALID;
    }

    public static long of(
            ShaderProgram shader,
            Texture2d albedo,
            ResolvedMaterial material,
            float rotationDegrees
    ) {
        if (shader == null || albedo == null) {
            return INVALID;
        }
        int shaderId = shader.programId();
        int albedoId = albedo.id();
        int normalId = 0;
        int useNormal = 0;
        if (material != null && material.normalMap != null) {
            normalId = material.normalMap.id();
            useNormal = material.useNormalMap ? 1 : 0;
        }
        int rotationBits = Float.floatToIntBits(rotationDegrees);
        return ((long) shaderId << 42)
                | ((long) albedoId << 26)
                | ((long) normalId << 10)
                | ((long) useNormal << 9)
                | (rotationBits & 0x1FF_FFFFL);
    }
}
