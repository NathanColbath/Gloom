package org.llw.studio.render;

import org.junit.jupiter.api.Test;
import org.llw.math.matrix.Matrix3x2;
import org.llw.math.transform.Transform2f;
import org.llw.studio.assets.SpriteDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpritePlacementSliceTest {
    @Test
    void centeredPivotUsesSliceDimensions() {
        SpriteDefinition slice = new SpriteDefinition(
                "g",
                "s",
                "tex",
                32,
                32,
                16,
                24,
                0.5f,
                0.5f,
                128,
                128
        );
        float originX = slice.width() * 0.5f;
        float originY = slice.height() * 0.5f;
        float centerX = 50f;
        float centerY = 60f;

        Transform2f transform = new Transform2f();
        transform.setOrigin(originX, originY);
        transform.setPosition(centerX - originX, centerY - originY);

        Matrix3x2 matrix = transform.toMatrix();
        float[] m = matrix.elements();
        float worldX = m[0] * originX + m[4] * originY + m[12];
        float worldY = m[1] * originX + m[5] * originY + m[13];
        assertEquals(centerX, worldX, 0.01f);
        assertEquals(centerY, worldY, 0.01f);
        assertEquals(8f, originX, 0.01f);
        assertEquals(12f, originY, 0.01f);
    }

    @Test
    void rotationDegreesConvertToRendererRadians() {
        assertEquals((float) Math.toRadians(90f), SpritePlacement.rotationDegreesToRadians(90f), 0.0001f);
        assertEquals(0f, SpritePlacement.rotationDegreesToRadians(0f), 0.0001f);
    }
}
