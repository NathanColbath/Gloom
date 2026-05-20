package org.llw.studio.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.studio.camera.CameraViewBounds;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.EditorViewportMath;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.render.EditorRenderLayers;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

/**
 * Draws orthographic camera frusta and a small camera icon in the scene view.
 *
 * <p><b>Coordinates (Y-down):</b> frustum corners use {@link CameraViewBounds} world space;
 * borders are snapped to pixels via {@link EditorViewportMath}.
 */
public final class CameraGizmoDrawPass {
    private static final Color FRAME_MAIN = new Color(255, 193, 7, 90);
    private static final Color FRAME_SELECTED = new Color(255, 193, 7, 220);
    private static final Color ICON = new Color(255, 193, 7, 220);
    private static final float ICON_HALF_PIXELS = 20f;
    private static final float LINE_PIXELS = 2f;
    private static final DrawState STATE = DrawState.DEFAULT.withLayer(EditorRenderLayers.CAMERA_GIZMO);

    private CameraGizmoDrawPass() {
    }

    /**
     * @param scene            scene containing cameras
     * @param target           offscreen scene-view target
     * @param selection        highlights the selected camera frustum
     * @param editorCamera     pan/zoom editor camera
     * @param viewWidth        scene-view width in pixels
     * @param viewHeight       scene-view height in pixels
     * @param gameViewWidth    game-view width used for camera aspect
     * @param gameViewHeight   game-view height used for camera aspect
     */
    public static void draw(
            Scene scene,
            OffscreenTarget target,
            SelectionService selection,
            EditorCamera editorCamera,
            int viewWidth,
            int viewHeight,
            int gameViewWidth,
            int gameViewHeight
    ) {
        float line = EditorViewportMath.pixelsToWorld(editorCamera.zoom(), LINE_PIXELS);
        float iconHalf = EditorViewportMath.pixelsToWorld(editorCamera.zoom(), ICON_HALF_PIXELS);

        new TransformSystem().onUpdate(scene.world(), 0f);
        float aspect = CameraViewBounds.aspectFromViewport(gameViewWidth, gameViewHeight);
        ComponentStore<Camera2DComponent> cameras = scene.world().store(Camera2DComponent.class);
        for (int i = 0; i < cameras.size(); i++) {
            EntityId entity = cameras.entityAt(i);
            Camera2DComponent camera = cameras.componentAt(i);
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            float centerX = world != null ? world.worldX : local != null ? local.x : 0f;
            float centerY = world != null ? world.worldY : local != null ? local.y : 0f;
            CameraViewBounds bounds = CameraViewBounds.fromCenter(centerX, centerY, camera.orthographicSize, aspect);
            boolean selected = selection.isSelected(entity);
            if (camera.mainCamera || selected) {
                Color frameColor = selected ? FRAME_SELECTED : FRAME_MAIN;
                drawSnappedBorder(target, editorCamera, viewWidth, viewHeight, bounds, line, frameColor);
            }
            drawCameraIcon(target, centerX, centerY, iconHalf, line);
        }
    }

    private static void drawSnappedBorder(
            OffscreenTarget target,
            EditorCamera editorCamera,
            int viewWidth,
            int viewHeight,
            CameraViewBounds bounds,
            float line,
            Color color
    ) {
        float minX = EditorViewportMath.snapWorldX(editorCamera, viewWidth, bounds.minX);
        float minY = EditorViewportMath.snapWorldY(editorCamera, viewHeight, bounds.minY);
        float maxX = EditorViewportMath.snapWorldX(editorCamera, viewWidth, bounds.maxX);
        float maxY = EditorViewportMath.snapWorldY(editorCamera, viewHeight, bounds.maxY);
        if (maxX <= minX) {
            maxX = minX + line;
        }
        if (maxY <= minY) {
            maxY = minY + line;
        }
        drawBorder(target, minX, minY, maxX - minX, maxY - minY, line, color);
    }

    private static void drawCameraIcon(
            OffscreenTarget target,
            float centerX,
            float centerY,
            float iconHalf,
            float line
    ) {
        float bodyLeft = centerX - iconHalf;
        float bodyTop = centerY - iconHalf * 0.6f;
        float bodyWidth = iconHalf * 1.4f;
        float bodyHeight = iconHalf * 1.2f;
        drawBorder(target, bodyLeft, bodyTop, bodyWidth, bodyHeight, line, ICON);
        drawBar(
                target,
                centerX + iconHalf * 0.2f,
                centerY - iconHalf * 0.25f,
                iconHalf * 0.9f,
                line,
                ICON
        );
    }

    private static void drawBorder(OffscreenTarget target, float x, float y, float width, float height, float line, Color color) {
        drawBar(target, x, y, width, line, color);
        drawBar(target, x, y + height - line, width, line, color);
        drawBar(target, x, y, line, height, color);
        drawBar(target, x + width - line, y, line, height, color);
    }

    private static void drawBar(OffscreenTarget target, float x, float y, float width, float height, Color color) {
        Rectangle bar = new Rectangle();
        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setFillColor(color);
        target.draw(bar, STATE);
    }
}
