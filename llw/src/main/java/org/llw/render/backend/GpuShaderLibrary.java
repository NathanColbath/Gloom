package org.llw.render.backend;

import org.llw.render.graphics.ShaderProgram;

/**
 * Registry of built-in GPU programs for the active {@link RenderBackend}.
 */
public interface GpuShaderLibrary {
    ShaderProgram spriteShader();

    ShaderProgram litSpriteShader();

    ShaderProgram shapeShader();

    ShaderProgram textShader();

    ShaderProgram get(String name);

    ShaderProgram loadFromSources(String name, String vertexSource, String fragmentSource);

    void dispose();
}
