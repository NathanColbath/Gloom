package org.llw.studio.render;

import org.junit.jupiter.api.Test;
import org.llw.math.matrix.Matrix3x2;
import org.llw.math.transform.Transform2f;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpritePlacementTest {
    @Test
    void centeredPivotMapsToTransformPosition() {
        float originX = 32f;
        float originY = 32f;
        float centerX = 100f;
        float centerY = 120f;

        Transform2f transform = new Transform2f();
        transform.setOrigin(originX, originY);
        transform.setPosition(centerX - originX, centerY - originY);

        Matrix3x2 matrix = transform.toMatrix();
        float[] m = matrix.elements();
        float worldX = m[0] * originX + m[4] * originY + m[12];
        float worldY = m[1] * originX + m[5] * originY + m[13];
        assertEquals(centerX, worldX, 0.01f);
        assertEquals(centerY, worldY, 0.01f);
    }
}
