package org.llw.studio.ecs.components;

/**
 * Unity {@code CircleCollider2D} equivalent.
 */
public final class CircleCollider2DComponent implements Cloneable {
    public float radius = 0.5f;
    public float offsetX;
    public float offsetY;
    public boolean isTrigger;
    public int layer;
    public int layerMask = 0xFFFF_FFFF;

    public CircleCollider2DComponent copy() {
        CircleCollider2DComponent copy = new CircleCollider2DComponent();
        copy.radius = radius;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.isTrigger = isTrigger;
        copy.layer = layer;
        copy.layerMask = layerMask;
        return copy;
    }

    @Override
    public CircleCollider2DComponent clone() {
        return copy();
    }
}
