package org.llw.studio.editor.theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EditorStyle} text formatting utilities.
 * These tests cover the character-count-based {@code middleTruncate(String, int)}
 * method, which does NOT depend on ImGui native bindings.
 */
class EditorStyleTest {

    // --------------- middleTruncate(String, int) ---------------

    @Test
    void middleTruncate_returnsEmpty_forNull() {
        assertEquals("", EditorStyle.middleTruncate(null, 10));
    }

    @Test
    void middleTruncate_returnsEmpty_forEmpty() {
        assertEquals("", EditorStyle.middleTruncate("", 10));
    }

    @Test
    void middleTruncate_returnsTextAsIs_whenTextFits() {
        assertEquals("hello", EditorStyle.middleTruncate("hello", 10));
    }

    @Test
    void middleTruncate_returnsTextAsIs_whenTextExactlyFits() {
        assertEquals("abcdefghij", EditorStyle.middleTruncate("abcdefghij", 10));
    }

    @Test
    void middleTruncate_preservesSuffix_whenTextExceedsLimit() {
        // Path scenario: textures/characters/player/walk_cycle.png (41 chars)
        // maxChars=25, left=11, right=11
        // → "textures/ch...k_cycle.png"
        String result = EditorStyle.middleTruncate("textures/characters/player/walk_cycle.png", 25);
        assertEquals(25, result.length());
        assertTrue(result.startsWith("textures/ch"));
        assertTrue(result.endsWith("k_cycle.png"));
        assertTrue(result.contains("..."));
    }

    @Test
    void middleTruncate_preservesFileExtension_whenMaxCharsSmall() {
        // Short but still long enough to need truncation
        // "player_sprite_atlas.png" (22 chars) at maxChars=16
        // left=(16-3)/2=6, right=16-3-6=7
        // → "player...as.png" (preserves ".png" extension)
        String result = EditorStyle.middleTruncate("player_sprite_atlas.png", 16);
        assertEquals(16, result.length());
        assertTrue(result.endsWith(".png"));
        assertTrue(result.contains("..."));
    }

    @Test
    void middleTruncate_fallsBackToEndTruncation_whenMaxCharsTooSmall() {
        // maxChars=3 (<4): no room for "...", do end-truncation
        assertEquals("abc", EditorStyle.middleTruncate("abcdefghij", 3));
    }

    @Test
    void middleTruncate_returnsExactText_whenMaxCharsExactlyLength() {
        assertEquals("abc", EditorStyle.middleTruncate("abc", 3));
    }

    @Test
    void middleTruncate_preservesBothPrefixAndSuffix() {
        // "prefix.middlecontent.suffix" (25 chars) at maxChars=15
        // left=(15-3)/2=6, right=15-3-6=6
        // → "prefix...suffix"
        String result = EditorStyle.middleTruncate("prefix.middlecontent.suffix", 15);
        assertEquals(15, result.length());
        assertTrue(result.startsWith("prefix"));
        assertTrue(result.endsWith("suffix"));
        assertTrue(result.contains("..."));
    }

    @Test
    void middleTruncate_handlesSingleCharacterSuffix() {
        // "a.b.c.d.e.f.g.h.i.j.k.l.m.n.o" (27 chars) at maxChars=15
        // left=6, right=6
        // Should include the last 6 characters
        String text = "a.b.c.d.e.f.g.h.i.j.k.l.m.n.o";
        String result = EditorStyle.middleTruncate(text, 15);
        assertEquals(15, result.length());
        assertTrue(result.endsWith("m.n.o"));
    }

    @Test
    void middleTruncate_handlesMaxCharsOf4() {
        // maxChars=4: exactly enough for "a...b"?
        // remaining = 4-3 = 1, left=0, right=1
        // → "..." + last char → "...b"
        String result = EditorStyle.middleTruncate("abcdefgh", 4);
        assertEquals(4, result.length());
        assertEquals(1, result.replace("...", "").length());
        assertEquals('h', result.charAt(3));
    }

    @ParameterizedTest
    @CsvSource({
            "1,          1, a",
            "2,          2, aa",
            "3,          3, aaa",
    })
    void middleTruncate_shortStrings(int length, int maxChars, String expected) {
        String input = "a".repeat(length);
        assertEquals(expected, EditorStyle.middleTruncate(input, maxChars));
    }
}
