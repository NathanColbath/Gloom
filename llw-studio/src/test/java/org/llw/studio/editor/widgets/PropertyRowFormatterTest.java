package org.llw.studio.editor.widgets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PropertyRowFormatter}.
 * Pure-Java tests — no ImGui native bindings needed.
 */
class PropertyRowFormatterTest {

    // ── null / empty ──────────────────────────────────────────

    @Test
    void formatLabel_returnsEmpty_forNull() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel(null, 14);
        assertEquals("", r.display);
        assertNull(r.tooltip);
    }

    @Test
    void formatLabel_returnsEmpty_forEmpty() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("", 14);
        assertEquals("", r.display);
        assertNull(r.tooltip);
    }

    // ── fits within limit ─────────────────────────────────────

    @Test
    void formatLabel_returnsTextAsIs_whenTextFits() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("Hello", 14);
        assertEquals("Hello", r.display);
        assertNull(r.tooltip);
    }

    @Test
    void formatLabel_returnsTextAsIs_whenTextExactlyFits() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("Hello world!", 14);
        assertEquals("Hello world!", r.display);
        assertNull(r.tooltip);
    }

    // ── exceeds limit ─────────────────────────────────────────

    @Test
    void formatLabel_truncatesWithEllipsis_whenTextExceedsLimit() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel(
                "A very long inspector label that should be truncated", 14);
        assertEquals(14, r.display.length());
        assertTrue(r.display.contains("..."));
        assertNotNull(r.tooltip);
    }

    @Test
    void formatLabel_setsTooltipToOriginal_whenTruncated() {
        String original = "This is a very long label for testing purposes";
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel(original, 14);
        assertEquals(original, r.tooltip);
    }

    @Test
    void formatLabel_maxCharsOf3_stillWorks() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("abcdefghij", 3);
        assertEquals(3, r.display.length());
        assertEquals("abc", r.display);
        assertNotNull(r.tooltip);
    }

    @Test
    void formatLabel_preservesSuffix_forLongLabels() {
        // maxChars=14, ellipsis=3, remaining=11, left=5, right=6
        String original = "asset/textures/character/walk_cycle.png";
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel(original, 14);
        assertEquals(14, r.display.length());
        assertTrue(r.display.contains("..."));
        // Should end with something meaningful (suffix preserved)
        String suffix = original.substring(original.length() - 6);
        assertTrue(r.display.endsWith(suffix), "Expected display to end with '" + suffix + "' but got '" + r.display + "'");
    }

    // ── whitespace ────────────────────────────────────────────

    @Test
    void formatLabel_handlesWhitespaceOnly() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("   ", 14);
        // Whitespace is still valid text — just return it if it fits
        assertEquals("   ", r.display);
        assertNull(r.tooltip);
    }

    // ── edge: very small maxChars ─────────────────────────────

    @Test
    void formatLabel_maxCharsOf2() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("abcdef", 2);
        assertEquals("ab", r.display);
        assertNotNull(r.tooltip);
    }

    @Test
    void formatLabel_maxCharsOf1() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("abcdef", 1);
        assertEquals("a", r.display);
        assertNotNull(r.tooltip);
    }

    @Test
    void formatLabel_maxCharsOf0() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("abcdef", 0);
        assertEquals("", r.display);
        assertNotNull(r.tooltip);
    }

    @Test
    void formatLabel_negativeMaxChars() {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel("abcdef", -1);
        assertEquals("", r.display);
        // Tooltip should show the original text so user can discover it via hover
        assertEquals("abcdef", r.tooltip);
    }

    @ParameterizedTest
    @CsvSource({
            "abcdefghijklmnop, 14, 14",
            "short,          14, 5",
            "a,              14, 1",
            "a,               1, 1",
    })
    void formatLabel_displayLengthNeverExceedsMaxChars(String text, int maxChars, int expectedLen) {
        PropertyRowFormatter.FormatResult r = PropertyRowFormatter.formatLabel(text, maxChars);
        assertTrue(r.display.length() <= maxChars,
                "Display length " + r.display.length() + " exceeds maxChars " + maxChars);
    }
}
