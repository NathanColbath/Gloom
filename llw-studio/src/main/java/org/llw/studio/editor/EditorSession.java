package org.llw.studio.editor;

import org.llw.studio.editor.animation.AnimationEditorState;
import org.llw.studio.editor.panels.AnimationPanel;
import org.llw.studio.editor.panels.ParticlePanel;
import org.llw.studio.editor.panels.ShaderGraphPanel;
import org.llw.studio.editor.particles.ParticleEditorState;
import org.llw.studio.editor.tilemap.TilemapEditState;
import org.llw.studio.editor.ui.UiEditorState;
import org.llw.studio.materials.runtime.MaterialProgramCache;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.shadergraph.editor.ShaderGraphEditorState;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.JsScriptSystem;

import java.util.function.Consumer;

/**
 * Per-editor-session UI state: scene tools, play-mode script bridge, and view focus requests.
 */
public final class EditorSession {
  private final SceneToolState toolState = new SceneToolState();
  private Consumer<Scene> frameSceneCallback = scene -> {};
  private boolean gameViewFocused;
  private boolean focusGameViewNextFrame;
  private boolean focusSceneViewNextFrame;
  private int gameViewWidth = 640;
  private int gameViewHeight = 360;
  private JsScriptSystem playScriptSystem;
  private AnimationEditorState animationEditorState;
  private ShaderGraphEditorState shaderGraphEditorState;
  private ShaderGraphPanel shaderGraphPanel;
  private ParticlePanel particlePanel;
  private AnimationPanel animationPanel;
  private ParticleEditorState particleEditorState;
  private final ParticleWorld particleWorld = new ParticleWorld();
  private ParticleWorld playParticleWorld;
  private final TilemapEditState tilemapEditState = new TilemapEditState();
  private final UiEditorState uiEditorState = new UiEditorState();
  private MaterialProgramCache materialProgramCache;

  /** @return shared scene-tool state for toolbar and scene view */
  public SceneToolState toolState() {
    return toolState;
  }

  /**
   * Registers a callback invoked when the scene view should frame all content.
   *
   * @param callback receiver; null clears the callback
   */
  public void setFrameSceneCallback(Consumer<Scene> callback) {
    frameSceneCallback = callback == null ? scene -> {} : callback;
  }

  /**
   * @param scene scene passed to the frame callback
   */
  public void frameScene(Scene scene) {
    frameSceneCallback.accept(scene);
  }

  /** @return whether the game view ImGui window currently has focus */
  public boolean isGameViewFocused() {
    return gameViewFocused;
  }

  /**
   * @param gameViewFocused updated focus flag from {@link org.llw.studio.editor.panels.GameViewPanel}
   */
  public void setGameViewFocused(boolean gameViewFocused) {
    this.gameViewFocused = gameViewFocused;
  }

  /** Requests keyboard focus on the game view on the next frame. */
  public void requestFocusGameView() {
    focusGameViewNextFrame = true;
  }

  /**
   * @return true once if a focus request was pending; clears the request
   */
  public boolean consumeFocusGameView() {
    if (!focusGameViewNextFrame) {
      return false;
    }
    focusGameViewNextFrame = false;
    return true;
  }

  /** Requests keyboard focus on the scene view on the next frame. */
  public void requestFocusSceneView() {
    focusSceneViewNextFrame = true;
  }

  /**
   * @return true once if a focus request was pending; clears the request
   */
  public boolean consumeFocusSceneView() {
    if (!focusSceneViewNextFrame) {
      return false;
    }
    focusSceneViewNextFrame = false;
    return true;
  }

  /**
   * @param width  last rendered game view width in screen pixels
   * @param height last rendered game view height in screen pixels
   */
  public void setGameViewSize(int width, int height) {
    gameViewWidth = Math.max(1, width);
    gameViewHeight = Math.max(1, height);
  }

  /** @return cached game view width used for camera gizmo preview */
  public int gameViewWidth() {
    return gameViewWidth;
  }

  /** @return cached game view height used for camera gizmo preview */
  public int gameViewHeight() {
    return gameViewHeight;
  }

  /**
   * @param playScriptSystem script system active during play mode, or null when stopped
   */
  public void setPlayScriptSystem(JsScriptSystem playScriptSystem) {
    this.playScriptSystem = playScriptSystem;
  }

  /** @return play-mode script system, or null when not playing */
  public JsScriptSystem playScriptSystem() {
    return playScriptSystem;
  }

  public void setAnimationEditorState(AnimationEditorState animationEditorState) {
    this.animationEditorState = animationEditorState;
  }

  /** @return animation preview state for edit-mode scrubbing */
  public AnimationEditorState animationEditorState() {
    return animationEditorState;
  }

  /** @return tilemap paint tool state */
  public TilemapEditState tilemapEdit() {
    return tilemapEditState;
  }

  public void setShaderGraphEditorState(ShaderGraphEditorState shaderGraphEditorState) {
    this.shaderGraphEditorState = shaderGraphEditorState;
  }

  /** @return active shader graph editor document state */
  public ShaderGraphEditorState shaderGraphEditorState() {
    return shaderGraphEditorState;
  }

  public void setShaderGraphPanel(ShaderGraphPanel shaderGraphPanel) {
    this.shaderGraphPanel = shaderGraphPanel;
  }

  /** @return shader graph dock panel for inspector shortcuts */
  public ShaderGraphPanel shaderGraphPanel() {
    return shaderGraphPanel;
  }

  public void setParticlePanel(ParticlePanel particlePanel) {
    this.particlePanel = particlePanel;
  }

  public void setAnimationPanel(AnimationPanel animationPanel) {
    this.animationPanel = animationPanel;
  }

  /** @return animation dock panel for inspector shortcuts */
  public AnimationPanel animationPanel() {
    return animationPanel;
  }

  /** @return particle system dock panel for inspector shortcuts */
  public ParticlePanel particlePanel() {
    return particlePanel;
  }

  public void setParticleEditorState(ParticleEditorState particleEditorState) {
    this.particleEditorState = particleEditorState;
  }

  /** @return particle editor state for preview and scene sync */
  public ParticleEditorState particleEditorState() {
    return particleEditorState;
  }

  /** @return shared particle simulation world for editor preview and edit scene */
  public ParticleWorld particleWorld() {
    return particleWorld;
  }

  public void setPlayParticleWorld(ParticleWorld playParticleWorld) {
    this.playParticleWorld = playParticleWorld;
  }

  /** @return particle world during play mode, or null when stopped */
  public ParticleWorld playParticleWorld() {
    return playParticleWorld;
  }

  /** @return UI editor canvas selection and layout state */
  public UiEditorState uiEditorState() {
    return uiEditorState;
  }

  /**
   * @param materialProgramCache material programs for inspector invalidation; may be null
   */
  public void setMaterialProgramCache(MaterialProgramCache materialProgramCache) {
    this.materialProgramCache = materialProgramCache;
  }

  /** @return material program cache when a project is loaded */
  public MaterialProgramCache materialProgramCache() {
    return materialProgramCache;
  }
}
