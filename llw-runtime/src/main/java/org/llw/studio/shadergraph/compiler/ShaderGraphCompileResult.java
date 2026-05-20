package org.llw.studio.shadergraph.compiler;

/**
 * Result of compiling a shader graph to GLSL source.
 */
public final class ShaderGraphCompileResult {
    private final boolean success;
    private final String fragmentSource;
    private final String errorMessage;

    private ShaderGraphCompileResult(boolean success, String fragmentSource, String errorMessage) {
        this.success = success;
        this.fragmentSource = fragmentSource;
        this.errorMessage = errorMessage;
    }

    public static ShaderGraphCompileResult ok(String fragmentSource) {
        return new ShaderGraphCompileResult(true, fragmentSource, "");
    }

    public static ShaderGraphCompileResult error(String message) {
        return new ShaderGraphCompileResult(false, "", message);
    }

    public boolean success() {
        return success;
    }

    public String fragmentSource() {
        return fragmentSource;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
