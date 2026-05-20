package org.llw.render.graphics.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs(OS.WINDOWS)
class WindowsSystemFontResolverTest {

    @Test
    void scanFindsSegoeUi() {
        WindowsSystemFontResolver resolver = new WindowsSystemFontResolver();
        assertTrue(resolver.isSupported());

        var faces = resolver.scan();
        assertFalse(faces.isEmpty());

        var segoe = faces.stream()
                .filter(face -> "Segoe UI".equals(face.family()) && face.style() == org.llw.render.graphics.FontStyle.PLAIN)
                .findFirst();
        assertTrue(segoe.isPresent());
        assertTrue(Files.isRegularFile(segoe.get().path()));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Segoe UI",
                SystemFonts.faceName(segoe.get().family(), segoe.get().style())
        );
    }

    @Test
    void scanCatalogContainsSegoeUiFace() {
        var catalog = SystemFonts.scanCatalog();
        assertTrue(catalog.containsKey("Segoe UI"));
        Path path = catalog.get("Segoe UI");
        assertNotNull(path);
        assertTrue(Files.isRegularFile(path));
    }
}
