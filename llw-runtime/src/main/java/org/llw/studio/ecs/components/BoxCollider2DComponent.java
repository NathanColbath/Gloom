package org.llw.studio.ecs.components;

/**
 * Unity {@code BoxCollider2D} equivalent.
 */
public final class BoxCollider2DComponent implements Cloneable {
    public float sizeX = 1f;
    public float sizeY = 1f;
    public float offsetX;
    public float offsetY;
    public boolean isTrigger;
    public int layer;
    public int layerMask = 0xFFFF_FFFF;

    public BoxCollider2DComponent copy() {
        BoxCollider2DComponent copy = new BoxCollider2DComponent();
        copy.sizeX = sizeX;
        copy.sizeY = sizeY;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.isTrigger = isTrigger;
        copy.layer = layer;
        copy.layerMask = layerMask;
        return copy;
    }

    @Override
    public BoxCollider2DComponent clone() {
        return copy();
    }
}
