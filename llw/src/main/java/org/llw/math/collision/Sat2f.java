package org.llw.math.collision;

import org.llw.math.vector.Vector2f;

/**
 * Separating axis theorem for 2D convex polygons.
 */
public final class Sat2f {
    private Sat2f() {}

    /**
     * Returns whether two convex polygons overlap.
     *
     * @param a first polygon vertices in order (CW or CCW)
     * @param b second polygon vertices in order
     * @return {@code true} when overlapping
     */
    public static boolean intersects(Vector2f[] a, Vector2f[] b) {
        return !findSeparatingAxis(a, b) && !findSeparatingAxis(b, a);
    }

    private static boolean findSeparatingAxis(Vector2f[] shapeA, Vector2f[] shapeB) {
        int count = shapeA.length;
        for (int i = 0; i < count; i++) {
            Vector2f p1 = shapeA[i];
            Vector2f p2 = shapeA[(i + 1) % count];
            float axisX = -(p2.y - p1.y);
            float axisY = p2.x - p1.x;
            float minA = Float.POSITIVE_INFINITY;
            float maxA = Float.NEGATIVE_INFINITY;
            float minB = Float.POSITIVE_INFINITY;
            float maxB = Float.NEGATIVE_INFINITY;
            for (Vector2f v : shapeA) {
                float proj = v.x * axisX + v.y * axisY;
                minA = Math.min(minA, proj);
                maxA = Math.max(maxA, proj);
            }
            for (Vector2f v : shapeB) {
                float proj = v.x * axisX + v.y * axisY;
                minB = Math.min(minB, proj);
                maxB = Math.max(maxB, proj);
            }
            if (maxA < minB || maxB < minA) {
                return true;
            }
        }
        return false;
    }
}
