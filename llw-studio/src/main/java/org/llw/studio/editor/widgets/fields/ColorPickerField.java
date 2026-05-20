package org.llw.studio.editor.widgets.fields;

import imgui.ImGui;
import org.llw.studio.editor.widgets.PropertyRow;

/**
 * RGBA color property using ImGui's color picker widget.
 */
public final class ColorPickerField {
    private ColorPickerField() {
    }

    public static float[] draw(String label, float r, float g, float b, float a) {
        PropertyRow.begin(label);
        float[] rgba = {r, g, b, a};
        ImGui.colorEdit4("##color", rgba);
        PropertyRow.end();
        return rgba;
    }
}
