package org.llw.studio.editor.theme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorMetricsTest {

    @Test
    void dockPanelsUseFlatRounding() {
        assertEquals(0f, EditorMetrics.ROUNDING_WINDOW);
        assertEquals(0f, EditorMetrics.ROUNDING_CHILD);
    }

    @Test
    void controlsUseSubtleRounding() {
        assertTrue(EditorMetrics.ROUNDING_FRAME >= 2f && EditorMetrics.ROUNDING_FRAME <= 4f);
        assertTrue(EditorMetrics.ROUNDING_TAB >= 2f && EditorMetrics.ROUNDING_TAB <= 4f);
    }

    @Test
    void hierarchyIndentMatchesPlan() {
        assertEquals(18f, EditorMetrics.INDENT_SPACING);
    }
}
