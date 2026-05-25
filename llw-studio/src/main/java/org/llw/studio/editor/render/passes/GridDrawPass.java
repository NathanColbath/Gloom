package org.llw.studio.editor.render.passes;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.render.EditorRenderLayers;

/**
 * Draws a world-aligned editor grid snapped to pixel boundaries in the scene view.
 *
 * <p><b>Coordinates (Y-down):</b> vertical lines vary {@code worldX}; horizontal lines vary
 * {@code worldY}. The visible region is derived from {@link EditorViewportMath} using the
 * editor camera center and viewport size in pixels.
 */
public final class GridDrawPass {
    private static final DrawState GRID_STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.GRID);

    private GridDrawPass() {
    }

    /**
     * @param target      offscreen scene-view target
     * @param camera      pan/zoom editor camera
     * @param viewWidth   viewport width in pixels
     * @param viewHeight  viewport height in pixels
     */
    public static void draw(OffscreenTarget target, EditorCamera camera, int viewWidth, int viewHeight) {
        float zoom = camera.zoom();
        // Step widens as zoom out so grid lines stay roughly one screen pixel apart.
        float step = EditorViewportMath.chooseGridStep(zoom);
        float lineThickness = EditorViewportMath.pixelsToWorld(zoom, 1f);
        Color line = new Color(60, 60, 60, 255);

        float left = EditorViewportMath.worldLeft(camera, viewWidth);
        float right = left + EditorViewportMath.worldWidth(camera, viewWidth);
        float top = EditorViewportMath.worldTop(camera, viewHeight);
        float bottom = top + EditorViewportMath.worldHeight(camera, viewHeight);

        float startX = (float) (Math.floor(left / step) * step);
        for (float x = startX; x <= right + step * 0.001f; x += step) {
            float snapped = EditorViewportMath.snapWorldX(camera, viewWidth, x);
            float barX = snapped - lineThickness * 0.5f;
            Rectangle lineRect = new Rectangle();
            lineRect.setPosition(barX, top);
            lineRect.setSize(lineThickness, bottom - top);
            lineRect.setFillColor(line);
            target.draw(lineRect, GRID_STATE);
        }

        float startY = (float) (Math.floor(top / step) * step);
        for (float y = startY; y <= bottom + step * 0.001f; y += step) {
            float snapped = EditorViewportMath.snapWorldY(camera, viewHeight, y);
            float barY = snapped - lineThickness * 0.5f;
            Rectangle lineRect = new Rectangle();
            lineRect.setPosition(left, barY);
            lineRect.setSize(right - left, lineThickness);
            lineRect.setFillColor(line);
            target.draw(lineRect, GRID_STATE);
        }
    }
}

