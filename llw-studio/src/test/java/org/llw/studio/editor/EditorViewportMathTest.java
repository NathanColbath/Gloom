package org.llw.studio.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorViewportMathTest {
    @Test
    void gridStepScalesInverselyWithZoom() {
        float zoomedOut = EditorViewportMath.chooseGridStep(0.25f);
        float zoomedIn = EditorViewportMath.chooseGridStep(2f);
        assertTrue(zoomedOut > zoomedIn);
        assertTrue(screenSpacingPixels(0.25f, zoomedOut) >= 32f);
        assertTrue(screenSpacingPixels(0.25f, zoomedOut) <= 128f);
        assertTrue(screenSpacingPixels(2f, zoomedIn) >= 32f);
        assertTrue(screenSpacingPixels(2f, zoomedIn) <= 128f);
    }

    private static float screenSpacingPixels(float zoom, float worldStep) {
        return worldStep * zoom;
    }

    @Test
    void pixelsToWorldMatchesZoom() {
        assertEquals(2f, EditorViewportMath.pixelsToWorld(1f, 2f), 0.001f);
        assertEquals(1f, EditorViewportMath.pixelsToWorld(2f, 2f), 0.001f);
    }

    @Test
    void snapWorldXAlignsToPixelGrid() {
        EditorCamera camera = new EditorCamera();
        camera.pan(0f, 0f);
        int viewWidth = 800;
        float left = EditorViewportMath.worldLeft(camera, viewWidth);
        float snapped = EditorViewportMath.snapWorldX(camera, viewWidth, left + 10.3f);
        float screen = (snapped - left) / EditorViewportMath.worldWidth(camera, viewWidth) * viewWidth;
        assertEquals(Math.round(screen), screen, 0.001f);
    }
}
