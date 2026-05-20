package org.llw.studio.editor.launcher;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.llw.studio.build.BuildSettings;
import org.llw.studio.build.BuildSettingsSerializer;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.widgets.fields.AssetReferenceField;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Floating window for player build settings (non-modal so assets can be dragged from the Project panel).
 */
public final class BuildSettingsDialog {
    private final NativeFolderChooser folderChooser;
    private final ImBoolean visible = new ImBoolean(false);
    private final ImString productName = new ImString(64);
    private final ImString version = new ImString(32);
    private final ImString outputDirectory = new ImString(512);
    private final ImString windowTitle = new ImString(64);
    private final ImInt windowWidth = new ImInt(1280);
    private final ImInt windowHeight = new ImInt(720);
    private final ImBoolean vsync = new ImBoolean(true);
    private String iconAssetGuid = "";

    public BuildSettingsDialog(NativeFolderChooser folderChooser) {
        this.folderChooser = folderChooser;
    }

    public void open(StudioContext context) {
        try {
            BuildSettings settings = BuildSettingsSerializer.load(
                    context.projectRoot(),
                    context.project() == null ? "" : context.project().name()
            );
            productName.set(settings.productName());
            version.set(settings.version());
            outputDirectory.set(settings.outputDirectory());
            windowTitle.set(settings.windowTitle());
            windowWidth.set(settings.windowWidth());
            windowHeight.set(settings.windowHeight());
            vsync.set(settings.vsync());
            iconAssetGuid = settings.iconAssetGuid();
        } catch (IOException ex) {
            String name = context.project() == null ? "" : context.project().name();
            productName.set(name);
            windowTitle.set(name);
        }
        visible.set(true);
    }

    public void render(StudioContext context, AssetDatabase assets, Consumer<String> onError) {
        if (!visible.get()) {
            return;
        }
        ImGui.setNextWindowSize(420f, 0f, ImGuiCond.Appearing);
        if (!ImGui.begin("Build Settings", visible, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.end();
            return;
        }
        ImGui.text("Product Name");
        ImGui.inputText("##BuildProductName", productName);
        ImGui.text("Version");
        ImGui.inputText("##BuildVersion", version);
        ImGui.text("Output Directory (optional)");
        ImGui.inputText("##BuildOutputDir", outputDirectory);
        if (ImGui.button("Browse Output...")) {
            Path start = outputDirectory.get().isBlank()
                    ? context.projectRoot()
                    : Path.of(outputDirectory.get());
            folderChooser.request("Select Output Folder", start);
        }
        folderChooser.poll();
        folderChooser.takeResult().ifPresent(path -> outputDirectory.set(path.toString()));
        ImGui.separator();
        ImGui.text("Window Title");
        ImGui.inputText("##BuildWindowTitle", windowTitle);
        ImGui.inputInt("Width", windowWidth);
        ImGui.inputInt("Height", windowHeight);
        ImGui.checkbox("VSync", vsync);
        ImGui.separator();
        ImGui.text("Application Icon");
        ImGui.textDisabled("Drag a texture or .ico from the Project panel.");
        if (assets != null) {
            iconAssetGuid = AssetReferenceField.draw("Icon", iconAssetGuid, assets);
        }
        ImGui.separator();
        if (ImGui.button("Save", 120f, 0f)) {
            save(context, onError);
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 120f, 0f)) {
            visible.set(false);
        }
        ImGui.end();
    }

    public BuildSettings currentSettings() {
        BuildSettings settings = new BuildSettings();
        settings.setProductName(productName.get().trim());
        settings.setVersion(version.get().trim());
        settings.setOutputDirectory(outputDirectory.get().trim());
        settings.setWindowTitle(windowTitle.get().trim());
        settings.setWindowWidth(windowWidth.get());
        settings.setWindowHeight(windowHeight.get());
        settings.setVsync(vsync.get());
        settings.setIconAssetGuid(iconAssetGuid);
        return settings;
    }

    private void save(StudioContext context, Consumer<String> onError) {
        try {
            BuildSettingsSerializer.save(context.projectRoot(), currentSettings());
            visible.set(false);
        } catch (IOException ex) {
            onError.accept(ex.getMessage());
        }
    }
}
