package org.llw.studio.editor.shell;

import org.llw.studio.StudioEditorRuntime;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.animation.AnimationClipActions;
import org.llw.studio.editor.animation.AnimationSetActions;
import org.llw.studio.editor.launcher.BuildPlayerDialog;
import org.llw.studio.editor.launcher.BuildSettingsDialog;
import org.llw.studio.editor.launcher.CreateScriptDialog;
import org.llw.studio.editor.launcher.NativeFolderChooser;
import org.llw.studio.editor.launcher.NewProjectDialog;
import org.llw.studio.editor.panels.PanelVisibility;
import org.llw.studio.editor.panels.ParticlePanel;
import org.llw.studio.editor.panels.ShaderGraphPanel;
import org.llw.studio.particles.assets.ParticleSystemActions;
import org.llw.studio.shadergraph.assets.ShaderGraphActions;
import org.llw.studio.log.ConsoleLogSink;
import org.llw.studio.playmode.PlayModeRunner;
import org.llw.studio.scripting.setup.ScriptProjectGenerator;
import org.llw.studio.serialization.ProjectSerializer;
import org.llw.studio.serialization.SceneSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Default {@link EditorMenuActions} wired to project load, save, undo, and script dialogs.
 */
public final class EditorMenuActionsHandler implements EditorMenuActions {
    private final StudioEditorRuntime runtime;
    private final StudioContext context;
    private final UndoStack undoStack;
    private final AssetDatabase assets;
    private final ConsoleLogSink console;
    private final EditorSession session;
    private final PanelVisibility panelVisibility;
    private ShaderGraphPanel shaderGraphPanel;
    private ParticlePanel particlePanel;
    private EditorShell shell;
    private final NativeFolderChooser folderChooser;
    private final NewProjectDialog newProjectDialog;
    private final BuildSettingsDialog buildSettingsDialog;
    private final BuildPlayerDialog buildPlayerDialog;
    private final CreateScriptDialog createScriptDialog = new CreateScriptDialog();
    private final PathLoadRequest projectLoader;
    private final Runnable exitAction;

    /**
     * Loads a project from a filesystem path.
     */
    @FunctionalInterface
    public interface PathLoadRequest {
        /**
         * @param path project root directory
         */
        void load(Path path) throws IOException;
    }

    /**
     * @param context           studio context
     * @param undoStack         edit undo
     * @param assets            asset database
     * @param console           error logging
     * @param session           editor session
     * @param panelVisibility   closable panel open/close state
     * @param folderChooser     native folder dialog
     * @param newProjectDialog  new project modal
     * @param projectLoader     opens a project path
     * @param exitAction        application exit
     */
    public EditorMenuActionsHandler(
            StudioEditorRuntime runtime,
            StudioContext context,
            UndoStack undoStack,
            AssetDatabase assets,
            ConsoleLogSink console,
            EditorSession session,
            PanelVisibility panelVisibility,
            NativeFolderChooser folderChooser,
            NewProjectDialog newProjectDialog,
            BuildSettingsDialog buildSettingsDialog,
            BuildPlayerDialog buildPlayerDialog,
            PathLoadRequest projectLoader,
            Runnable exitAction
    ) {
        this.runtime = runtime;
        this.context = context;
        this.undoStack = undoStack;
        this.assets = assets;
        this.console = console;
        this.session = session;
        this.panelVisibility = panelVisibility;
        this.folderChooser = folderChooser;
        this.newProjectDialog = newProjectDialog;
        this.buildSettingsDialog = buildSettingsDialog;
        this.buildPlayerDialog = buildPlayerDialog;
        this.projectLoader = projectLoader;
        this.exitAction = exitAction;
    }

    /** @param shell shell used for layout reset */
    public void bindShell(EditorShell shell) {
        this.shell = shell;
    }

    /** @param shaderGraphPanel panel opened by asset create / inspector edit actions */
    public void bindShaderGraphPanel(ShaderGraphPanel shaderGraphPanel) {
        this.shaderGraphPanel = shaderGraphPanel;
    }

    public void bindParticlePanel(ParticlePanel panel) {
        this.particlePanel = panel;
    }

    /** {@inheritDoc} */
    @Override
    public void newProject() {
        newProjectDialog.open();
    }

    /** {@inheritDoc} */
    @Override
    public void openProject() {
        folderChooser.request("Open Project", context.projectRoot());
    }

    /** {@inheritDoc} */
    @Override
    public void saveScene() {
        try {
            var project = context.project();
            if (project == null) {
                return;
            }
            SceneSerializer.save(context.editScene(), project.startupScenePath());
        } catch (Exception ex) {
            logError("Failed to save scene: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void saveProject() {
        try {
            var project = context.project();
            if (project == null) {
                return;
            }
            ProjectSerializer.save(project.root(), project.name(), project.startupSceneRelative());
        } catch (Exception ex) {
            logError("Failed to save project: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void buildSettings() {
        if (context.project() == null) {
            logError("Open a project before changing build settings.");
            return;
        }
        buildSettingsDialog.open(context);
    }

    /** {@inheritDoc} */
    @Override
    public void buildPlayer() {
        buildPlayerDialog.start(runtime, this::logError);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBuildingPlayer() {
        return buildPlayerDialog.isBuilding();
    }

    /** {@inheritDoc} */
    @Override
    public void exit() {
        exitAction.run();
    }

    /** {@inheritDoc} */
    @Override
    public void undo() {
        undoStack.undo();
    }

    /** {@inheritDoc} */
    @Override
    public void redo() {
        undoStack.redo();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canUndo() {
        return undoStack.canUndo();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canRedo() {
        return undoStack.canRedo();
    }

    /** {@inheritDoc} */
    @Override
    public void createScript() {
        createScriptInFolder(null);
    }

    /** {@inheritDoc} */
    @Override
    public void createAnimationClip() {
        createAnimationClipInFolder(null);
    }

    /** {@inheritDoc} */
    @Override
    public void createAnimationClipInFolder(Path folder) {
        try {
            AnimationClipActions.createClip(assets, folder, "NewClip");
        } catch (IOException ex) {
            logError("Failed to create animation clip: " + ex.getMessage());
        }
    }

    @Override
    public void createAnimation() {
        createAnimationInFolder(null);
    }

    @Override
    public void createAnimationInFolder(Path folder) {
        try {
            AnimationSetActions.createAnimation(assets, folder, "NewAnimation");
        } catch (IOException ex) {
            logError("Failed to create animation: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void createScriptInFolder(Path folder) {
        createScriptDialog.open(folder);
    }

    /**
     * Renders the create-script modal when open.
     *
     * <p>Implementation note: Call from the launcher or editor UI loop each frame while a project may be loaded.
     */
    public void renderCreateScriptDialog() {
        createScriptDialog.render(context, assets, this::logError);
    }

    /** Renders build settings and build progress dialogs. */
    public void renderBuildDialogs() {
        buildSettingsDialog.render(context, assets, this::logError);
        buildPlayerDialog.render(this::logError);
    }

    /** {@inheritDoc} */
    @Override
    public void refreshScripts() {
        ScriptProjectGenerator.ensureProject(context.projectRoot());
        PlayModeRunner.refreshScripts(context.projectRoot(), assets, console);
    }

    /** {@inheritDoc} */
    @Override
    public void frameScene() {
        session.frameScene(null);
    }

    /** {@inheritDoc} */
    @Override
    public void toggleAnimationPanel() {
        panelVisibility.toggle("animation");
    }

    /** {@inheritDoc} */
    @Override
    public void toggleTilePalettePanel() {
        panelVisibility.toggle("tile_palette");
    }

    @Override
    public void toggleShaderGraphPanel() {
        panelVisibility.toggle("shader_graph");
    }

    @Override
    public void createShaderGraph() {
        createShaderGraphInFolder(null);
    }

    @Override
    public void createShaderGraphInFolder(Path folder) {
        try {
            String guid = ShaderGraphActions.createShaderGraph(assets, folder, "NewShader");
            if (shaderGraphPanel != null) {
                StudioAsset asset = assets.get(guid);
                if (asset != null) {
                    shaderGraphPanel.openAsset(guid, asset.path());
                }
            }
        } catch (IOException ex) {
            logError("Failed to create shader graph: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnimationPanelOpen() {
        return panelVisibility.isOpen("animation");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTilePalettePanelOpen() {
        return panelVisibility.isOpen("tile_palette");
    }

    @Override
    public boolean isShaderGraphPanelOpen() {
        return panelVisibility.isOpen("shader_graph");
    }

    @Override
    public void toggleParticlePanel() {
        panelVisibility.toggle("particle_system");
    }

    @Override
    public void createParticleSystem() {
        createParticleSystemInFolder(null);
    }

    @Override
    public void createParticleSystemInFolder(Path folder) {
        try {
            String guid = ParticleSystemActions.createParticleSystem(assets, folder, "NewParticles");
            if (particlePanel != null) {
                StudioAsset asset = assets.get(guid);
                if (asset != null) {
                    particlePanel.openAsset(guid, asset.path());
                }
            }
        } catch (IOException ex) {
            logError("Failed to create particle system: " + ex.getMessage());
        }
    }

    @Override
    public boolean isParticlePanelOpen() {
        return panelVisibility.isOpen("particle_system");
    }

    /** {@inheritDoc} */
    @Override
    public void resetLayout() {
        if (shell == null) {
            return;
        }
        panelVisibility.resetAllOpen();
        try {
            Files.deleteIfExists(shell.imguiIniPath());
        } catch (IOException ignored) {
        }
        shell.setApplyDefaultLayout(true);
    }

    /** {@inheritDoc} */
    @Override
    public String projectName() {
        return context.project() == null ? "" : context.project().name();
    }

    /**
     * Completes open-project after {@link NativeFolderChooser} returns a path.
     *
     * @param path selected project root
     */
    public void handleFolderSelection(Path path) {
        try {
            projectLoader.load(path);
        } catch (IOException ex) {
            logError("Failed to open project: " + ex.getMessage());
        }
    }

    private void logError(String message) {
        if (console != null) {
            console.append(org.llw.util.log.LogLevel.ERROR, message);
        }
    }
}
