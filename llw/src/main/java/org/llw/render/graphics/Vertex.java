package org.llw.render.graphics;

import org.llw.render.core.Color;
import org.llw.math.vector.Vector2f;

/**
 * Single vertex for batched 2D geometry: position, texture coordinates, and color.
 *
 * <p>Positions are in Y-down world or local space depending on the active
 * {@link DrawState#transform()}. Texture coordinates follow the usual {@code (0,0)}–{@code (1,1)}
 * range with {@code v} increasing downward for atlas compatibility.
 */
public final class Vertex {
    /** Position in local or world space (Y-down). */
    public final Vector2f position;

    /** Normalized texture coordinates {@code (u, v)}. */
    public final Vector2f texCoord;

    /** Per-vertex tint color in RGBA byte components. */
    public final Color color;

    /**
     * Creates a vertex from position, texture coordinates, and color.
     *
     * @param position local/world position (Y-down)
     * @param texCoord normalized UV coordinates
     * @param color    vertex tint
     */
    public Vertex(Vector2f position, Vector2f texCoord, Color color) {
        this.position = position;
        this.texCoord = texCoord;
        this.color = color;
    }

    /**
     * Creates a vertex from scalar components.
     *
     * @param x vertex X in local/world space
     * @param y vertex Y in local/world space (Y-down)
     * @param u texture U coordinate
     * @param v texture V coordinate
     * @param color vertex tint
     */
    public Vertex(float x, float y, float u, float v, Color color) {
        this(new Vector2f(x, y), new Vector2f(u, v), color);
    }
}
