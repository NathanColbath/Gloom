package org.llw.render.backend;

/**
 * API-neutral compiled shader program handle.
 */
public interface GpuProgram {
    int programId();

    int mvpLocation();

    int textureLocation();

    int useTextureLocation();

    int timeLocation();

    void setUniform1f(int location, float value);

    int uniformLocation(String name);

    void destroy();
}
