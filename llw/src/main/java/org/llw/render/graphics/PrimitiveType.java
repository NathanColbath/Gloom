package org.llw.render.graphics;

/**
 * OpenGL primitive topology for vertex batches submitted by {@link Renderable} implementations.
 */
public enum PrimitiveType {
    /** Each vertex is rendered as an isolated point. */
    POINTS,

    /** Each pair of consecutive vertices forms a line segment. */
    LINES,

    /** Connected line strip through all vertices in order. */
    LINE_STRIP,

    /** Each group of three vertices forms a triangle. */
    TRIANGLES,

    /** Fan of triangles sharing the first vertex. */
    TRIANGLE_FAN,

    /** Strip of triangles sharing edges between consecutive triples. */
    TRIANGLE_STRIP
}
