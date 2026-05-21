package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.panels.ShaderGraphPanel;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Shader graph asset picker with drag-drop filtering and an Edit shortcut.
 */
public final class ShaderGraphReferenceField {
    private ShaderGraphReferenceField() {
    }

    public static String draw(String label, String guid, AssetDatabase assets, ShaderGraphPanel shaderGraphPanel) {
        PropertyRow.begin(label);
        String display = guid == null || guid.isBlank() ? "None" : assets.displayName(guid);
        float maxWidth = ImGui.getContentRegionAvailX() - 96f;
        String truncated = EditorStyle.middleTruncate(display, Math.max(20f, maxWidth));
        ImGui.textUnformatted(truncated);
        if (!display.equals(truncated) && ImGui.isItemHovered()) {
            ImGui.setTooltip(display);
        }
        ImGui.sameLine();
        if (ImGui.smallButton("Clear##" + label)) {
            guid = "";
        }
        ImGui.sameLine();
        boolean canEdit = guid != null && !guid.isBlank() && shaderGraphPanel != null;
        if (!canEdit) {
            ImGui.beginDisabled();
        }
        if (ImGui.smallButton("Edit##" + label)) {
            StudioAsset asset = assets.get(guid);
            if (asset != null && asset.type() == AssetType.SHADER_GRAPH) {
                shaderGraphPanel.openAsset(asset.guid(), asset.path());
            }
        }
        if (!canEdit) {
            ImGui.endDisabled();
        }
        if (ImGui.beginDragDropTarget()) {
            String payload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (payload != null) {
                StudioAsset asset = assets.get(payload);
                if (asset != null && asset.type() == AssetType.SHADER_GRAPH) {
                    guid = payload;
                }
            }
            ImGui.endDragDropTarget();
        }
        PropertyRow.end();
        return guid == null ? "" : guid;
    }
}
