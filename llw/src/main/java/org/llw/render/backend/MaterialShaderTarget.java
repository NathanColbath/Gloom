package org.llw.render.backend;

/**
 * Tracks whether custom GLSL materials can be compiled for the active renderer.
 */
public final class MaterialShaderTarget {
    private static volatile RendererType activeType = RendererType.OPENGL;

    private MaterialShaderTarget() {
    }

    public static void setActiveRenderer(RendererType type) {
        activeType = type == null ? RendererType.OPENGL : type;
    }

    public static boolean supportsCustomGlsl() {
        return activeType == RendererType.OPENGL;
    }

    public static boolean supportsShaderGraphs() {
        return supportsCustomGlsl();
    }
}
