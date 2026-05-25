package org.llw.studio.editor.shell;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.studio.editor.animation.AnimationEditorState;
import org.llw.studio.editor.EditorDragDrop;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.panels.EditorPanel;
import org.llw.studio.editor.panels.PanelRegistry;
import org.llw.studio.editor.widgets.TopToolbar;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.log.ConsoleLogSink;
import org.llw.studio.memory.StudioMemory;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.playmode.PlayModeRunner;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Top-level editor frame: menu bar, dock space, toolbar, panels, and play mode.
 *
 * <p>Implementation note: {@link #render()} runs once per application frame after {@link org.llw.studio.editor.imgui.ImGuiContext#beginFrame}
 * and before ImGui render; it flushes {@link MainThreadQueue}, then draws all {@link EditorPanel}s.
 */
public final class EditorShell {
  private static final String DOCK_HOST = "##StudioDockHost";
  private static final String DOCK_SPACE = "StudioDockSpace";

  private final StudioContext context;
  private final PanelRegistry panels;
  private final SelectionService selection;
  private final UndoStack undoStack;
  private final EditorSession session;
  private final PlayModeRunner playModeRunner = new PlayModeRunner();
  private final ExecutorService playPrepareExecutor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r, "play-prepare");
    thread.setDaemon(true);
    return thread;
  });
  private final AppMenuBar menuBar;
  private final TopToolbar toolbar;
  private final AssetDatabase assets;
  private final ConsoleLogSink console;
  private final long windowHandle;
  private final MainThreadQueue mainThreadQueue;
  private final EditorMenuActions menuActions;
  private Runnable onExitPlayMode = () -> {};
  private boolean applyDefaultLayout;
  private Path imguiIniPath = Path.of(".");

  /**
   * @param context              loaded project
   * @param panels               registered dock panels
   * @param selection            entity selection
   * @param undoStack            edit undo
   * @param session              editor session
   * @param assets               asset database
   * @param console              console log sink
   * @param mainThreadQueue      deferred main-thread work
   * @param windowHandle         GLFW window handle for play mode input
   * @param applyDefaultLayout   when true, builds default dock splits on first frame
   * @param menuActions          menu command handler
   */
  public EditorShell(
      StudioContext context,
      PanelRegistry panels,
      SelectionService selection,
      UndoStack undoStack,
      EditorSession session,
      AssetDatabase assets,
      ConsoleLogSink console,
      MainThreadQueue mainThreadQueue,
      long windowHandle,
      boolean applyDefaultLayout,
      EditorMenuActions menuActions
  ) {
    this.context = context;
    this.panels = panels;
    this.selection = selection;
    this.undoStack = undoStack;
    this.session = session;
    this.assets = assets;
    this.console = console;
    this.mainThreadQueue = mainThreadQueue;
    this.windowHandle = windowHandle;
    this.applyDefaultLayout = applyDefaultLayout;
    this.menuActions = menuActions;
    this.playModeRunner.setWindowHandle(windowHandle);
    this.menuBar = new AppMenuBar(menuActions);
    this.toolbar = new TopToolbar(session, this::togglePlay);
  }

  /** @param imguiIniPath path to ImGui layout ini (for reset layout) */
  public void setImguiIniPath(Path imguiIniPath) {
    this.imguiIniPath = imguiIniPath;
  }

  /** @return persisted ImGui ini path */
  public Path imguiIniPath() {
    return imguiIniPath;
  }

  /** @param applyDefaultLayout when true, {@link DockLayout} runs on next dock frame */
  public void setApplyDefaultLayout(boolean applyDefaultLayout) {
    this.applyDefaultLayout = applyDefaultLayout;
  }

  /** @param onExitPlayMode callback after play mode stops and selection is restored */
  public void setOnExitPlayMode(Runnable onExitPlayMode) {
    this.onExitPlayMode = onExitPlayMode == null ? () -> {} : onExitPlayMode;
  }

  /** Stops play mode, restores edit selection, and focuses the scene view. */
  public void stopPlayMode() {
    context.setPlayPrepareStatus(null);
    if (context.isPlaying()) {
      // Map play selection back to edit scene via stable scene-object id before tearing down play world.
      EntityId selected = selection.selected();
      int selectedSceneId = -1;
      if (!selected.isNone() && context.playScene() != null) {
        selectedSceneId = SceneObjectIds.get(context.playScene().world(), selected);
      }
      context.setPlaying(false);
      context.setPlayScene(null);
      playModeRunner.stop();
      session.setPlayScriptSystem(null);
      session.setPlayParticleWorld(null);
      selection.clear();
      org.llw.studio.scripting.js.PlayCameraBridge.reset();
      if (selectedSceneId >= 0) {
        EntityId editEntity = SceneObjectIds.findBySceneId(context.editScene().world(), selectedSceneId);
        if (!editEntity.isNone()) {
          selection.select(editEntity);
        }
      }
      onExitPlayMode.run();
      session.setGameViewFocused(false);
      session.requestFocusSceneView();
    }
  }

  /** Disposes panels that implement {@link AutoCloseable}. */
  public void disposePanels() {
    for (EditorPanel panel : panels.all()) {
      if (panel instanceof AutoCloseable closeable) {
        try {
          closeable.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  /**
   * Renders one editor frame (menu, dock, toolbar, panels, play update).
   *
   * <p>Implementation note: Call exactly once per frame between ImGui {@code newFrame} and {@code render}.
   */
  public void render() {
    render(0f);
  }

  /**
   * @param frameDeltaSeconds wall-clock seconds since the previous frame; when {@code <= 0}, uses ImGui delta
   */
  public void render(float frameDeltaSeconds) {
    // Script watcher / play-prepare callbacks enqueue here before any panel draws.
    mainThreadQueue.flush();

    float deltaTime = frameDeltaSeconds > 0f ? frameDeltaSeconds : ImGui.getIO().getDeltaTime();
    if (deltaTime <= 0f) {
      deltaTime = 1f / 60f;
    }
    if (deltaTime > 0.1f) {
      deltaTime = 0.1f;
    }
    // Exactly one preview/sim path per frame: play, animation preview, particle editor, or edit particles.
    if (context.isPlaying() && context.playScene() != null) {
      PlayUiInputBridge.setWantCaptureKeyboard(ImGui.getIO().getWantCaptureKeyboard());
      playModeRunner.update(context.playScene(), deltaTime, session.isGameViewFocused());
    } else if (session.animationEditorState() != null) {
      AnimationEditorState animState = session.animationEditorState();
      if (animState.playing()) {
        animState.advancePreview(deltaTime);
      }
      animState.applyScenePreview(context.editScene(), assets);
    } else if (session.particleEditorState() != null) {
      var particleState = session.particleEditorState();
      particleState.applyScenePreview(context.editScene(), assets, session.particleWorld());
      stepEditSceneParticles(context.editScene(), deltaTime);
    } else {
      stepEditSceneParticles(context.editScene(), deltaTime);
    }

    menuBar.render();
    renderDockSpace();
    toolbar.render(context);

    EditorDragDrop.beginFrame(selection.selected());

    for (EditorPanel panel : panels.all()) {
      panel.render(context);
    }
    EditorDragDrop.endFrame(selection);
    StudioMemory.endFrame();
  }

  private void renderDockSpace() {
    ImGuiViewport viewport = ImGui.getMainViewport();
    ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
    ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
    ImGui.setNextWindowViewport(viewport.getID());

    int hostFlags = ImGuiWindowFlags.NoDocking
        | ImGuiWindowFlags.NoTitleBar
        | ImGuiWindowFlags.NoCollapse
        | ImGuiWindowFlags.NoResize
        | ImGuiWindowFlags.NoMove
        | ImGuiWindowFlags.NoBringToFrontOnFocus
        | ImGuiWindowFlags.NoNavFocus;

    ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
    ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
    ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f);
    ImGui.begin(DOCK_HOST, hostFlags);
    ImGui.popStyleVar(3);

    int dockspaceId = ImGui.getID(DOCK_SPACE);
    ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.None);

    if (applyDefaultLayout) {
      DockLayout.buildDefault(dockspaceId, viewport.getWorkSizeX(), viewport.getWorkSizeY());
      applyDefaultLayout = false;
    }

    ImGui.end();
  }

  private void togglePlay() {
    if (context.isPlaying()) {
      stopPlayMode();
    } else if (context.isPlayPreparing()) {
      // ignore while a play session is being prepared
    } else {
      beginPlayModeAsync();
    }
  }

  private void beginPlayModeAsync() {
    EntityId selected = selection.selected();
    int selectedSceneId = selected.isNone() ? -1 : SceneObjectIds.get(context.editScene().world(), selected);
    context.setPlayPrepareStatus("Preparing scripts...");
    // Heavy script bundle + scene clone off the UI thread; activation must run on main thread.
    playPrepareExecutor.submit(() -> {
      PlayModeRunner.PlayPrepareResult prepared = playModeRunner.prepareScene(
              context.editScene(),
              context.projectRoot(),
              assets,
              console
      );
      mainThreadQueue.enqueue(() -> {
        Scene playScene = playModeRunner.activate(prepared, assets, assets.resourceManager());
        finishPlayModeEnter(playScene, selectedSceneId);
      });
    });
  }

  private void finishPlayModeEnter(Scene playScene, int selectedSceneId) {
    context.setPlayPrepareStatus(null);
    context.setPlayScene(playScene);
    context.setPlaying(true);
    session.setPlayScriptSystem(playModeRunner.scriptSystem());
    session.setPlayParticleWorld(playModeRunner.particleWorld());
    if (selectedSceneId >= 0) {
      EntityId playEntity = playModeRunner.playEntityForSceneId(selectedSceneId);
      if (!playEntity.isNone()) {
        selection.select(playEntity);
      } else {
        selection.clear();
      }
    }
    session.requestFocusGameView();
  }

  /**
   * Hot-reloads a script in the active play session.
   *
   * @param scriptGuid asset GUID of the script
   */
  public void onScriptRecompiled(String scriptGuid) {
    playModeRunner.reloadScript(scriptGuid);
  }

  private void stepEditSceneParticles(Scene scene, float deltaTime) {
    if (scene == null || deltaTime <= 0f) {
      return;
    }
    var emitters = scene.world().store(ParticleEmitterComponent.class);
    if (emitters.size() == 0) {
      return;
    }
    ParticleWorld world = session.particleWorld();
    world.syncScene(scene.world(), assets);
    for (int i = 0; i < emitters.size(); i++) {
      EntityId entity = emitters.entityAt(i);
      ParticleEmitterComponent component = emitters.componentAt(i);
      EmitterState state = world.emitter(entity);
      if (state != null && component.emitting) {
        world.stepEmitter(state, component, deltaTime, assets, null);
      }
    }
  }
}
