package org.llw.studio.editor.animation;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

/**
 * Modal prompt for a new animation state name.
 */
public final class CreateStateDialog {
    private boolean open;
    private final ImString name = new ImString("NewState", 64);

    public void open() {
        open = true;
        name.set("NewState");
        ImGui.openPopup("Create Animation State");
    }

    /**
     * @return entered state name when the user confirms, or {@code null}
     */
    public String render() {
        if (!open) {
            return null;
        }
        ImGui.setNextWindowSize(320f, 0f);
        if (!ImGui.beginPopupModal("Create Animation State", ImGuiWindowFlags.AlwaysAutoResize)) {
            return null;
        }
        ImGui.textWrapped("State name (a matching clip file is created automatically):");
        ImGui.inputText("Name", name);
        ImGui.separator();
        String result = null;
        if (ImGui.button("Create", 100f, 0f)) {
            result = name.get().trim();
            open = false;
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 100f, 0f)) {
            open = false;
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
        if (result != null && result.isBlank()) {
            return null;
        }
        return result;
    }
}
