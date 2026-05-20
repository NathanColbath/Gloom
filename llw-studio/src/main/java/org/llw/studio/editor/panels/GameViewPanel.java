package org.llw.studio.editor.panels;

import imgui.ImGui;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.render.SceneDrawPass;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
import org.llw.studio.render.TilemapDrawPass;
import org.llw.studio.render.UiDrawPass;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.PlayCameraBridge;

/**
 * Play-mode game camera preview rendered to an offscreen texture.
 */
public final class GameViewPanel implements EditorPanel {
  private final OpenGlBackend backend;
  private final AssetDatabase assets;
  private final EditorSession session;
  private final ShaderGraphProgramCache shaderGraphs;
  private OffscreenTarget target;
  private int lastWidth = 640;
  private int lastHeight = 360;

  /**
   * @param backend OpenGL backend for offscreen targets
   * @param assets  assets for scene draw
   * @param session session (focus, viewport size for scripts)
   */
  public GameViewPanel(OpenGlBackend backend, AssetDatabase assets, EditorSession session, ShaderGraphProgramCache shaderGraphs) {
    this.backend = backend;
    this.assets = assets;
    this.session = session;
    this.shaderGraphs = shaderGraphs;
    target = new OffscreenTarget(backend, new IntSize(lastWidth, lastHeight));
  }

  /** {@inheritDoc} */
  @Override
  public String id() {
    return "game";
  }

  /** {@inheritDoc} */
  @Override
  public String title() {
    return "Game";
  }

  /** {@inheritDoc} */
  @Override
  public void render(StudioContext context) {
    if (session.consumeFocusGameView()) {
      ImGui.setNextWindowFocus();
    }
    if (!ImGui.begin(title())) {
      ImGui.end();
      return;
    }
    session.setGameViewFocused(ImGui.isWindowFocused());
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
      session.setGameViewSize(w, h);
      PlayUiInputBridge.setViewportSize(w, h);
      if (context.isPlaying() && context.playScene() != null) {
      Scene scene = context.playScene();
      float contentX = ImGui.getCursorScreenPosX();
      float contentY = ImGui.getCursorScreenPosY();
      PlayCameraBridge.syncScene(scene, w, h);
      PlayCameraBridge.setViewportScreenRect(contentX, contentY);
      PlayCameraBridge.applyTo(target.getCamera());
      target.clear(new Color(
              Math.round(PlayCameraBridge.backgroundR() * 255f),
              Math.round(PlayCameraBridge.backgroundG() * 255f),
              Math.round(PlayCameraBridge.backgroundB() * 255f),
              Math.round(PlayCameraBridge.backgroundA() * 255f)
      ));
      TilemapDrawPass.draw(scene, target, assets);
      SceneDrawPass.draw(scene, target, assets, shaderGraphs);
      target.flush();
      UiDrawPass.draw(scene, target, assets.uiFontCache(), w, h);
      target.flush();
      Texture2d texture = target.colorTexture();
      ImGui.image(texture.id(), width, height, 0f, 1f, 1f, 0f);
    } else {
      float textW = ImGui.calcTextSize("Enter Play mode to preview").x;
      ImGui.setCursorPosX((width - textW) * 0.5f);
      ImGui.setCursorPosY(height * 0.5f);
      EditorStyle.pushMutedText();
      ImGui.text("Enter Play mode to preview");
      EditorStyle.popMutedText();
    }
    ImGui.end();
  }

  /** Releases the offscreen render target. */
  public void dispose() {
    target.dispose();
  }
}
