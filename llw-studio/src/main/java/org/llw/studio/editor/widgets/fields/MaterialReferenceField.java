package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Material asset picker with drag-drop and optional edit shortcut.
 */
public final class MaterialReferenceField {
    private MaterialReferenceField() {
    }

    public static String draw(String label, String guid, AssetDatabase assets, Runnable onEdit) {
        PropertyRow.begin(label);
        String display = guid == null || guid.isBlank() ? "None" : assets.displayName(guid);
        float maxWidth = ImGui.getContentRegionAvailX() - 96f;
        String truncated = EditorStyle.truncate(display, Math.max(20f, maxWidth));
        ImGui.textUnformatted(truncated);
        if (!display.equals(truncated) && ImGui.isItemHovered()) {
            ImGui.setTooltip(display);
        }
        ImGui.sameLine();
        if (ImGui.smallButton("Clear##" + label)) {
            guid = "";
        }
        ImGui.sameLine();
        boolean canEdit = guid != null && !guid.isBlank() && onEdit != null;
        if (!canEdit) {
            ImGui.beginDisabled();
        }
        if (ImGui.smallButton("Edit##" + label) && onEdit != null) {
            onEdit.run();
        }
        if (!canEdit) {
            ImGui.endDisabled();
        }
        if (ImGui.beginDragDropTarget()) {
            String payload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (payload != null) {
                StudioAsset asset = assets.get(payload);
                if (asset != null && asset.type() == AssetType.MATERIAL) {
                    guid = payload;
                }
            }
            ImGui.endDragDropTarget();
        }
        PropertyRow.end();
        return guid == null ? "" : guid;
    }
}
