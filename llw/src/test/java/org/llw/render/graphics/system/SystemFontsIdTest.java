package org.llw.render.graphics.system;

import org.llw.render.graphics.FontStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemFontsIdTest {

    @Test
    void faceNamePlainHasNoSuffix() {
        assertEquals("Segoe UI", SystemFonts.faceName("Segoe UI", FontStyle.PLAIN));
    }

    @Test
    void faceNameStyledVariants() {
        assertEquals("Segoe UI.bold", SystemFonts.faceName("Segoe UI", FontStyle.BOLD));
        assertEquals("Segoe UI.italic", SystemFonts.faceName("Segoe UI", FontStyle.ITALIC));
        assertEquals("Segoe UI.bold-italic", SystemFonts.faceName("Segoe UI", FontStyle.BOLD_ITALIC));
    }

    @Test
    void assetIdIncludesPrefixSizeAndFace() {
        assertEquals("sys::Segoe UI@28", SystemFonts.assetId("Segoe UI", FontStyle.PLAIN, 28));
        assertEquals("sys::Segoe UI.bold@36", SystemFonts.assetId("Segoe UI", FontStyle.BOLD, 36));
    }
}
