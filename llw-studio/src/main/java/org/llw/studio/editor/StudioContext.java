package org.llw.studio.editor;

import org.llw.studio.project.ProjectDescriptor;
import org.llw.studio.scene.Scene;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Loaded project and edit/play scene state for the studio editor.
 */
public final class StudioContext {
    private Path projectRoot;
    private ProjectDescriptor project;
    private Scene editScene = new Scene();
    private Scene playScene;
    private boolean playing;
    private volatile String playPrepareStatus;

    /**
     * @param projectRoot filesystem root of the open project
     */
    public StudioContext(Path projectRoot) {
        this.projectRoot = Objects.requireNonNull(projectRoot);
    }

    /** @return project root directory */
    public Path projectRoot() {
        return projectRoot;
    }

    /**
     * @param projectRoot new project root
     */
    public void setProjectRoot(Path projectRoot) {
        this.projectRoot = Objects.requireNonNull(projectRoot);
    }

    /** @return loaded project descriptor, or null before a project is opened */
    public ProjectDescriptor project() {
        return project;
    }

    /**
     * @param project descriptor; also updates {@link #projectRoot()} from the descriptor
     */
    public void setProject(ProjectDescriptor project) {
        this.project = project;
        if (project != null) {
            this.projectRoot = project.root();
        }
    }

    /** @return scene edited in the editor (not the play clone) */
    public Scene editScene() {
        return editScene;
    }

    /**
     * @param scene replacement edit scene; null creates an empty scene
     */
    public void setEditScene(Scene scene) {
        editScene = scene == null ? new Scene() : scene;
    }

    /** @return cloned scene used during play mode, or null when stopped */
    public Scene playScene() {
        return playScene;
    }

    /** @return whether play mode is active */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * @param playing play-mode flag
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /** @return {@code true} while play mode is starting on a background thread */
    public boolean isPlayPreparing() {
        return playPrepareStatus != null;
    }

    /** @return toolbar status during play preparation, or {@code null} */
    public String playPrepareStatus() {
        return playPrepareStatus;
    }

    /**
     * @param playPrepareStatus preparation message, or {@code null} when idle
     */
    public void setPlayPrepareStatus(String playPrepareStatus) {
        this.playPrepareStatus = playPrepareStatus;
    }

    /**
     * @param playScene play clone; cleared when exiting play mode
     */
    public void setPlayScene(Scene playScene) {
        this.playScene = playScene;
    }

    /**
     * @return {@link #playScene()} while playing, otherwise {@link #editScene()}
     */
    public Scene activeScene() {
        // Play clone is authoritative while running; edit scene stays untouched for stop/revert.
        return playing && playScene != null ? playScene : editScene;
    }

    /** Hook for panels to clear selection when context resets; selection lives in {@link SelectionService}. */
    public void clearObjectSelection() {
        // selection cleared by SelectionService from panels
    }
}
