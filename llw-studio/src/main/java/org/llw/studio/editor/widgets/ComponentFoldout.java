package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.theme.EditorMetrics;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.theme.ThemeColors;

import java.util.HashMap;
import java.util.Map;

/**
 * Inspector component card with a fixed-height header strip and optional body.
 */
public final class ComponentFoldout {
    private static final Map<String, Boolean> openStates = new HashMap<>();
    private static boolean firstCardInRegion = true;

    private static float cardLeftX;
    private static float cardTopY;
    private static float cardRightX;
    private static boolean cardOutlinePending;

    private ComponentFoldout() {
    }

    /** Resets spacing before the first component card in a scroll region. */
    public static void resetCardSpacing() {
        firstCardInRegion = true;
    }

    /** Result of {@link #header(String, String, String, boolean, boolean)}. */
    public static final class State {
        private final boolean open;
        private final boolean removeClicked;
        private final boolean optionsClicked;

        State(boolean open, boolean removeClicked, boolean optionsClicked) {
            this.open = open;
            this.removeClicked = removeClicked;
            this.optionsClicked = optionsClicked;
        }

        public boolean open() {
            return open;
        }

        public boolean removeClicked() {
            return removeClicked;
        }

        public boolean optionsClicked() {
            return optionsClicked;
        }
    }

    /**
     * Opens a component card. Pair with {@link #endComponent()} after {@link #endBody()} (if any).
     */
    public static State header(String key, String title, String icon, boolean defaultOpen, boolean removable) {
        if (!firstCardInRegion) {
            InspectorChrome.sectionGap();
        } else {
            firstCardInRegion = false;
        }

        ImGui.pushID(key);
        boolean open = openStates.getOrDefault(key, defaultOpen);

        float width = ImGui.getContentRegionAvailX();
        float headerH = ImGui.getFrameHeight() + EditorMetrics.INSPECTOR_HEADER_PAD_Y * 2f;

        cardLeftX = ImGui.getCursorScreenPosX();
        cardTopY = ImGui.getCursorScreenPosY();
        cardRightX = cardLeftX + width;
        cardOutlinePending = true;

        InspectorChrome.pushComponentCard();
        ImGui.beginChild("##hdr", width, headerH, false);

        boolean removeClicked = false;
        boolean optionsClicked = false;
        int columns = removable ? 3 : 2;
        int tableFlags = ImGuiTableFlags.SizingStretchProp
                | ImGuiTableFlags.NoBordersInBodyUntilResize
                | ImGuiTableFlags.NoPadOuterX;
        if (ImGui.beginTable("##hdr_tbl", columns, tableFlags)) {
            if (removable) {
                ImGui.tableSetupColumn("title", imgui.flag.ImGuiTableColumnFlags.WidthStretch);
                ImGui.tableSetupColumn("opt", imgui.flag.ImGuiTableColumnFlags.WidthFixed, 24f);
                ImGui.tableSetupColumn("rm", imgui.flag.ImGuiTableColumnFlags.WidthFixed, 24f);
            } else {
                ImGui.tableSetupColumn("title", imgui.flag.ImGuiTableColumnFlags.WidthStretch);
                ImGui.tableSetupColumn("opt", imgui.flag.ImGuiTableColumnFlags.WidthFixed, 24f);
            }
            ImGui.tableNextRow();

            ImGui.tableNextColumn();
            EditorStyle.pushComponentHeader();
            if (ImGui.arrowButton("##foldout", open ? imgui.flag.ImGuiDir.Down : imgui.flag.ImGuiDir.Right)) {
                open = !open;
            }
            ImGui.sameLine(0f, 4f);
            if (icon != null && !icon.isBlank()) {
                EditorStyle.pushMutedText();
                ImGui.text(icon);
                EditorStyle.popMutedText();
                ImGui.sameLine(0f, 6f);
            }
            ImGui.alignTextToFramePadding();
            ImGui.textUnformatted(title);
            EditorStyle.popComponentHeader();

            ImGui.tableNextColumn();
            if (ImGui.smallButton(EditorIcons.ELLIPSIS + "##options")) {
                optionsClicked = true;
            }

            if (removable) {
                ImGui.tableNextColumn();
                if (ImGui.smallButton(EditorIcons.TIMES + "##remove")) {
                    removeClicked = true;
                }
            }
            ImGui.endTable();
        }

        ImGui.endChild();
        InspectorChrome.popComponentCard();

        if (open) {
            float sepX = ImGui.getCursorScreenPosX();
            float sepY = ImGui.getCursorScreenPosY();
            float sepW = ImGui.getContentRegionAvailX();
            ImGui.getWindowDrawList().addLine(sepX, sepY, sepX + sepW, sepY,
                    ThemeColors.toU32(EditorColors.INSPECTOR_COMPONENT_BORDER), 1f);
            ImGui.dummy(0f, 1f);
        }

        openStates.put(key, open);
        return new State(open, removeClicked, optionsClicked);
    }

    public static void beginBody() {
        ImGui.indent(EditorMetrics.INSPECTOR_BODY_INDENT);
        ImGui.dummy(0f, 4f);
    }

    public static void endBody() {
        ImGui.dummy(0f, EditorMetrics.INSPECTOR_BODY_PAD_BOTTOM);
        ImGui.unindent(EditorMetrics.INSPECTOR_BODY_INDENT);
        finishCardOutline();
    }

    /** Closes the card when the body was not shown (collapsed). */
    public static void endComponent() {
        if (cardOutlinePending) {
            finishCardOutline();
        }
        ImGui.popID();
    }

    private static void finishCardOutline() {
        if (!cardOutlinePending) {
            return;
        }
        float bottom = ImGui.getCursorScreenPosY();
        if (bottom > cardTopY) {
            var drawList = ImGui.getWindowDrawList();
            int border = ThemeColors.toU32(EditorColors.INSPECTOR_COMPONENT_BORDER);
            float r = EditorMetrics.INSPECTOR_CARD_ROUNDING;
            drawList.addRect(cardLeftX, cardTopY, cardRightX, bottom, border, r, 0, 1f);
        }
        cardOutlinePending = false;
    }
}
