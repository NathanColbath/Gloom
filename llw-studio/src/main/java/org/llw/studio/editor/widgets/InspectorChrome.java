package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorMetrics;

/**
 * Scoped layout and chrome for the inspector scroll region (spacing, cards, object header).
 */
public final class InspectorChrome {
    private static boolean fieldSpacingActive;

    private InspectorChrome() {
    }

    /** Call once at the start of the inspector scroll area. */
    public static void beginScrollRegion() {
        pushFieldSpacing();
    }

    /** Call once when leaving the inspector scroll area. */
    public static void endScrollRegion() {
        popFieldSpacing();
    }

    /** Tighter row spacing for property fields. */
    public static void pushFieldSpacing() {
        if (fieldSpacingActive) {
            return;
        }
        fieldSpacingActive = true;
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, EditorMetrics.INSPECTOR_ITEM_SPACING_X,
                EditorMetrics.INSPECTOR_ITEM_SPACING_Y);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, EditorMetrics.INSPECTOR_FRAME_PAD_X,
                EditorMetrics.INSPECTOR_FRAME_PAD_Y);
    }

    public static void popFieldSpacing() {
        if (!fieldSpacingActive) {
            return;
        }
        fieldSpacingActive = false;
        ImGui.popStyleVar(2);
    }

    /** Gap between stacked component cards. */
    public static void sectionGap() {
        ImGui.dummy(0f, EditorMetrics.INSPECTOR_SECTION_GAP);
    }

    /** Card header strip background (child only). */
    public static void pushComponentCard() {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, EditorMetrics.INSPECTOR_CARD_ROUNDING);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, EditorColors.INSPECTOR_COMPONENT_HEADER[0],
                EditorColors.INSPECTOR_COMPONENT_HEADER[1], EditorColors.INSPECTOR_COMPONENT_HEADER[2],
                EditorColors.INSPECTOR_COMPONENT_HEADER[3]);
        ImGui.pushStyleColor(ImGuiCol.Border, EditorColors.INSPECTOR_COMPONENT_BORDER[0],
                EditorColors.INSPECTOR_COMPONENT_BORDER[1], EditorColors.INSPECTOR_COMPONENT_BORDER[2],
                EditorColors.INSPECTOR_COMPONENT_BORDER[3]);
    }

    public static void popComponentCard() {
        ImGui.popStyleColor(2);
        ImGui.popStyleVar();
    }
}
