package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.theme.EditorMetrics;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.theme.ThemeColors;
import org.llw.studio.editor.widgets.fields.BoolField;

/**
 * Inspector header for the selected object: icon, name, tag, and optional Active checkbox.
 */
public final class InspectorObjectHeader {
    private static final String ICON = EditorIcons.GAME_OBJECT;

    private InspectorObjectHeader() {
    }

    /**
     * @return updated {@code active} when {@code hasActive}; otherwise unchanged
     */
    public static boolean render(ImString nameBuffer, ImString tagBuffer, boolean active, boolean hasActive) {
        float width = ImGui.getContentRegionAvailX();
        float height = hasActive ? 88f : 64f;

        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.ChildRounding, EditorMetrics.INSPECTOR_CARD_ROUNDING);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg, EditorColors.INSPECTOR_OBJECT_HEADER_BG[0],
                EditorColors.INSPECTOR_OBJECT_HEADER_BG[1], EditorColors.INSPECTOR_OBJECT_HEADER_BG[2],
                EditorColors.INSPECTOR_OBJECT_HEADER_BG[3]);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border, EditorColors.INSPECTOR_COMPONENT_BORDER[0],
                EditorColors.INSPECTOR_COMPONENT_BORDER[1], EditorColors.INSPECTOR_COMPONENT_BORDER[2],
                EditorColors.INSPECTOR_COMPONENT_BORDER[3]);
        ImGui.beginChild("##inspector_object_header", width, height, true);

        ImGui.dummy(0f, 4f);
        EditorStyle.pushMutedText();
        ImGui.text(ICON);
        EditorStyle.popMutedText();
        ImGui.sameLine(0f, 8f);

        PropertyRow.begin("Name");
        ImGui.inputText("##object_name", nameBuffer);
        PropertyRow.end();

        PropertyRow.begin("Tag");
        ImGui.inputText("##object_tag", tagBuffer);
        PropertyRow.end();

        boolean result = active;
        if (hasActive) {
            result = BoolField.draw("Active", active);
        }

        ImGui.endChild();
        ImGui.popStyleColor(2);
        ImGui.popStyleVar();

        ImGui.dummy(0f, EditorMetrics.INSPECTOR_SECTION_GAP);
        float sepX = ImGui.getCursorScreenPosX();
        float sepY = ImGui.getCursorScreenPosY();
        float sepW = ImGui.getContentRegionAvailX();
        ImGui.getWindowDrawList().addLine(sepX, sepY, sepX + sepW, sepY, ThemeColors.toU32(EditorColors.BORDER), 1f);
        ImGui.dummy(0f, EditorMetrics.INSPECTOR_SECTION_GAP);
        return result;
    }
}
