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
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.gizmo.GizmoController;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.render.CameraGizmoDrawPass;
import org.llw.studio.render.PhysicsGizmoDrawPass;
import org.llw.studio.render.GizmoDrawPass;
import org.llw.studio.render.GridDrawPass;
import org.llw.studio.render.SceneBounds;
import org.llw.studio.render.SceneDrawPass;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
import org.llw.studio.render.UiDrawPass;
import org.llw.studio.render.TilemapDrawPass;
import org.llw.studio.render.TilemapGridDrawPass;
import org.llw.studio.render.SelectionOutlinePass;
import org.llw.studio.scene.Scene;

/**
 * Offscreen-rendered scene view with editor camera, grid, gizmos, and picking.
 *
 * <p>Implementation note: {@link #render(StudioContext)} runs inside the shell frame after {@code ImGui.newFrame};
 * viewport input is delegated to {@link SceneViewInput} after the scene texture is drawn.
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

  /**
   * @param backend   OpenGL backend for offscreen targets
   * @param assets    assets for scene draw and picking
   * @param selection entity selection
   * @param undoStack undo for gizmo edits
   * @param session   editor session (tools, game view size)
   */
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

  /** {@inheritDoc} */
  @Override
  public String id() {
    return "scene";
  }

  /** {@inheritDoc} */
  @Override
  public String title() {
    return "Scene";
  }

  /** {@inheritDoc} */
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
    GridDrawPass.draw(target, camera, w, h);
    var activeTilemap = viewInput.tilemapPaint().activeTilemapEntity(sceneToRender);
    if (!activeTilemap.isNone()) {
      TilemapGridDrawPass.draw(sceneToRender, target, camera, w, h, activeTilemap);
    }
    TilemapDrawPass.draw(sceneToRender, target, assets);
    SceneDrawPass.draw(sceneToRender, target, assets, shaderGraphs);
    if (!context.isPlaying()) {
      CameraGizmoDrawPass.draw(
              context.editScene(),
              target,
              selection,
              camera,
              w,
              h,
              session.gameViewWidth(),
              session.gameViewHeight()
      );
      PhysicsGizmoDrawPass.draw(context.editScene(), target, camera, w, h);
      SelectionOutlinePass.draw(sceneToRender, target, assets, selection, camera, w, h);
      GizmoDrawPass.draw(sceneToRender, target, camera, session.toolState(), selection, gizmoController, w, h);
    } else {
      SelectionOutlinePass.draw(sceneToRender, target, assets, selection, camera, w, h);
      float labelX = ImGui.getCursorScreenPosX() + 8f;
      float labelY = ImGui.getCursorScreenPosY() - height + 8f;
      ImGui.getWindowDrawList().addText(labelX, labelY, 0xFF78DC78, "Playing");
    }
    target.flush();
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
    viewInput.setViewportRect(imageX, imageY, width, height, viewportHovered);
    viewInput.handle(sceneToRender, target, w, h, context.isPlaying());

    ImGui.end();
  }

  /**
   * Queues a camera frame-to-fit on the next render using the given scene bounds.
   *
   * @param scene scene to measure (typically edit scene)
   */
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

  /** Releases the offscreen render target. */
  public void dispose() {
    target.dispose();
  }
}
