package org.llw.render.graphics.system;

import org.llw.render.graphics.FontStyle;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Facade for discovering host system fonts and building stable face/asset ids.
 */
public final class SystemFonts {
    private static final SystemFontResolver RESOLVER = selectResolver();

    private SystemFonts() {
    }

    /**
     * Builds the catalog key for a family/style pair.
     *
     * @param family font family name
     * @param style  logical style
     * @return face name used in the system font catalog
     */
    public static String faceName(String family, FontStyle style) {
        if (style == FontStyle.PLAIN) {
            return family;
        }
        return family + styleSuffix(style);
    }

    /**
     * Builds the internal asset id for a lazily registered sized system font.
     *
     * @param family      font family name
     * @param style       logical style
     * @param pixelHeight rasterized glyph height in pixels
     * @return internal asset id
     */
    public static String assetId(String family, FontStyle style, int pixelHeight) {
        return "sys::" + faceName(family, style) + "@" + pixelHeight;
    }

    /**
     * Scans installed system fonts and returns a face-name catalog.
     *
     * @return immutable map of face name to font file path
     */
    public static Map<String, Path> scanCatalog() {
        Map<String, Path> catalog = new LinkedHashMap<>();
        if (!RESOLVER.isSupported()) {
            return Map.copyOf(catalog);
        }
        if (RESOLVER instanceof WindowsSystemFontResolver windows) {
            for (SystemFontFace face : windows.scanKnownUiFonts()) {
                catalog.putIfAbsent(faceName(face.family(), face.style()), face.path());
            }
            if (catalog.containsKey("Segoe UI")) {
                return Map.copyOf(catalog);
            }
        }
        for (SystemFontFace face : RESOLVER.scan()) {
            catalog.putIfAbsent(faceName(face.family(), face.style()), face.path());
        }
        return Map.copyOf(catalog);
    }

  /**
   * @return whether system font scanning is supported on this host
   */
    public static boolean isSupported() {
        return RESOLVER.isSupported();
    }

    private static String styleSuffix(FontStyle style) {
        return switch (style) {
            case BOLD -> ".bold";
            case ITALIC -> ".italic";
            case BOLD_ITALIC -> ".bold-italic";
            case PLAIN -> "";
        };
    }

    private static SystemFontResolver selectResolver() {
        SystemFontResolver windows = new WindowsSystemFontResolver();
        if (windows.isSupported()) {
            return windows;
        }
        return new UnsupportedSystemFontResolver();
    }

    private static final class UnsupportedSystemFontResolver implements SystemFontResolver {
        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public List<SystemFontFace> scan() {
            return List.of();
        }
    }
}
