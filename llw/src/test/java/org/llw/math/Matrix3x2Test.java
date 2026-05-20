package org.llw.math;

import org.llw.math.matrix.Matrix3x2;
import org.llw.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Matrix3x2Test {

    @Test
    void orthoMapsTopLeftToClipTop() {
        Matrix3x2 m = Matrix3x2.ortho(0f, 100f, 0f, 100f);
        float[] e = m.elements();
        assertEquals(0.02f, e[0], 1e-5f);
        assertEquals(-0.02f, e[5], 1e-5f);
        assertEquals(-1f, e[12], 1e-5f);
        assertEquals(1f, e[13], 1e-5f);
    }

    @Test
    void fromTransformTranslates() {
        Matrix3x2 m = Matrix3x2.fromTransform(new Vector2f(10f, 20f), 0f, new Vector2f(1f, 1f), new Vector2f());
        assertEquals(10f, m.elements()[12], 1e-5f);
        assertEquals(20f, m.elements()[13], 1e-5f);
    }
}
