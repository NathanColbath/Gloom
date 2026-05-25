package org.llw.studio.editor.theme;

import imgui.ImGui;

/**
 * RGBA helpers for {@link EditorColors} and draw-list rendering.
 */
public final class ThemeColors {
    private ThemeColors() {
    }

    /** @return RGBA components in 0–1 range */
    public static float[] rgba(int r, int g, int b, int a) {
        return new float[]{
                r / 255f,
                g / 255f,
                b / 255f,
                a / 255f
        };
    }

    /** @return RGBA with full opacity */
    public static float[] rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    /** @return packed ARGB color for {@link imgui.ImDrawList} */
    public static int toU32(float[] rgba) {
        return ImGui.colorConvertFloat4ToU32(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /** @return packed ARGB from 0–255 components */
    public static int toU32(int r, int g, int b, int a) {
        return toU32(rgba(r, g, b, a));
    }
}
