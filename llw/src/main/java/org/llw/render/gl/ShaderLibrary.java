package org.llw.render.gl;

import java.util.HashMap;
import java.util.Map;

import org.llw.render.graphics.ShaderProgram;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Named registry of compiled {@link ShaderProgram} instances.
 *
 * <p>{@link #loadDefaults()} registers the built-in sprite, shape, and text shaders from
 * {@link DefaultShaders} in-memory sources.
 */
public final class ShaderLibrary {
    private static final Logger log = Log.get(Loggers.GL);

    private final Map<String, ShaderProgram> programs = new HashMap<>();
    private ShaderProgram spriteShader;
    private ShaderProgram shapeShader;
    private ShaderProgram textShader;

    /**
     * Compiles and registers the default sprite, shape, and text shader programs from
     * embedded GLSL sources.
     */
    public void loadDefaults() {
        spriteShader = loadFromSources("sprite",
                DefaultShaders.SPRITE_VERTEX,
                DefaultShaders.SPRITE_FRAGMENT);
        shapeShader = loadFromSources("shape",
                DefaultShaders.SHAPE_VERTEX,
                DefaultShaders.SHAPE_FRAGMENT);
        textShader = loadFromSources("text",
                DefaultShaders.TEXT_VERTEX,
                DefaultShaders.TEXT_FRAGMENT);
    }

    /**
     * Returns the default shader used for batched textured quads.
     *
     * @return sprite shader loaded by {@link #loadDefaults()}
     */
    public ShaderProgram spriteShader() {
        return spriteShader;
    }

    /**
     * Returns the default shader used for untextured vertex draws.
     *
     * @return shape shader loaded by {@link #loadDefaults()}
     */
    public ShaderProgram shapeShader() {
        return shapeShader;
    }

    /**
     * Returns the default shader used for text glyph quads.
     *
     * @return text shader loaded by {@link #loadDefaults()}
     */
    public ShaderProgram textShader() {
        return textShader;
    }

    /**
     * Compiles a shader pair from source strings and stores it under {@code name}.
     *
     * @param name           registry key for later {@link #get(String)} lookups
     * @param vertexSource   vertex shader GLSL source
     * @param fragmentSource fragment shader GLSL source
     * @return the compiled and registered program
     */
    public ShaderProgram loadFromSources(String name, String vertexSource, String fragmentSource) {
        ShaderProgram program = ShaderProgram.fromSources(vertexSource, fragmentSource);
        programs.put(name, program);
        log.debug("Loaded shader name={} source=inline programId={}", name, program.programId());
        return program;
    }

    /**
     * Replaces a named program, destroying the previous instance if present.
     *
     * @param name           registry key
     * @param vertexSource   vertex shader GLSL source
     * @param fragmentSource fragment shader GLSL source
     * @return newly compiled program
     */
    public ShaderProgram reloadFromSources(String name, String vertexSource, String fragmentSource) {
        ShaderProgram existing = programs.remove(name);
        if (existing != null) {
            existing.destroy();
        }
        return loadFromSources(name, vertexSource, fragmentSource);
    }

    /**
     * Compiles a shader pair from classpath sources and stores it under {@code name}.
     *
     * @param name          registry key for later {@link #get(String)} lookups
     * @param vertexPath    classpath path to the vertex shader source
     * @param fragmentPath  classpath path to the fragment shader source
     * @return the compiled and registered program
     */
    public ShaderProgram load(String name, String vertexPath, String fragmentPath) {
        ShaderProgram program = ShaderProgram.fromClasspath(vertexPath, fragmentPath);
        programs.put(name, program);
        log.debug("Loaded shader name={} vertex={} fragment={} programId={}", name, vertexPath, fragmentPath, program.programId());
        return program;
    }

    /**
     * Returns a previously loaded program by name.
     *
     * @param name registry key passed to {@link #load}
     * @return the registered shader program
     * @throws IllegalArgumentException if no program was loaded under {@code name}
     */
    public ShaderProgram get(String name) {
        ShaderProgram program = programs.get(name);
        if (program == null) {
            throw new IllegalArgumentException("Shader not loaded: " + name);
        }
        return program;
    }

    /**
     * Destroys all registered programs and clears the registry.
     */
    public void dispose() {
        programs.values().forEach(ShaderProgram::destroy);
        programs.clear();
    }
}
