package org.llw.studio.ecs.components;

/**
 * 2D sprite draw settings referencing a texture by GUID.
 * <p>
 * Rendered in editor scene view and play mode at the entity's world transform
 * (Y-down). {@link #sortingOrder} breaks ties among sprites at the same depth.
 */
public final class SpriteRendererComponent implements Cloneable {
    /** Asset GUID of the sprite slice to draw. */
    public String spriteGuid = "";
    /** @deprecated use {@link #spriteGuid}; kept for scene migration */
    public String textureGuid = "";
    /** Tint red channel, {@code [0, 1]}. */
    public float r = 1f;
    /** Tint green channel, {@code [0, 1]}. */
    public float g = 1f;
    /** Tint blue channel, {@code [0, 1]}. */
    public float b = 1f;
    /** Tint alpha channel, {@code [0, 1]}. */
    public float a = 1f;
    /** Draw order among sprites (higher draws on top). */
    public int sortingOrder;
    /** Optional shader graph asset GUID; empty uses the default sprite shader. */
    public String shaderGraphGuid = "";

    /**
     * @return deep copy of this sprite renderer settings
     */
    public SpriteRendererComponent copy() {
        SpriteRendererComponent copy = new SpriteRendererComponent();
        copy.spriteGuid = spriteGuid;
        copy.textureGuid = textureGuid;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.sortingOrder = sortingOrder;
        copy.shaderGraphGuid = shaderGraphGuid;
        return copy;
    }

    @Override
    public SpriteRendererComponent clone() {
        return copy();
    }
}
