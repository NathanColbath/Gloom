package org.llw.studio.editor.theme;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads Inter as the default UI font and merges Font Awesome icons for {@link EditorIcons}.
 */
public final class EditorFonts {
    private static final String INTER_PATH = "/fonts/Inter-Regular.ttf";
    private static final String ICON_PATH = "/fonts/fa-solid-900.ttf";

    private EditorFonts() {
    }

    public static void load() {
        ImGuiIO io = ImGui.getIO();
        float size = EditorMetrics.FONT_SIZE_UI;

        byte[] interFont = loadResource(INTER_PATH);
        ImFontConfig uiConfig = new ImFontConfig();
        uiConfig.setPixelSnapH(true);
        uiConfig.setOversampleH(2);
        uiConfig.setOversampleV(2);
        io.getFonts().addFontFromMemoryTTF(interFont, size, uiConfig, io.getFonts().getGlyphRangesDefault());
        uiConfig.destroy();

        byte[] iconFont = loadResource(ICON_PATH);
        ImFontConfig iconConfig = new ImFontConfig();
        iconConfig.setMergeMode(true);
        iconConfig.setPixelSnapH(true);
        iconConfig.setGlyphMinAdvanceX(EditorMetrics.ICON_MIN_ADVANCE);
        iconConfig.setGlyphMaxAdvanceX(EditorMetrics.ICON_MIN_ADVANCE);
        io.getFonts().addFontFromMemoryTTF(iconFont, size, iconConfig, EditorIcons.ICON_RANGE);
        iconConfig.destroy();

        io.getFonts().build();
    }

    private static byte[] loadResource(String path) {
        try (InputStream in = EditorFonts.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing font resource: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load font resource: " + path, e);
        }
    }
}
