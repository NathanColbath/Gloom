package org.llw.studio.ecs.components;

import java.util.Arrays;

/**
 * Unity {@code EdgeCollider2D} equivalent (open polyline in local space).
 */
public final class EdgeCollider2DComponent implements Cloneable {
    /** Flat {@code [x0,y0,x1,y1,...]} in local space (Y-down). */
    public float[] points = new float[] { -0.5f, 0f, 0.5f, 0f };
    public boolean isTrigger;
    public int layer;
    public int layerMask = 0xFFFF_FFFF;

    public EdgeCollider2DComponent copy() {
        EdgeCollider2DComponent copy = new EdgeCollider2DComponent();
        copy.points = points == null ? new float[0] : Arrays.copyOf(points, points.length);
        copy.isTrigger = isTrigger;
        copy.layer = layer;
        copy.layerMask = layerMask;
        return copy;
    }

    @Override
    public EdgeCollider2DComponent clone() {
        return copy();
    }
}
