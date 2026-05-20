package org.llw.studio.editor.launcher;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiWindowFlags;
import org.llw.studio.StudioEditorRuntime;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.project.RecentProjectsStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Full-screen project picker shown before a project is loaded.
 *
 * <p>Implementation note: Rendered each frame from the bootstrap loop; hosts {@link NewProjectDialog} and folder open.
 */
public final class ProjectLauncherScreen {
    private final StudioEditorRuntime runtime;
    private final Consumer<String> onError;
    private final Path sampleProjectPath;

    /**
     * @param runtime           editor runtime (dialogs, project load)
     * @param onError           receives user-visible error messages
     * @param sampleProjectPath optional bundled sample project path
     */
    public ProjectLauncherScreen(StudioEditorRuntime runtime, Consumer<String> onError, Path sampleProjectPath) {
        this.runtime = runtime;
        this.onError = onError;
        this.sampleProjectPath = sampleProjectPath;
    }

    /** Draws the launcher UI for the current ImGui frame. */
    public void render() {
        // NewProjectDialog is rendered once per frame from StudioEditorRuntime.pollAsyncUi().
        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
        int flags = ImGuiWindowFlags.NoDecoration
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoBringToFrontOnFocus;
        ImGui.begin("##ProjectLauncher", flags);

        float panelWidth = 560f;
        float panelHeight = 420f;
        float x = (viewport.getWorkSizeX() - panelWidth) * 0.5f;
        float y = (viewport.getWorkSizeY() - panelHeight) * 0.5f;
        ImGui.setCursorPos(x, y);
        ImGui.beginChild("LauncherPanel", panelWidth, panelHeight, true);

        ImGui.text("LLW Studio");
        EditorStyle.pushMutedText();
        ImGui.text("Open a recent project or create a new one.");
        EditorStyle.popMutedText();
        ImGui.separator();
        ImGui.spacing();

        if (ImGui.button("Open Project...", 160f, 0f)) {
            runtime.folderChooser().request("Open Project", null);
        }
        ImGui.sameLine();
        if (ImGui.button("New Project...", 160f, 0f)) {
            runtime.newProjectDialog().open();
        }
        if (sampleProjectPath != null && Files.isDirectory(sampleProjectPath)) {
            ImGui.sameLine();
            if (ImGui.button("Open Sample", 160f, 0f)) {
                openPath(sampleProjectPath);
            }
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.text("Recent Projects");
        ImGui.beginChild("RecentList", 0f, 240f, true);
        boolean hasRecent = false;
        for (RecentProjectsStore.RecentProjectEntry entry : runtime.recentProjects().entries()) {
            if (!Files.isDirectory(entry.path())) {
                continue;
            }
            hasRecent = true;
            if (ImGui.selectable(entry.name() + "##" + entry.path(), false)) {
                openPath(entry.path());
            }
            EditorStyle.pushMutedText();
            ImGui.textWrapped(entry.path().toString());
            EditorStyle.popMutedText();
        }
        if (!hasRecent) {
            EditorStyle.pushMutedText();
            ImGui.textWrapped("No recent projects yet.");
            EditorStyle.popMutedText();
        }
        ImGui.endChild();
        ImGui.endChild();
        ImGui.end();
    }

    private void openPath(Path path) {
        try {
            runtime.loadProject(path);
        } catch (IOException ex) {
            onError.accept(ex.getMessage());
        }
    }
}
