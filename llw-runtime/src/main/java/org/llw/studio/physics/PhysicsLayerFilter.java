package org.llw.studio.physics;

import org.jbox2d.dynamics.Filter;

/**
 * Layer category/mask helpers (Unity-style physics layers).
 */
public final class PhysicsLayerFilter {
    private PhysicsLayerFilter() {
    }

    public static Filter createFilter(int layer, int layerMask) {
        int clampedLayer = Math.max(0, Math.min(15, layer));
        Filter filter = new Filter();
        filter.categoryBits = 1 << clampedLayer;
        filter.maskBits = layerMask;
        return filter;
    }

    public static boolean canCollide(int layerA, int maskA, int layerB, int maskB) {
        int categoryA = 1 << Math.max(0, Math.min(15, layerA));
        int categoryB = 1 << Math.max(0, Math.min(15, layerB));
        return (maskA & categoryB) != 0 && (maskB & categoryA) != 0;
    }
}
