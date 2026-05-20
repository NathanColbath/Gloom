package org.llw.math;

import org.llw.math.collision.Intersection2;
import org.llw.math.collision.Sat2f;
import org.llw.math.geometry.Circle2f;
import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollisionTest {

    @Test
    void circleRectOverlap() {
        Circle2f circle = new Circle2f(50f, 50f, 20f);
        RectF rect = new RectF(60f, 60f, 40f, 40f);
        assertTrue(Intersection2.intersects(circle, rect));
    }

    @Test
    void pointInTriangle() {
        Vector2f a = new Vector2f(0f, 0f);
        Vector2f b = new Vector2f(100f, 0f);
        Vector2f c = new Vector2f(50f, 100f);
        assertTrue(Intersection2.pointInTriangle(new Vector2f(50f, 30f), a, b, c));
        assertFalse(Intersection2.pointInTriangle(new Vector2f(200f, 200f), a, b, c));
    }

    @Test
    void satSquareOverlap() {
        Vector2f[] a = {
                new Vector2f(0f, 0f), new Vector2f(40f, 0f),
                new Vector2f(40f, 40f), new Vector2f(0f, 40f)
        };
        Vector2f[] b = {
                new Vector2f(20f, 20f), new Vector2f(60f, 20f),
                new Vector2f(60f, 60f), new Vector2f(20f, 60f)
        };
        assertTrue(Sat2f.intersects(a, b));
    }
}
