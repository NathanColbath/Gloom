package org.llw.studio.editor;



import imgui.ImGui;

import imgui.flag.ImGuiMouseButton;

import org.llw.studio.assets.AssetDatabase;

import org.llw.studio.editor.commands.UndoStack;

import org.llw.studio.editor.gizmo.GizmoContext;

import org.llw.studio.editor.gizmo.GizmoController;
import org.llw.studio.editor.tilemap.TilemapPaintController;

import org.llw.render.graphics.OffscreenTarget;

import org.llw.studio.scene.Scene;



/**

 * Scene-view mouse handling: gizmo drag, picking, and play-mode selection.

 *

 * <p>Implementation note: Call {@link #setViewportRect(float, float, float, float, boolean)} then {@link #handle(Scene, OffscreenTarget, int, int, boolean)}

 * each ImGui frame after the scene image is drawn, while the viewport window is open.

 */

public final class SceneViewInput {

    private final SelectionService selection;

    private final UndoStack undoStack;

    private final AssetDatabase assets;

    private final SceneToolState toolState;

    private final GizmoController gizmoController;

    private final EditorCamera camera;

    private final EditorSession session;

    private final TilemapPaintController tilemapPaint;



    private boolean viewportHovered;

    private float viewportX;

    private float viewportY;

    private float viewportW;

    private float viewportH;



    /**

     * @param selection       entity selection

     * @param undoStack       undo for completed gizmo drags

     * @param assets          assets for picking bounds

     * @param toolState       active scene tool

     * @param gizmoController translate/rotate/scale controller

     * @param camera          editor camera for screen/world conversion

     * @param session         session (game view size for picking)

     */

    public SceneViewInput(

            SelectionService selection,

            UndoStack undoStack,

            AssetDatabase assets,

            SceneToolState toolState,

            GizmoController gizmoController,

            EditorCamera camera,

            EditorSession session

    ) {

        this.selection = selection;

        this.undoStack = undoStack;

        this.assets = assets;

        this.toolState = toolState;

        this.gizmoController = gizmoController;

        this.camera = camera;

        this.session = session;

        this.tilemapPaint = new TilemapPaintController(selection, undoStack, session, toolState);

    }

    /** @return tile paint controller for grid overlay wiring */
    public TilemapPaintController tilemapPaint() {
        return tilemapPaint;
    }



    /**

     * Updates the scene image rectangle in screen space (ImGui item rect).

     *

     * @param x       left of the rendered texture in screen pixels

     * @param y       top of the rendered texture in screen pixels

     * @param width   image width in screen pixels

     * @param height  image height in screen pixels

     * @param hovered whether the image item is hovered

     */

    public void setViewportRect(float x, float y, float width, float height, boolean hovered) {

        viewportX = x;

        viewportY = y;

        viewportW = width;

        viewportH = height;

        viewportHovered = hovered;

    }



    /**

     * Processes hover, gizmo drag, and pick for the current frame.

     *

     * @param scene      scene being edited or played

     * @param target     offscreen target (render camera)

     * @param viewWidth  viewport width in screen pixels

     * @param viewHeight viewport height in screen pixels

     * @param playing    when true, only picking runs (no gizmo edit)

     * <p>Implementation note: Uses ImGui mouse state; must run inside the scene view panel's {@code ImGui.begin} scope.

     */

    public void handle(Scene scene, OffscreenTarget target, int viewWidth, int viewHeight, boolean playing) {

        if (!viewportHovered) {

            return;

        }

        float mouseX = ImGui.getMousePosX() - viewportX;

        float mouseY = ImGui.getMousePosY() - viewportY;

        GizmoContext context = new GizmoContext(camera, target.getCamera(), viewWidth, viewHeight);



        tilemapPaint.updateActiveTilemap(scene);

        if (!playing) {

            if (toolState.mode() == SceneToolMode.TILE_PAINT || toolState.mode() == SceneToolMode.TILE_ERASE) {
                if (tilemapPaint.handle(scene, context, mouseX, mouseY)) {
                    return;
                }
            }

            if (!gizmoController.isDragging()) {

                gizmoController.updateHover(scene, toolState, selection, context, mouseX, mouseY);

            }



            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {

                if (toolState.mode() != SceneToolMode.HAND
                        && toolState.mode() != SceneToolMode.TILE_PAINT
                        && toolState.mode() != SceneToolMode.TILE_ERASE
                        && gizmoController.beginDrag(scene, toolState, selection, context, mouseX, mouseY)) {

                    return;

                }

                if (toolState.mode() != SceneToolMode.TILE_PAINT && toolState.mode() != SceneToolMode.TILE_ERASE) {
                    pick(scene, context);
                }

            }



            if (gizmoController.isDragging() && ImGui.isMouseDragging(ImGuiMouseButton.Left)) {

                gizmoController.updateDrag(scene, toolState, context, mouseX, mouseY);

            }



            if (gizmoController.isDragging() && ImGui.isMouseReleased(ImGuiMouseButton.Left)) {

                gizmoController.endDrag(scene, undoStack);

            }

            return;

        }



        if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {

            pick(scene, context);

        }

    }



    private void pick(Scene scene, GizmoContext context) {

        var world = context.screenToWorld(

                ImGui.getMousePosX() - viewportX,

                ImGui.getMousePosY() - viewportY

        );

        var picked = ScenePicker.pick(

                scene,

                assets,

                world.x,

                world.y,

                session.gameViewWidth(),

                session.gameViewHeight()

        );

        if (!picked.isNone()) {

            selection.select(picked);

        }

    }

}

