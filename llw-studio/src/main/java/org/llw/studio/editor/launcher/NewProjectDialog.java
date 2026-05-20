package org.llw.studio.editor.launcher;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.llw.studio.StudioEditorRuntime;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.studio.log.ConsoleLogSink;
import org.llw.studio.project.ProjectDescriptor;
import org.llw.studio.project.ProjectScaffolder;
import org.llw.studio.scripting.setup.ScriptProjectSetup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Modal dialog to scaffold a new project under a chosen parent folder.
 */
public final class NewProjectDialog {
    private final NativeFolderChooser folderChooser;
    private final ImString projectName = new ImString(64);
    private Path parentDirectory;
    private final ImBoolean visible = new ImBoolean(false);
    private final ImBoolean creating = new ImBoolean(false);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("");
    private volatile float progressFraction;
    private final ExecutorService setupExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "new-project-setup");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * @param folderChooser native picker for parent directory
     */
    public NewProjectDialog(NativeFolderChooser folderChooser) {
        this.folderChooser = folderChooser;
    }

    /** Opens the modal on the next {@link #render} call. */
    public void open() {
        projectName.set("");
        parentDirectory = Path.of(System.getProperty("user.home"));
        progressFraction = 0f;
        statusMessage.set("");
        creating.set(false);
        visible.set(true);
        ImGui.openPopup("New Project");
    }

    /**
     * Renders the popup when open.
     *
     * @param runtime editor runtime to load the created project
     * @param onError error callback for validation failures
     */
    public void render(StudioEditorRuntime runtime, Consumer<String> onError) {
        if (creating.get()) {
            renderProgress(runtime);
            return;
        }
        if (visible.get()) {
            ImGui.openPopup("New Project");
        }
        if (!ImGui.beginPopupModal("New Project", visible, ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        ImGui.text("Project Name");
        ImGui.inputText("##ProjectName", projectName);
        ImGui.spacing();
        ImGui.text("Location");
        String location = parentDirectory == null ? "" : parentDirectory.toString();
        ImGui.textWrapped(location);
        if (ImGui.button("Browse...")) {
            folderChooser.request("Select Parent Folder", parentDirectory);
        }
        folderChooser.poll();
        folderChooser.takeResult().ifPresent(path -> parentDirectory = path);
        ImGui.separator();
        if (ImGui.button("Create", 120f, 0f)) {
            startCreate(runtime, onError);
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 120f, 0f)) {
            visible.set(false);
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }

    private void renderProgress(StudioEditorRuntime runtime) {
        ImGui.setNextWindowSize(420f, 0f);
        if (!ImGui.beginPopupModal("Creating Project", creating, ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        ImGui.textWrapped(statusMessage.get());
        ImGui.progressBar(progressFraction, 380f, 0f, "");
        ImGui.spacing();
        ImGui.beginDisabled();
        ImGui.button("Please wait...", 160f, 0f);
        ImGui.endDisabled();
        ImGui.endPopup();
    }

    private void startCreate(StudioEditorRuntime runtime, Consumer<String> onError) {
        if (parentDirectory == null) {
            onError.accept("Select a parent folder.");
            return;
        }
        String name = projectName.get().trim();
        if (name.isBlank()) {
            onError.accept("Enter a project name.");
            return;
        }
        Path parent = parentDirectory;
        visible.set(false);
        creating.set(true);
        progressFraction = 0f;
        statusMessage.set("Creating project...");
        ImGui.openPopup("Creating Project");
        MainThreadQueue mainThread = runtime.mainThreadQueue();
        ConsoleLogSink console = runtime.consoleSink();
        setupExecutor.submit(() -> {
            try {
                statusMessage.set("Writing project files...");
                progressFraction = 0.1f;
                ProjectDescriptor descriptor = ProjectScaffolder.create(parent, name);
                ScriptProjectSetup.prepareNewProject(
                        descriptor.root(),
                        console,
                        (fraction, message) -> {
                            progressFraction = fraction;
                            statusMessage.set(message);
                        }
                );
                mainThread.enqueue(() -> {
                    try {
                        runtime.loadProject(descriptor.root());
                        finishCreate();
                    } catch (IOException ex) {
                        onError.accept(ex.getMessage());
                        creating.set(false);
                    }
                });
            } catch (Exception ex) {
                mainThread.enqueue(() -> {
                    onError.accept(ex.getMessage() == null ? "Could not create project." : ex.getMessage());
                    creating.set(false);
                });
            }
        });
    }

    private void finishCreate() {
        creating.set(false);
        progressFraction = 1f;
        statusMessage.set("");
        ImGui.closeCurrentPopup();
    }
}
