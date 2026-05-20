package org.llw.studio.editor.widgets;



import imgui.ImGui;

import imgui.type.ImString;

import org.llw.studio.editor.theme.EditorIcons;

import org.llw.studio.editor.theme.EditorColors;

import org.llw.studio.editor.theme.EditorStyle;

import org.llw.studio.editor.widgets.fields.BoolField;



/**

 * Inspector header for the selected object: icon, name, tag, and optional Active checkbox.

 */

public final class InspectorObjectHeader {

  private static final String ICON = EditorIcons.GAME_OBJECT;



  private InspectorObjectHeader() {}



  /**

   * @return updated {@code active} when {@code hasActive}; otherwise unchanged

   */

  public static boolean render(ImString nameBuffer, ImString tagBuffer, boolean active, boolean hasActive) {

    EditorStyle.pushPanelChrome();

    ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg, EditorColors.INSPECTOR_OBJECT_HEADER_BG[0],

        EditorColors.INSPECTOR_OBJECT_HEADER_BG[1], EditorColors.INSPECTOR_OBJECT_HEADER_BG[2],

        EditorColors.INSPECTOR_OBJECT_HEADER_BG[3]);

    float height = hasActive ? 80f : 56f;

    ImGui.beginChild("##inspector_object_header", 0f, height, true);



    EditorStyle.pushMutedText();

    ImGui.text(ICON);

    EditorStyle.popMutedText();

    ImGui.sameLine();

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

    ImGui.popStyleColor();

    EditorStyle.popPanelChrome();

    ImGui.spacing();

    return result;

  }

}

