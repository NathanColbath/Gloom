package org.llw.render.graphics;

import org.llw.render.resources.ResourceLoader;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.opengl.GL20;

/**
 * Linked OpenGL shader program with cached uniform locations for the default 2D pipeline.
 *
 * <p>Expects vertex shaders to expose {@code uMvp} and fragment shaders to expose
 * {@code uTexture} and {@code uUseTexture}. Call {@link #destroy()} when the program is
 * no longer used.
 */
public final class ShaderProgram {
    private static final Logger log = Log.get(Loggers.GL);

    private final int programId;
    private final int mvpLocation;
    private final int textureLocation;
    private final int useTextureLocation;
    private final int timeLocation;

    private ShaderProgram(int programId, int mvpLocation, int textureLocation, int useTextureLocation, int timeLocation) {
        this.programId = programId;
        this.mvpLocation = mvpLocation;
        this.textureLocation = textureLocation;
        this.useTextureLocation = useTextureLocation;
        this.timeLocation = timeLocation;
    }

    /**
     * Loads GLSL sources from the classpath and compiles/links a program.
     *
     * @param vertexPath   classpath path to the vertex shader source
     * @param fragmentPath classpath path to the fragment shader source
     * @return linked {@link ShaderProgram}
     * @throws IllegalStateException if compilation or linking fails
     */
    public static ShaderProgram fromClasspath(String vertexPath, String fragmentPath) {
        String vertexSource = ResourceLoader.loadText(vertexPath);
        String fragmentSource = ResourceLoader.loadText(fragmentPath);
        return fromSources(vertexSource, fragmentSource);
    }

    /**
     * Compiles and links a program from in-memory GLSL source strings.
     *
     * @param vertexSource   full vertex shader source
     * @param fragmentSource full fragment shader source
     * @return linked {@link ShaderProgram}
     * @throws IllegalStateException if compilation or linking fails
     */
    public static ShaderProgram fromSources(String vertexSource, String fragmentSource) {
        int vertexShader = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(program);
            log.error("Shader link failed: {}", infoLog);
            GL20.glDeleteProgram(program);
            throw new IllegalStateException("Shader link failed: " + infoLog);
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        int mvp = GL20.glGetUniformLocation(program, "uMvp");
        int texture = GL20.glGetUniformLocation(program, "uTexture");
        int useTexture = GL20.glGetUniformLocation(program, "uUseTexture");
        int time = GL20.glGetUniformLocation(program, "uTime");
        return new ShaderProgram(program, mvp, texture, useTexture, time);
    }

    /**
     * Returns the OpenGL program object name.
     *
     * @return linked program id
     */
    public int programId() {
        return programId;
    }

    /**
     * Returns the uniform location of {@code uMvp} (model-view-projection matrix).
     *
     * @return location index, or {@code -1} if not present
     */
    public int mvpLocation() {
        return mvpLocation;
    }

    /**
     * Returns the uniform location of {@code uTexture} (sampler unit).
     *
     * @return location index, or {@code -1} if not present
     */
    public int textureLocation() {
        return textureLocation;
    }

    /**
     * Returns the uniform location of {@code uUseTexture} (boolean sampler enable).
     *
     * @return location index, or {@code -1} if not present
     */
    public int useTextureLocation() {
        return useTextureLocation;
    }

    /**
     * Returns the uniform location of {@code uTime} (shader graph animation).
     *
     * @return location index, or {@code -1} if not present
     */
    public int timeLocation() {
        return timeLocation;
    }

    /**
     * Sets a float uniform when the location is active ({@code >= 0}).
     *
     * @param location from {@link #uniformLocation(String)} or cached getters
     * @param value    uniform value
     */
    public void setUniform1f(int location, float value) {
        if (location >= 0) {
            GL20.glUniform1f(location, value);
        }
    }

    /**
     * Looks up a custom uniform location by name.
     *
     * @param name GLSL uniform identifier
     * @return location index, or {@code -1} if not active in the linked program
     */
    public int uniformLocation(String name) {
        return GL20.glGetUniformLocation(programId, name);
    }

    /**
     * Deletes the OpenGL program object.
     *
     * <p>Do not use this program after destruction.
     */
    public void destroy() {
        GL20.glDeleteProgram(programId);
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(shader);
            log.error("Shader compile failed (type={}): {}", type == GL20.GL_VERTEX_SHADER ? "vertex" : "fragment", infoLog);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Shader compile failed: " + infoLog);
        }
        return shader;
    }
}
