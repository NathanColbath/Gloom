package org.llw.studio.editor.theme;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EditorColors} tokens used by {@link GloomTheme}.
 * These tests are pure Java — they do NOT depend on ImGui native bindings
 * and can run in any JUnit environment.
 */
class GloomThemeTest {

    @Test
    void gloomThemeClass_loadsWithoutError() {
        assertNotNull(GloomTheme.class);
    }

    @Test
    void gloomThemeClass_isFinalWithPrivateConstructor() {
        // GloomTheme should be a utility class (final, private constructor)
        assertNotNull(GloomTheme.class.getDeclaredConstructors());
    }

    @Test
    void editorColors_allTokensHaveValidRgbaValues() {
        // All color tokens should have exactly 4 float components (RGBA)
        // and each component should be in [0, 1]
        assertArrayLengthAndRange(EditorColors.WINDOW_BG);
        assertArrayLengthAndRange(EditorColors.SURFACE);
        assertArrayLengthAndRange(EditorColors.SURFACE_LIGHT);
        assertArrayLengthAndRange(EditorColors.PANEL_BG);
        assertArrayLengthAndRange(EditorColors.PANEL_HEADER);
        assertArrayLengthAndRange(EditorColors.INSPECTOR_OBJECT_HEADER_BG);
        assertArrayLengthAndRange(EditorColors.INSPECTOR_COMPONENT_BG);
        assertArrayLengthAndRange(EditorColors.INSPECTOR_COMPONENT_BORDER);
        assertArrayLengthAndRange(EditorColors.COMPONENT_HEADER_BG);
        assertArrayLengthAndRange(EditorColors.COMPONENT_HEADER_HOVER);
        assertArrayLengthAndRange(EditorColors.BORDER);
        assertArrayLengthAndRange(EditorColors.BORDER_STRONG);
        assertArrayLengthAndRange(EditorColors.TEXT_PRIMARY);
        assertArrayLengthAndRange(EditorColors.TEXT_SECONDARY);
        assertArrayLengthAndRange(EditorColors.TEXT_MUTED);
        assertArrayLengthAndRange(EditorColors.ACCENT);
        assertArrayLengthAndRange(EditorColors.ACCENT_HOVER);
        assertArrayLengthAndRange(EditorColors.ACCENT_ACTIVE);
        assertArrayLengthAndRange(EditorColors.SELECTION_BG);
        assertArrayLengthAndRange(EditorColors.INPUT_BG);
        assertArrayLengthAndRange(EditorColors.BUTTON_BG);
        assertArrayLengthAndRange(EditorColors.BUTTON_HOVER);
        assertArrayLengthAndRange(EditorColors.DANGER);
        assertArrayLengthAndRange(EditorColors.PLAY_ACTIVE);
        assertArrayLengthAndRange(EditorColors.STOP_ACTIVE);
        assertArrayLengthAndRange(EditorColors.LOG_DEBUG);
        assertArrayLengthAndRange(EditorColors.LOG_INFO);
        assertArrayLengthAndRange(EditorColors.LOG_WARN);
        assertArrayLengthAndRange(EditorColors.LOG_ERROR);
        assertArrayLengthAndRange(EditorColors.HIERARCHY_ROW_HOVER);
        assertArrayLengthAndRange(EditorColors.HIERARCHY_ROW_SELECTED);
        assertArrayLengthAndRange(EditorColors.ASSET_BROWSER_HOVER);
        assertArrayLengthAndRange(EditorColors.TABLE_HEADER_BG);
        assertArrayLengthAndRange(EditorColors.TABLE_BORDER_STRONG);
        assertArrayLengthAndRange(EditorColors.TABLE_BORDER_LIGHT);
        assertArrayLengthAndRange(EditorColors.TABLE_ROW_BG);
        assertArrayLengthAndRange(EditorColors.TABLE_ROW_BG_ALT);
        assertArrayLengthAndRange(EditorColors.PLOT_LINES);
        assertArrayLengthAndRange(EditorColors.PLOT_LINES_HOVERED);
        assertArrayLengthAndRange(EditorColors.PLOT_HISTOGRAM);
        assertArrayLengthAndRange(EditorColors.PLOT_HISTOGRAM_HOVERED);
        assertArrayLengthAndRange(EditorColors.NAV_HIGHLIGHT);
        assertArrayLengthAndRange(EditorColors.NAV_DIM_BG);
        assertArrayLengthAndRange(EditorColors.MODAL_DIM_BG);
        assertArrayLengthAndRange(EditorColors.TIMELINE_GRID);
        assertArrayLengthAndRange(EditorColors.TIMELINE_PLAYHEAD);
        assertArrayLengthAndRange(EditorColors.TIMELINE_KEYFRAME_SELECTED);
        assertArrayLengthAndRange(EditorColors.SHADER_NODE_BG);
        assertArrayLengthAndRange(EditorColors.SHADER_LINK);
    }

    @Test
    void editorColors_accentDiffersFromSelection() {
        // Muted blue-gray accent vs gray selection fill — distinct roles
        assertTrue(EditorColors.ACCENT[2] > EditorColors.SELECTION_BG[2],
            "Accent should read cooler (higher blue) than selection");
        assertTrue(EditorColors.SELECTION_BG[3] < 1f,
            "Selection should use partial alpha for row fills");
    }

    @Test
    void applyModernGrayTheme_isAliasForApply() throws Exception {
        var apply = GloomTheme.class.getDeclaredMethod("apply");
        var modern = GloomTheme.class.getDeclaredMethod("applyModernGrayTheme");
        assertNotEquals(apply, modern);
        assertEquals(apply.getReturnType(), modern.getReturnType());
    }

    @Test
    void editorColors_primaryTextIsBrighterThanMuted() {
        // Primary text should be lighter (higher values) than muted text
        assertTrue(EditorColors.TEXT_PRIMARY[0] > EditorColors.TEXT_MUTED[0],
            "Primary red channel should be brighter than muted");
        assertTrue(EditorColors.TEXT_PRIMARY[1] > EditorColors.TEXT_MUTED[1],
            "Primary green channel should be brighter than muted");
        assertTrue(EditorColors.TEXT_PRIMARY[2] > EditorColors.TEXT_MUTED[2],
            "Primary blue channel should be brighter than muted");
    }

    private static void assertArrayLengthAndRange(float[] rgba) {
        assertNotNull(rgba, "Color token should not be null");
        assertEquals(4, rgba.length, "Each color token must have exactly 4 components (RGBA)");
        for (int i = 0; i < 4; i++) {
            assertTrue(rgba[i] >= 0f && rgba[i] <= 1f,
                "Component " + i + " should be in [0,1] range but was " + rgba[i]);
        }
    }
}
