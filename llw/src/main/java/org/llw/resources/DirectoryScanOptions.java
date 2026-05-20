package org.llw.resources;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Options for {@link ResourceManager#registerDirectory} and classpath directory registration.
 *
 * @param defaultFontPixelHeight font raster size for {@code .ttf}/{@code .otf} files
 * @param musicExtensions        file extensions treated as {@link AssetType#MUSIC} instead of sound
 * @param ignored                file names to skip (e.g. {@code _index.json})
 */
public record DirectoryScanOptions(int defaultFontPixelHeight, Set<String> musicExtensions, Set<String> ignored) {
    /** Default scan options: 24px fonts, {@code .ogg} as music. */
    public static final DirectoryScanOptions DEFAULT = new DirectoryScanOptions(
            24,
            Set.of(".ogg"),
            Set.of("_index.json")
    );

    public DirectoryScanOptions {
        musicExtensions = Set.copyOf(normalizeExtensions(musicExtensions));
        ignored = Set.copyOf(ignored);
        if (defaultFontPixelHeight <= 0) {
            throw new IllegalArgumentException("defaultFontPixelHeight must be positive");
        }
    }

    /**
     * Infers asset type from a filename extension.
     *
     * @param fileName file name or path tail
     * @return inferred type, or empty if unsupported
     */
    public java.util.Optional<AssetType> inferType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        for (String ignoredName : ignored) {
            if (lower.endsWith(ignoredName) || lower.equals(ignoredName)) {
                return java.util.Optional.empty();
            }
        }
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return java.util.Optional.of(AssetType.TEXTURE);
        }
        if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
            return java.util.Optional.of(AssetType.FONT);
        }
        if (lower.endsWith(".wav")) {
            return java.util.Optional.of(AssetType.SOUND);
        }
        for (String ext : musicExtensions) {
            if (lower.endsWith(ext)) {
                return java.util.Optional.of(AssetType.MUSIC);
            }
        }
        if (lower.endsWith(".ogg")) {
            return java.util.Optional.of(AssetType.SOUND);
        }
        return java.util.Optional.empty();
    }

    private static Set<String> normalizeExtensions(Set<String> extensions) {
        Set<String> normalized = new HashSet<>();
        for (String ext : extensions) {
            String e = ext.startsWith(".") ? ext : "." + ext;
            normalized.add(e.toLowerCase(Locale.ROOT));
        }
        return normalized;
    }
}
