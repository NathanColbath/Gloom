package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * Read-only asset GUID display with clear button and drag-drop assignment from the project browser.
 */
public final class AssetReferenceField {
  private AssetReferenceField() {}

  public static String draw(String label, String guid, AssetDatabase assets) {
    PropertyRow.begin(label);
    String display = guid == null || guid.isBlank() ? "None" : assets.displayName(guid);
    float maxWidth = ImGui.getContentRegionAvailX() - 52f;
    String truncated = EditorStyle.truncate(display, Math.max(20f, maxWidth));
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
        guid = payload;
      }
      ImGui.endDragDropTarget();
    }
    PropertyRow.end();
    return guid == null ? "" : guid;
  }
}
