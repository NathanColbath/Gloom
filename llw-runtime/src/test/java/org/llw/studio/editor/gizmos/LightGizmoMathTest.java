package org.llw.studio.editor.gizmos;

import org.junit.jupiter.api.Test;
import org.llw.render.core.Color;
import org.llw.render.graphics.Vertex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightGizmoMathTest {
    @Test
    void directionFromRotationUsesMinusNinetyOffset() {
        float[] dir = LightGizmoMath.directionFromRotation(90f);
        assertEquals(0f, dir[0], 0.001f);
        assertEquals(-1f, dir[1], 0.001f);
    }

    @Test
    void coneOutlineIncludesApexAndArc() {
        Vertex[] vertices = LightGizmoMath.coneOutline(0f, 0f, 0f, 1f, 100f, 45f, 8, Color.WHITE);
        assertTrue(vertices.length >= 4);
        assertEquals(0f, vertices[0].position.x, 0.001f);
        assertEquals(0f, vertices[0].position.y, 0.001f);
    }
}
