package org.llw.studio.editor.launcher;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.llw.studio.StudioEditorRuntime;
import org.llw.studio.build.BuildProgress;
import org.llw.studio.build.BuildResult;
import org.llw.studio.build.BuildSettings;
import org.llw.studio.build.BuildSettingsSerializer;
import org.llw.studio.build.ProjectBuildService;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.project.ProjectDescriptor;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Modal build progress dialog for packaging a standalone player.
 */
public final class BuildPlayerDialog {
    private final ImBoolean visible = new ImBoolean(false);
    private final ImBoolean building = new ImBoolean(false);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("");
    private volatile float progressFraction;
    private BuildResult lastResult;
    private final ExecutorService buildExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "build-player");
        thread.setDaemon(true);
        return thread;
    });

    public boolean isBuilding() {
        return building.get();
    }

    public void start(StudioEditorRuntime runtime, Consumer<String> onError) {
        if (building.get()) {
            visible.set(true);
            return;
        }
        StudioContext context = runtime.context();
        if (context.projectRoot() == null) {
            onError.accept("Open a project before building.");
            return;
        }
        ProjectDescriptor project = context.project();
        if (project == null) {
            onError.accept("Open a project before building.");
            return;
        }
        BuildSettings settings;
        try {
            settings = BuildSettingsSerializer.load(
                    context.projectRoot(),
                    context.project() == null ? "" : context.project().name()
            );
        } catch (IOException ex) {
            onError.accept(ex.getMessage());
            return;
        }
        visible.set(true);
        building.set(true);
        progressFraction = 0f;
        statusMessage.set("Starting build...");
        lastResult = null;
        MainThreadQueue mainThread = runtime.mainThreadQueue();
        BuildProgress progress = (fraction, message) -> {
            progressFraction = fraction;
            statusMessage.set(message);
        };
        buildExecutor.submit(() -> {
            BuildResult result = ProjectBuildService.build(
                    project,
                    runtime.assets(),
                    settings,
                    runtime.consoleSink(),
                    progress
            );
            mainThread.enqueue(() -> {
                lastResult = result;
                building.set(false);
                if (!result.success()) {
                    onError.accept(result.errorMessage() == null ? "Build failed." : result.errorMessage());
                } else {
                    statusMessage.set("Build complete.");
                    progressFraction = 1f;
                }
            });
        });
    }

    public void render(Consumer<String> onError) {
        if (!visible.get()) {
            return;
        }
        ImGui.setNextWindowSize(460f, 0f);
        ImGui.openPopup("Building Player");
        if (!ImGui.beginPopupModal("Building Player", visible, ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        ImGui.textWrapped(statusMessage.get());
        ImGui.progressBar(progressFraction, 420f, 0f, "");
        if (lastResult != null && lastResult.success()) {
            ImGui.spacing();
            ImGui.textWrapped("Output: " + lastResult.outputDirectory());
            if (ImGui.button("Open Output Folder", 180f, 0f)) {
                openFolder(lastResult.outputDirectory());
            }
            ImGui.sameLine();
            if (ImGui.button("Close", 120f, 0f)) {
                close();
            }
        } else if (!building.get()) {
            if (ImGui.button("Close", 120f, 0f)) {
                close();
            }
        } else {
            ImGui.beginDisabled();
            ImGui.button("Please wait...", 160f, 0f);
            ImGui.endDisabled();
        }
        ImGui.endPopup();
    }

    private void close() {
        visible.set(false);
        building.set(false);
        lastResult = null;
        ImGui.closeCurrentPopup();
    }

    private static void openFolder(Path path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
            }
        } catch (Exception ignored) {
        }
    }
}
