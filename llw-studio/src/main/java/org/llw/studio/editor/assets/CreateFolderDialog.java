package org.llw.studio.editor.assets;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
/**
 * Modal dialog to create a new folder under a project assets directory.
 */
public final class CreateFolderDialog {
    private final ImString folderName = new ImString("New Folder", 128);
    private String parentFolderGuid;
    private boolean open;
    private boolean focusName;

    public void open(String parentFolderGuid) {
        this.parentFolderGuid = parentFolderGuid;
        folderName.set("New Folder");
        open = true;
        focusName = true;
        ImGui.openPopup("Create Folder");
    }

    /**
     * @param assets   asset database to refresh after create
     * @param actions  folder create operation
     * @param onError  validation and IO errors
     */
    public void render(AssetEditorActions actions, java.util.function.Consumer<String> onError) {
        if (open) {
            ImGui.openPopup("Create Folder");
        }
        if (!ImGui.beginPopupModal("Create Folder", ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        open = false;

        ImGui.text("Folder name");
        if (focusName) {
            ImGui.setKeyboardFocusHere();
            focusName = false;
        }
        ImGui.inputText("##FolderName", folderName, ImGuiInputTextFlags.AutoSelectAll);

        ImGui.spacing();
        ImGui.separator();
        if (ImGui.button("Create", 120f, 0f)) {
            submit(actions, onError);
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 120f, 0f)) {
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }

    private void submit(AssetEditorActions actions, java.util.function.Consumer<String> onError) {
        String name = folderName.get().trim();
        if (name.isBlank()) {
            onError.accept("Enter a folder name.");
            return;
        }
        if (parentFolderGuid == null) {
            onError.accept("No parent folder selected.");
            return;
        }
        if (actions.createFolder(parentFolderGuid, name) == null) {
            onError.accept("Could not create folder. Check the name and try again.");
            return;
        }
        ImGui.closeCurrentPopup();
    }
}
