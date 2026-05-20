package org.llw.math;

import org.llw.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector2fTest {

    @Test
    void dotAndPerp() {
        Vector2f a = new Vector2f(3f, 4f);
        Vector2f b = new Vector2f(2f, 1f);
        assertEquals(10f, a.dot(b), 1e-5f);
        Vector2f perp = a.perp();
        assertEquals(-4f, perp.x, 1e-5f);
        assertEquals(3f, perp.y, 1e-5f);
    }

    @Test
    void lerpAndDistance() {
        Vector2f a = new Vector2f(0f, 0f);
        Vector2f b = new Vector2f(10f, 0f);
        Vector2f mid = Vector2f.lerp(a, b, 0.5f);
        assertEquals(5f, mid.x, 1e-5f);
        assertEquals(10f, a.distance(b), 1e-5f);
    }

    @Test
    void isApproxEqual() {
        assertTrue(new Vector2f(1f, 2f).isApproxEqual(new Vector2f(1.0001f, 2.0001f), 1e-3f));
    }
}
