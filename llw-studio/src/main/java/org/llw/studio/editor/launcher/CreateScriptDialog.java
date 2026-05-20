package org.llw.studio.editor.launcher;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.scripting.setup.ScriptProjectGenerator;
import org.llw.studio.scripting.setup.ScriptTemplateGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Modal dialog to create a new JavaScript script asset from a template.
 */
public final class CreateScriptDialog {
    private final ImString scriptName = new ImString("NewScript", 128);
    private Path targetFolder;
    private boolean open;
    private boolean focusName;

    /**
     * @param folder assets folder for the new script, or null for project scripts root
     */
    public void open(Path folder) {
        targetFolder = folder;
        scriptName.set("NewScript");
        open = true;
        focusName = true;
        ImGui.openPopup("Create New Script");
    }

    /**
     * @param context studio context (project root)
     * @param assets  asset database to refresh after create
     * @param onError validation and IO errors
     * <p>Implementation note: Call each editor frame from {@link org.llw.studio.editor.shell.EditorMenuActionsHandler#renderCreateScriptDialog()}.
     */
    public void render(StudioContext context, AssetDatabase assets, Consumer<String> onError) {
        if (open) {
            ImGui.openPopup("Create New Script");
        }
        if (!ImGui.beginPopupModal("Create New Script", ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        open = false;

        ImGui.text("Script Name");
        if (focusName) {
            ImGui.setKeyboardFocusHere();
            focusName = false;
        }
        ImGui.inputText("##ScriptName", scriptName, ImGuiInputTextFlags.AutoSelectAll);

        ImGui.spacing();
        ImGui.separator();
        if (ImGui.button("Create", 120f, 0f)) {
            submit(context, assets, onError);
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 120f, 0f)) {
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }

    private void submit(StudioContext context, AssetDatabase assets, Consumer<String> onError) {
        String name = scriptName.get().trim();
        if (name.isBlank()) {
            onError.accept("Enter a script name.");
            return;
        }
        try {
            String guid = ScriptTemplateGenerator.createNewScript(
                    context.projectRoot(), assets, name, targetFolder);
            ScriptProjectGenerator.ensureProject(context.projectRoot());
            if (!guid.isEmpty()) {
                assets.select(guid);
            }
            ImGui.closeCurrentPopup();
        } catch (IOException ex) {
            onError.accept(ex.getMessage());
        }
    }
}
