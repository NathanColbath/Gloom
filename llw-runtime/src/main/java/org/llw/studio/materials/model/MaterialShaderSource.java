package org.llw.studio.materials.model;

/**
 * How a material resolves its GPU program.
 */
public enum MaterialShaderSource {
    /** Default unlit sprite shader (legacy). */
    BUILTIN_UNLIT,
    /** Built-in normal-map 2D lit sprite shader. */
    BUILTIN_LIT,
    /** Fragment graph compiled with lit vertex pass. */
    SHADER_GRAPH
}
