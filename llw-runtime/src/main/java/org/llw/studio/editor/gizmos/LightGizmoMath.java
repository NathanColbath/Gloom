package org.llw.studio.editor.gizmos;

import org.llw.render.core.Color;
import org.llw.render.graphics.Vertex;

/**
 * Shared 2D light gizmo geometry aligned with {@link org.llw.studio.lighting.LightingSystem}.
 */
public final class LightGizmoMath {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private LightGizmoMath() {
    }

    /**
     * @param rotationDegrees entity rotation in degrees
     * @return unit direction (cos, sin) using {@code rotation - 90°}
     */
    public static float[] directionFromRotation(float rotationDegrees) {
        float rad = (rotationDegrees - 90f) * DEG_TO_RAD;
        return new float[]{(float) Math.cos(rad), (float) Math.sin(rad)};
    }

    /**
     * Builds a spot-light cone outline as a triangle fan from the apex.
     *
     * @param apexX           light world X
     * @param apexY           light world Y
     * @param dirX            normalized direction X
     * @param dirY            normalized direction Y
     * @param range           cone length in world units
     * @param halfAngleDeg    half of the full cone angle in degrees
     * @param segments        arc segment count
     * @param color           vertex color
     * @return vertices for {@link org.llw.render.graphics.PrimitiveType#TRIANGLE_FAN}
     */
    public static Vertex[] coneOutline(
            float apexX,
            float apexY,
            float dirX,
            float dirY,
            float range,
            float halfAngleDeg,
            int segments,
            Color color
    ) {
        int count = Math.max(3, segments);
        float baseAngle = (float) Math.atan2(dirY, dirX);
        float halfRad = halfAngleDeg * DEG_TO_RAD;
        Vertex[] vertices = new Vertex[count + 2];
        vertices[0] = new Vertex(apexX, apexY, 0f, 0f, color);
        for (int i = 0; i <= count; i++) {
            float t = (float) i / count;
            float angle = baseAngle - halfRad + t * (halfRad * 2f);
            float x = apexX + (float) Math.cos(angle) * range;
            float y = apexY + (float) Math.sin(angle) * range;
            vertices[i + 1] = new Vertex(x, y, 0f, 0f, color);
        }
        return vertices;
    }

    /**
     * Builds an arc polyline (line strip) for a partial circle gizmo.
     */
    public static Vertex[] arcLineStrip(
            float centerX,
            float centerY,
            float radiusX,
            float radiusY,
            float startAngleRad,
            float arcRadians,
            int segments,
            Color color
    ) {
        int count = Math.max(2, segments);
        Vertex[] vertices = new Vertex[count + 1];
        for (int i = 0; i <= count; i++) {
            float t = (float) i / count;
            float angle = startAngleRad + arcRadians * t;
            float x = centerX + (float) Math.cos(angle) * radiusX;
            float y = centerY + (float) Math.sin(angle) * radiusY;
            vertices[i] = new Vertex(x, y, 0f, 0f, color);
        }
        return vertices;
    }
}
