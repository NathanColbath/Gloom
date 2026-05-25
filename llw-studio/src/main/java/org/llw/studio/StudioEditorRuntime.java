package org.llw.studio;

import org.llw.render.gl.OpenGlBackend;
import org.llw.render.window.Window;
import org.llw.resources.ResourceManager;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.assets.AssetClipboard;
import org.llw.studio.editor.assets.AssetEditorActions;
import org.llw.studio.editor.assets.EditorIconRegistry;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.components.ComponentCatalog;
import org.llw.studio.editor.imgui.ImGuiContext;
import org.llw.studio.editor.launcher.BuildPlayerDialog;
import org.llw.studio.editor.launcher.BuildSettingsDialog;
import org.llw.studio.editor.launcher.NativeFolderChooser;
import org.llw.studio.editor.launcher.NewProjectDialog;
import org.llw.studio.editor.panels.AnimationPanel;
import org.llw.studio.editor.panels.ConsolePanel;
import org.llw.studio.editor.panels.GameViewPanel;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.editor.panels.HierarchyPanel;
import org.llw.studio.editor.panels.InspectorPanel;
import org.llw.studio.editor.panels.PanelRegistry;
import org.llw.studio.editor.panels.PanelVisibility;
import org.llw.studio.editor.panels.ProjectPanel;
import org.llw.studio.editor.panels.SceneViewPanel;
import org.llw.studio.editor.panels.ParticlePanel;
import org.llw.studio.editor.panels.ShaderGraphPanel;
import org.llw.studio.editor.panels.TilePalettePanel;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
import org.llw.studio.editor.shell.EditorMenuActionsHandler;
import org.llw.studio.editor.shell.EditorShell;
import org.llw.studio.log.ConsoleLogSink;
import org.llw.studio.log.LogSinkRegistry;
import org.llw.studio.project.MetadataLayoutMigration;
import org.llw.studio.project.ProjectDescriptor;
import org.llw.studio.project.ProjectDiscovery;
import org.llw.studio.project.RecentProjectsStore;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.editor.scripting.ScriptFileWatcher;
import org.llw.studio.scripting.js.ScriptSceneIndex;
import org.llw.studio.scripting.setup.ScriptProjectGenerator;
import org.llw.studio.editor.SceneBootstrap;
import org.llw.util.log.Log;
import org.llw.util.log.LogConfig;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns editor session state, panels, asset database, and project lifecycle after the native
 * window and render backend are created.
 */
public final class StudioEditorRuntime {
    private final Window window;
    private final ImGuiContext imgui;
    private final RecentProjectsStore recentProjects = new RecentProjectsStore();
    private final NativeFolderChooser folderChooser = new NativeFolderChooser();
    private final NewProjectDialog newProjectDialog = new NewProjectDialog(folderChooser);
    private final BuildSettingsDialog buildSettingsDialog = new BuildSettingsDialog(folderChooser);
    private final BuildPlayerDialog buildPlayerDialog = new BuildPlayerDialog();

    private final StudioContext context;
    private final EditorSession editorSession;
    private final SelectionService selection;
    private final UndoStack undoStack;
    private final AssetDatabase assets;
    private final EditorIconRegistry editorIcons;
    private final ConsoleLogSink consoleSink;
    private final MainThreadQueue mainThreadQueue;
    private final SceneViewPanel scenePanel;
    private final GameViewPanel gamePanel;
    private final ShaderGraphPanel shaderGraphPanel;
    private final ShaderGraphProgramCache shaderGraphCache;
    private final EditorShell shell;
    private final EditorMenuActionsHandler menuActions;
    private final PanelVisibility panelVisibility;
    private final ResourceManager resources;
    private ScriptFileWatcher scriptWatcher;
    private ExecutorService sceneScriptExecutor;
    private boolean projectLoaded;

    /**
     * Wires panels, undo, assets, console logging, and the editor shell for a new session.
     *
     * @param window         native GLFW window
     * @param backend        OpenGL render backend
     * @param resources      shared GPU/audio resources
     * @param imgui          Dear ImGui integration
     * @param bootstrapRoot  user data directory (recent projects, global ImGui ini, etc.)
     */
    public StudioEditorRuntime(
            Window window,
            OpenGlBackend backend,
            ResourceManager resources,
            ImGuiContext imgui,
            Path bootstrapRoot
    ) {
        this.window = window;
        this.resources = resources;
        this.imgui = imgui;

        context = new StudioContext(bootstrapRoot);
        editorSession = new EditorSession();
        selection = new SelectionService();
        undoStack = new UndoStack(context.editScene());
        assets = new AssetDatabase(bootstrapRoot, resources);
        editorIcons = new EditorIconRegistry();
        AssetPreviewCache previews = new AssetPreviewCache(assets);
        AssetEditorActions assetActions = new AssetEditorActions(assets, new AssetClipboard());
        ComponentCatalog componentCatalog = new ComponentCatalog();
        ConsolePanel consolePanel = new ConsolePanel();
        consoleSink = new ConsoleLogSink(consolePanel);
        LogSinkRegistry.attach(consoleSink);
        mainThreadQueue = new MainThreadQueue();
        editorIcons.setMainThreadQueue(mainThreadQueue);
        editorIcons.prefetchAll();

        panelVisibility = new PanelVisibility(ImGuiContext.globalIniPath());
        panelVisibility.setOpen("shader_graph", false);
        panelVisibility.setOpen("particle_system", false);
        shaderGraphCache = new ShaderGraphProgramCache(backend.shaderLibrary(), assets);
        menuActions = new EditorMenuActionsHandler(
                this,
                context,
                undoStack,
                assets,
                consoleSink,
                editorSession,
                panelVisibility,
                folderChooser,
                newProjectDialog,
                buildSettingsDialog,
                buildPlayerDialog,
                this::loadProject,
                window::requestClose
        );

        PanelRegistry panels = new PanelRegistry();
        scenePanel = new SceneViewPanel(backend, assets, selection, undoStack, editorSession, shaderGraphCache);
        editorSession.setFrameSceneCallback(s -> scenePanel.frameScene(s != null ? s : context.editScene()));
        gamePanel = new GameViewPanel(backend, assets, editorSession, shaderGraphCache);
        shaderGraphPanel = new ShaderGraphPanel(backend, assets, editorSession, panelVisibility, shaderGraphCache);
        editorSession.setShaderGraphPanel(shaderGraphPanel);
        menuActions.bindShaderGraphPanel(shaderGraphPanel);
        ParticlePanel particlePanel = new ParticlePanel(
                backend,
                assets,
                selection,
                editorSession,
                panelVisibility,
                editorSession.particleWorld(),
                shaderGraphCache
        );
        editorSession.setParticlePanel(particlePanel);
        menuActions.bindParticlePanel(particlePanel);
        panels.register(new HierarchyPanel(selection, assets));
        panels.register(new InspectorPanel(selection, undoStack, assets, previews, componentCatalog, editorSession));
        AnimationPanel animationPanel = new AnimationPanel(selection, assets, editorSession, undoStack, panelVisibility);
        editorSession.setAnimationPanel(animationPanel);
        panels.register(animationPanel);
        panels.register(new TilePalettePanel(assets, previews, selection, editorSession, panelVisibility));
        panels.register(shaderGraphPanel);
        panels.register(particlePanel);
        panels.register(scenePanel);
        panels.register(gamePanel);
        panels.register(consolePanel);
        panels.register(new ProjectPanel(
                window,
                assets,
                previews,
                editorIcons,
                selection,
                menuActions,
                assetActions,
                shaderGraphPanel,
                particlePanel,
                animationPanel
        ));

        shell = new EditorShell(
                context,
                panels,
                selection,
                undoStack,
                editorSession,
                assets,
                consoleSink,
                mainThreadQueue,
                window.handle(),
                !Files.exists(ImGuiContext.globalIniPath()),
                menuActions
        );
        shell.setImguiIniPath(ImGuiContext.globalIniPath());
        menuActions.bindShell(shell);
    }

    /** @return persisted recent-project list for the launcher */
    public RecentProjectsStore recentProjects() {
        return recentProjects;
    }

    /** @return async native folder picker used by menus and dialogs */
    public NativeFolderChooser folderChooser() {
        return folderChooser;
    }

    /** @return new-project creation dialog */
    public NewProjectDialog newProjectDialog() {
        return newProjectDialog;
    }

    /** @return queue for posting work back to the GLFW main thread */
    public MainThreadQueue mainThreadQueue() {
        return mainThreadQueue;
    }

    /** @return editor console used for script compile messages */
    public ConsoleLogSink consoleSink() {
        return consoleSink;
    }

    /** @return active studio context */
    public StudioContext context() {
        return context;
    }

    /** @return project asset database */
    public AssetDatabase assets() {
        return assets;
    }

    /** @return {@code true} after a project has been loaded successfully */
    public boolean isProjectLoaded() {
        return projectLoaded;
    }

    /** Polls folder-picker and modal dialog state (call from launcher or editor each frame). */
    public void pollAsyncUi() {
        if (folderChooser.poll()) {
            folderChooser.takeResult().ifPresent(menuActions::handleFolderSelection);
        }
        newProjectDialog.render(this, this::logError);
        menuActions.renderCreateScriptDialog();
        menuActions.renderBuildDialogs();
    }

    /**
     * Discovers, loads, and activates a project at {@code projectRoot}.
     *
     * @param projectRoot directory containing project metadata and assets
     * @throws IOException when scene bootstrap or discovery fails
     */
    public void loadProject(Path projectRoot) throws IOException {
        // Tear down play + watchers before rebinding assets so GUIDs and selection stay consistent.
        if (projectLoaded) {
            shell.stopPlayMode();
            closeScriptWatcher();
            selection.clear();
            assets.clearSelection();
            assets.clearInfo();
        }

        ProjectDescriptor descriptor = ProjectDiscovery.discover(projectRoot);
        MetadataLayoutMigration.migrateIfNeeded(descriptor.root());
        configureLogging(descriptor.root());

        context.setProject(descriptor);
        context.setProjectRoot(descriptor.root());
        context.setPlaying(false);
        context.setPlayScene(null);
        assets.rebindProject(descriptor.root());

        SceneBootstrap.loadOrBootstrap(context, assets, descriptor.startupScenePath());
        ScriptProjectGenerator.ensureProject(descriptor.root());
        undoStack.reset(context.editScene());
        scenePanel.frameScene(context.editScene());

        startScriptWatcher();
        scheduleSceneScriptCompile();
        recentProjects.add(descriptor);
        updateWindowTitle(descriptor.name());
        editorIcons.prefetchAll();
        projectLoaded = true;
    }

    private void configureLogging(Path projectRoot) {
        Log.init(LogConfig.builder()
                .logDir(StudioProjectLayout.resolveLogsDir(projectRoot))
                .minLevel(org.llw.util.log.LogLevel.DEBUG)
                .build());
    }

    private void startScriptWatcher() {
        closeScriptWatcher();
        scriptWatcher = new ScriptFileWatcher(
                context.projectRoot(),
                assets,
                consoleSink,
                mainThreadQueue,
                context::isPlaying,
                shell::onScriptRecompiled,
                () -> ScriptSceneIndex.collectGuids(context.editScene())
        );
        scriptWatcher.start();
        // Saves made during play are recompiled after stopPlayMode clears isPlaying.
        shell.setOnExitPlayMode(scriptWatcher::flushDeferredRecompiles);
    }

    private void scheduleSceneScriptCompile() {
        if (sceneScriptExecutor == null) {
            sceneScriptExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "scene-script-compile");
                thread.setDaemon(true);
                return thread;
            });
        }
        Set<String> guids = ScriptSceneIndex.collectGuids(context.editScene());
        sceneScriptExecutor.submit(() ->
                ScriptCompileService.bundleGuids(context.projectRoot(), assets, guids, consoleSink));
    }

    private void closeScriptWatcher() {
        if (scriptWatcher != null) {
            scriptWatcher.close();
            scriptWatcher = null;
        }
        if (sceneScriptExecutor != null) {
            sceneScriptExecutor.shutdownNow();
            sceneScriptExecutor = null;
        }
    }

    private void updateWindowTitle(String projectName) {
        GLFW.glfwSetWindowTitle(window.handle(), "LLW Studio - " + projectName);
    }

    /** Renders the full editor shell (scene view, hierarchy, inspector, etc.). */
    public void renderEditor() {
        renderEditor(0f);
    }

    /**
     * @param frameDeltaSeconds wall-clock frame delta from the main loop (seconds)
     */
    public void renderEditor(float frameDeltaSeconds) {
        pollAsyncUi();
        if (context.isPlaying()) {
            PlayUiInputBridge.setEnteredText(window.takeEnteredText());
        }
        shell.render(frameDeltaSeconds);
        panelVisibility.reconcileIni();
    }

    /** Releases script watcher, view panels, ImGui, and shared resources. */
    public void dispose() {
        closeScriptWatcher();
        scenePanel.dispose();
        gamePanel.dispose();
        editorIcons.dispose();
        panelVisibility.flush();
        imgui.dispose();
        panelVisibility.persistToIni();
        resources.close();
    }

    private void logError(String message) {
        if (consoleSink != null) {
            consoleSink.append(org.llw.util.log.LogLevel.ERROR, message);
        }
    }
}
