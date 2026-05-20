package org.llw.math.collision;

import org.llw.math.geometry.Aabb2f;
import org.llw.math.geometry.Circle2f;
import org.llw.math.geometry.Line2f;
import org.llw.math.geometry.Ray2f;
import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;

/**
 * Static 2D intersection and containment tests.
 */
public final class Intersection2 {
    private Intersection2() {}

    /**
     * Returns whether {@code rect} contains {@code (x, y)}.
     *
     * @param rect axis-aligned rectangle
     * @param x point X
     * @param y point Y
     * @return {@code true} when inside (inclusive edges)
     */
    public static boolean contains(RectF rect, float x, float y) {
        return rect.contains(x, y);
    }

    /**
     * Returns whether {@code circle} contains {@code (x, y)}.
     *
     * @param circle query circle
     * @param x point X
     * @param y point Y
     * @return {@code true} when inside (inclusive boundary)
     */
    public static boolean contains(Circle2f circle, float x, float y) {
        return circle.contains(x, y);
    }

    /**
     * Returns whether two rectangles overlap.
     *
     * @param a first rectangle
     * @param b second rectangle
     * @return {@code true} when overlapping
     */
    public static boolean intersects(RectF a, RectF b) {
        return a.intersects(b);
    }

    /**
     * Returns whether two circles overlap.
     *
     * @param a first circle
     * @param b second circle
     * @return {@code true} when overlapping
     */
    public static boolean intersects(Circle2f a, Circle2f b) {
        return a.overlaps(b);
    }

    /**
     * Returns whether a circle overlaps a rectangle.
     *
     * @param circle query circle
     * @param rect axis-aligned rectangle
     * @return {@code true} when overlapping
     */
    public static boolean intersects(Circle2f circle, RectF rect) {
        return circle.overlaps(rect);
    }

    /**
     * Returns whether two axis-aligned boxes overlap.
     *
     * @param a first box
     * @param b second box
     * @return {@code true} when overlapping
     */
    public static boolean intersects(Aabb2f a, Aabb2f b) {
        return a.overlaps(b);
    }

    /**
     * Returns whether {@code point} lies inside the triangle {@code a, b, c} (inclusive edges).
     *
     * @param point query point
     * @param a first vertex
     * @param b second vertex
     * @param c third vertex
     * @return {@code true} when inside
     */
    public static boolean pointInTriangle(Vector2f point, Vector2f a, Vector2f b, Vector2f c) {
        float area = edge(a, b, c);
        if (area == 0f) {
            return false;
        }
        float w0 = edge(b, c, point) / area;
        float w1 = edge(c, a, point) / area;
        float w2 = edge(a, b, point) / area;
        return w0 >= 0f && w1 >= 0f && w2 >= 0f && w0 + w1 + w2 <= 1f + 1e-5f;
    }

    /**
     * Returns the closest point on segment {@code ab} to {@code point}.
     *
     * @param point query point
     * @param a segment start
     * @param b segment end
     * @return closest point
     */
    public static Vector2f closestPointOnSegment(Vector2f point, Vector2f a, Vector2f b) {
        return new Line2f(a.x, a.y, b.x, b.y).closestPoint(point);
    }

    /**
     * Tests whether two rays intersect and returns the parameter on {@code a} if they do.
     *
     * @param a first ray
     * @param b second ray
     * @param outTA optional length-1 array receiving t on {@code a}
     * @return {@code true} when rays intersect
     */
    public static boolean rayRay(Ray2f a, Ray2f b, float[] outTA) {
        float ox = a.origin().x;
        float oy = a.origin().y;
        float dx1 = a.direction().x;
        float dy1 = a.direction().y;
        float dx2 = b.direction().x;
        float dy2 = b.direction().y;
        float denom = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(denom) < 1e-8f) {
            return false;
        }
        float t = ((b.origin().x - ox) * dy2 - (b.origin().y - oy) * dx2) / denom;
        float u = ((b.origin().x - ox) * dy1 - (b.origin().y - oy) * dx1) / denom;
        if (t < 0f || u < 0f) {
            return false;
        }
        if (outTA != null && outTA.length > 0) {
            outTA[0] = t;
        }
        return true;
    }

    private static float edge(Vector2f a, Vector2f b, Vector2f p) {
        return (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
    }
}
