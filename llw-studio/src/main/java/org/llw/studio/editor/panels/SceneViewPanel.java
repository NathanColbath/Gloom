package org.llw.studio.editor.panels;



import imgui.ImVec2;

import imgui.ImGui;

import imgui.flag.ImGuiMouseButton;

import org.llw.render.core.Color;

import org.llw.render.core.IntSize;

import org.llw.render.gl.OpenGlBackend;

import org.llw.render.graphics.OffscreenTarget;

import org.llw.render.graphics.Texture2d;

import org.llw.studio.assets.AssetDatabase;

import org.llw.studio.editor.prefab.PrefabEditorActions;

import org.llw.studio.editor.EditorCamera;

import org.llw.studio.editor.EditorSession;

import org.llw.studio.editor.SceneViewInput;

import org.llw.studio.editor.SelectionService;

import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.ThemeColors;

import org.llw.studio.editor.commands.UndoStack;

import org.llw.studio.editor.gizmo.GizmoController;

import org.llw.studio.editor.render.EditorSceneViewportPipeline;

import org.llw.studio.editor.theme.EditorColors;

import org.llw.studio.render.SceneBounds;

import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

import org.llw.studio.render.UiDrawPass;

import org.llw.studio.scene.Scene;



/**

 * Offscreen-rendered scene view with editor camera, grid, gizmos, and picking.

 *

 * @see EditorSceneViewportPipeline

 */

public final class SceneViewPanel implements EditorPanel {

  private final OpenGlBackend backend;

  private final AssetDatabase assets;

  private final SelectionService selection;

  private final UndoStack undoStack;

  private final EditorSession session;

  private final ShaderGraphProgramCache shaderGraphs;

  private final EditorCamera camera = new EditorCamera();

  private final GizmoController gizmoController = new GizmoController();

  private final SceneViewInput viewInput;

  private OffscreenTarget target;

  private int lastWidth = 640;

  private int lastHeight = 360;

  private boolean pendingFrame = true;

  private Scene pendingFrameScene;



  public SceneViewPanel(

      OpenGlBackend backend,

      AssetDatabase assets,

      SelectionService selection,

      UndoStack undoStack,

      EditorSession session,

      ShaderGraphProgramCache shaderGraphs

  ) {

    this.backend = backend;

    this.assets = assets;

    this.selection = selection;

    this.undoStack = undoStack;

    this.session = session;

    this.shaderGraphs = shaderGraphs;

    this.viewInput = new SceneViewInput(selection, undoStack, assets, session.toolState(), gizmoController, camera, session);

    target = new OffscreenTarget(backend, new IntSize(lastWidth, lastHeight));

  }



  @Override

  public String id() {

    return "scene";

  }



  @Override

  public String title() {

    return "Scene";

  }



  @Override

  public void render(StudioContext context) {

    if (session.consumeFocusSceneView()) {

      ImGui.setNextWindowFocus();

    }

    if (!ImGui.begin(title())) {

      ImGui.end();

      return;

    }



    Scene sceneToRender = context.activeScene();

    // Deferred frame-to-fit (e.g. after "Frame selected") uses last known panel size.
    if (pendingFrame) {

      if (pendingFrameScene == null) {

        pendingFrameScene = sceneToRender;

      }

      applyPendingFrame(lastWidth, lastHeight);

    }



    float width = ImGui.getContentRegionAvailX();

    float height = ImGui.getContentRegionAvailY();

    int w = Math.max(1, (int) width);

    int h = Math.max(1, (int) height);

    if (w != lastWidth || h != lastHeight) {

      // FBO must match ImGui content size or picking and image UVs drift.
      target.dispose();

      target = new OffscreenTarget(backend, new IntSize(w, h));

      lastWidth = w;

      lastHeight = h;

    }



    boolean viewportHovered = false;

    float imageX = 0f;

    float imageY = 0f;



    if (ImGui.isWindowHovered() && !gizmoController.isDragging() && ImGui.isMouseDragging(ImGuiMouseButton.Middle)) {

      ImVec2 delta = ImGui.getMouseDragDelta(ImGuiMouseButton.Middle);

      camera.pan(-delta.x, -delta.y);

      ImGui.resetMouseDragDelta(ImGuiMouseButton.Middle);

    }

    if (ImGui.isWindowHovered() && !gizmoController.isDragging()) {

      float wheel = ImGui.getIO().getMouseWheel();

      if (wheel != 0f) {

        camera.zoomBy(wheel > 0 ? 1.1f : 0.9f);

      }

    }



    camera.applyTo(target.getCamera(), w, h);

    target.clear(new Color(EditorColors.VIEWPORT_BG_R, EditorColors.VIEWPORT_BG_G, EditorColors.VIEWPORT_BG_B, 255));

    var activeTilemap = viewInput.tilemapPaint().activeTilemapEntity(sceneToRender);

    // World + edit overlays into FBO; UI pass and ImGui.image happen below.
    EditorSceneViewportPipeline.draw(

            sceneToRender,

            context.editScene(),

            target,

            camera,

            w,

            h,

            assets,

            shaderGraphs,

            selection,

            session,

            context.isPlaying(),

            activeTilemap,

            session.toolState(),

            gizmoController

    );

    if (context.isPlaying()) {

      float labelX = ImGui.getCursorScreenPosX() + 8f;

      float labelY = ImGui.getCursorScreenPosY() - height + 8f;

      ImGui.getWindowDrawList().addText(labelX, labelY, ThemeColors.toU32(EditorColors.PLAY_ACTIVE), "Playing");

    }

    target.flush();

    // Screen-space UI is drawn after world flush so it layers above scene content in the FBO.
    UiDrawPass.draw(sceneToRender, target, assets.uiFontCache(), w, h);

    target.flush();



    Texture2d texture = target.colorTexture();

    ImGui.image(texture.id(), width, height, 0f, 1f, 1f, 0f);

    viewportHovered = ImGui.isItemHovered();

    imageX = ImGui.getItemRectMinX();

    imageY = ImGui.getItemRectMinY();

    if (!context.isPlaying() && ImGui.beginDragDropTarget()) {

      String guid = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);

      if (guid != null) {

        float mouseX = ImGui.getMousePosX() - imageX;

        float mouseY = ImGui.getMousePosY() - imageY;

        var world = camera.screenToWorld(mouseX, mouseY, w, h);

        PrefabEditorActions.tryInstantiatePrefab(

                context, assets, selection, guid, null, world.x, world.y, true, false);

      }

      ImGui.endDragDropTarget();

    }

    // Mouse coords for picking/gizmos are relative to the ImGui image item, not the window.
    viewInput.setViewportRect(imageX, imageY, width, height, viewportHovered);

    viewInput.handle(sceneToRender, target, w, h, context.isPlaying());



    ImGui.end();

  }



  public void frameScene(Scene scene) {

    pendingFrameScene = scene;

    pendingFrame = true;

  }



  private void applyPendingFrame(int viewWidth, int viewHeight) {

    pendingFrame = false;

    Scene scene = pendingFrameScene;

    if (scene == null) {

      camera.frameBounds(0f, 0f, 0f, 0f, viewWidth, viewHeight);

      return;

    }

    SceneBounds.Bounds bounds = SceneBounds.compute(scene, assets);

    if (bounds.empty) {

      camera.frameBounds(0f, 0f, 0f, 0f, viewWidth, viewHeight);

    } else {

      camera.frameBounds(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, viewWidth, viewHeight);

    }

  }



  public void dispose() {

    target.dispose();

  }

}

