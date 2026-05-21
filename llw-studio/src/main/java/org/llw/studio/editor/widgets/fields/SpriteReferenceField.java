package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Sprite sub-asset reference with drag-drop from the project browser.
 */
public final class SpriteReferenceField {
    private SpriteReferenceField() {
    }

    /**
     * @param label  field label
     * @param guid   current sprite GUID
     * @param assets asset database
     * @return updated sprite GUID
     */
    public static String draw(String label, String guid, AssetDatabase assets) {
        PropertyRow.begin(label);
        String display = guid == null || guid.isBlank() ? "None" : assets.displayName(guid);
        float maxWidth = ImGui.getContentRegionAvailX() - 52f;
        String truncated = EditorStyle.middleTruncate(display, Math.max(20f, maxWidth));
        ImGui.textUnformatted(truncated);
        if (!display.equals(truncated) && ImGui.isItemHovered()) {
            ImGui.setTooltip(display);
        }
        ImGui.sameLine();
        if (ImGui.smallButton("Clear##" + label)) {
            guid = "";
        }
        if (ImGui.beginDragDropTarget()) {
            String payload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (payload != null) {
                StudioAsset asset = assets.get(payload);
                if (asset != null && asset.type() == AssetType.SPRITE) {
                    guid = payload;
                } else if (asset != null && asset.type() == AssetType.TEXTURE) {
                    String defaultSprite = assets.defaultSpriteGuid(payload);
                    if (!defaultSprite.isBlank()) {
                        guid = defaultSprite;
                    }
                }
            }
            ImGui.endDragDropTarget();
        }
        PropertyRow.end();
        return guid == null ? "" : guid;
    }
}
