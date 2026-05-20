package org.llw.studio.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

/**
 * Draws pixel-snapped selection outlines around {@link EntityBounds} for each selected entity.
 *
 * <p><b>Coordinates (Y-down):</b> outline corners are snapped in world space before drawing
 * axis-aligned bars in the scene view.
 */
public final class SelectionOutlinePass {
    private static final Color OUTLINE = new Color(66, 150, 250, 255);
    private static final float OUTLINE_PIXELS = 2f;
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.SELECTION);

    private SelectionOutlinePass() {
    }

    /**
     * @param scene      scene being edited
     * @param target     offscreen scene-view target
     * @param assets     texture lookup for sprite bounds
     * @param selection  entities to outline
     * @param camera     pan/zoom editor camera
     * @param viewWidth  viewport width in pixels
     * @param viewHeight viewport height in pixels
     */
    public static void draw(
            Scene scene,
            OffscreenTarget target,
            AssetDatabase assets,
            SelectionService selection,
            EditorCamera camera,
            int viewWidth,
            int viewHeight
    ) {
        float thickness = EditorViewportMath.pixelsToWorld(camera.zoom(), OUTLINE_PIXELS);
        new TransformSystem().onUpdate(scene.world(), 0f);
        for (EntityId entity : selection.allSelected()) {
            if (!scene.world().isAlive(entity)) {
                continue;
            }
            EntityBounds bounds = EntityBounds.forEntity(scene.world(), entity, assets);
            drawSnappedBorder(target, camera, viewWidth, viewHeight, bounds, thickness);
        }
    }

    private static void drawSnappedBorder(
            OffscreenTarget target,
            EditorCamera camera,
            int viewWidth,
            int viewHeight,
            EntityBounds bounds,
            float thickness
    ) {
        float minX = EditorViewportMath.snapWorldX(camera, viewWidth, bounds.minX);
        float minY = EditorViewportMath.snapWorldY(camera, viewHeight, bounds.minY);
        float maxX = EditorViewportMath.snapWorldX(camera, viewWidth, bounds.maxX);
        float maxY = EditorViewportMath.snapWorldY(camera, viewHeight, bounds.maxY);
        if (maxX <= minX) {
            maxX = minX + thickness;
        }
        if (maxY <= minY) {
            maxY = minY + thickness;
        }
        drawBorder(target, minX, minY, maxX - minX, maxY - minY, thickness);
    }

    private static void drawBorder(OffscreenTarget target, float x, float y, float width, float height, float thickness) {
        drawBar(target, x, y, width, thickness);
        drawBar(target, x, y + height - thickness, width, thickness);
        drawBar(target, x, y, thickness, height);
        drawBar(target, x + width - thickness, y, thickness, height);
    }

    private static void drawBar(OffscreenTarget target, float x, float y, float width, float height) {
        Rectangle bar = new Rectangle();
        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setFillColor(OUTLINE);
        target.draw(bar, STATE);
    }
}
