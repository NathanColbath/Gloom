package org.llw.studio.shadergraph.model;

/**
 * Built-in fragment shader graph node kinds.
 */
public enum ShaderNodeType {
    FragmentOutput,
    TextureSample,
    VertexColor,
    Uv,
    Color,
    FloatConstant,
    Multiply,
    Add,
    Lerp,
    Time,
    SplitRgba,
    CombineRgba
}
