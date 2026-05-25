package org.llw.studio.lighting;

import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.materials.runtime.ResolvedMaterial;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.Map;

/**
 * Uploads {@link LightingFrameData} and per-sprite data to the lit sprite shader.
 */
public final class LitUniformBinder {
    private LitUniformBinder() {
    }

    public static void applyScene(ShaderProgram shader, LightingFrameData lighting) {
        if (shader == null || lighting == null) {
            return;
        }
        int ambientLoc = shader.uniformLocation("uAmbient");
        if (ambientLoc >= 0) {
            GL20.glUniform4f(
                    ambientLoc,
                    lighting.ambientR * lighting.ambientIntensity,
                    lighting.ambientG * lighting.ambientIntensity,
                    lighting.ambientB * lighting.ambientIntensity,
                    lighting.ambientIntensity
            );
        }
        int lightDataLoc = shader.uniformLocation("uLightData");
        if (lightDataLoc >= 0) {
            GL20.glUniform1fv(lightDataLoc, lighting.lightData);
        }
        int lightMapLoc = shader.uniformLocation("uLightMap");
        int useLightMapLoc = shader.uniformLocation("uUseLightMap");
        if (lighting.useLightmap && lighting.lightmapTexture != null) {
            lighting.lightmapTexture.bind(2);
            if (lightMapLoc >= 0) {
                GL20.glUniform1i(lightMapLoc, 2);
            }
            if (useLightMapLoc >= 0) {
                GL20.glUniform1i(useLightMapLoc, 1);
            }
            int transformLoc = shader.uniformLocation("uLightMapTransform");
            if (transformLoc >= 0) {
                GL20.glUniform4f(
                        transformLoc,
                        lighting.lightmapMinX,
                        lighting.lightmapMinY,
                        lighting.lightmapWidth,
                        lighting.lightmapHeight
                );
            }
        } else if (useLightMapLoc >= 0) {
            GL20.glUniform1i(useLightMapLoc, 0);
        }
    }

    public static void applyNormalMap(ShaderProgram shader, Texture2d normalMap, boolean useNormalMap) {
        if (shader == null) {
            return;
        }
        int normalLoc = shader.uniformLocation("uNormalMap");
        int useNormalLoc = shader.uniformLocation("uUseNormalMap");
        if (useNormalMap && normalMap != null) {
            normalMap.bind(1);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            if (normalLoc >= 0) {
                GL20.glUniform1i(normalLoc, 1);
            }
            if (useNormalLoc >= 0) {
                GL20.glUniform1i(useNormalLoc, 1);
            }
        } else if (useNormalLoc >= 0) {
            GL20.glUniform1i(useNormalLoc, 0);
        }
    }

    public static void applySpriteRotation(ShaderProgram shader, float rotationDegrees) {
        if (shader == null) {
            return;
        }
        float rad = (float) Math.toRadians(rotationDegrees);
        shader.setUniform1f(shader.uniformLocation("uSpriteRotation"), rad);
    }

    /**
     * Uploads named float and color properties from a resolved material when the shader declares matching uniforms.
     */
    public static void applyMaterialProperties(ShaderProgram shader, ResolvedMaterial material) {
        if (shader == null || material == null) {
            return;
        }
        for (Map.Entry<String, Float> entry : material.floats.entrySet()) {
            int location = shader.uniformLocation(entry.getKey());
            if (location >= 0) {
                GL20.glUniform1f(location, entry.getValue());
            }
        }
        for (Map.Entry<String, float[]> entry : material.colors.entrySet()) {
            int location = shader.uniformLocation(entry.getKey());
            if (location >= 0) {
                float[] rgba = entry.getValue();
                GL20.glUniform4f(location, rgba[0], rgba[1], rgba[2], rgba[3]);
            }
        }
    }
}
