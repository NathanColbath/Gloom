package org.llw.render.graphics.system;

import org.llw.render.graphics.FontStyle;
import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Scans {@code %WINDIR%\Fonts} and indexes faces by family name and style.
 */
public final class WindowsSystemFontResolver implements SystemFontResolver {
    private static final Logger log = Log.get(Loggers.SYSTEM_FONTS);

    private static final String[] EXTENSIONS = {".ttf", ".otf"};

    private static final String[] KNOWN_UI_FONT_FILES = {
            "segoeui.ttf",
            "segoeuib.ttf",
            "segoeuii.ttf",
            "segoeuiz.ttf",
            "arial.ttf",
            "arialbd.ttf",
            "ariali.ttf",
            "arialbi.ttf",
            "consola.ttf",
            "consolab.ttf",
            "consolai.ttf",
            "consolaz.ttf",
    };

    private List<SystemFontFace> cached;
    private List<SystemFontFace> knownUiCached;

    @Override
    public boolean isSupported() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win");
    }

    @Override
    public synchronized List<SystemFontFace> scan() {
        if (cached != null) {
            return List.copyOf(cached);
        }
        if (!isSupported()) {
            cached = List.of();
            return cached;
        }

        String windir = System.getenv("WINDIR");
        if (windir == null || windir.isBlank()) {
            windir = "C:\\Windows";
        }
        Path fontsDir = Path.of(windir, "Fonts");
        if (!Files.isDirectory(fontsDir)) {
            log.warn("Windows fonts directory not found: {}", fontsDir);
            cached = List.of();
            return cached;
        }

        log.debug("Scanning Windows fonts directory: {}", fontsDir.toAbsolutePath());
        Map<String, SystemFontFace> faces = new LinkedHashMap<>();
        int filesScanned = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fontsDir)) {
            for (Path file : stream) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!hasFontExtension(name)) {
                    continue;
                }
                filesScanned++;
                probeFile(file).ifPresentOrElse(
                        face -> {
                            faces.putIfAbsent(SystemFonts.faceName(face.family(), face.style()), face);
                            log.debug("Indexed font family={} style={} path={}", face.family(), face.style(), file.toAbsolutePath());
                        },
                        () -> log.warn("Skipped font probe failed: {}", file.getFileName())
                );
            }
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to scan Windows fonts directory: " + fontsDir, e);
        }

        cached = List.copyOf(faces.values());
        log.info("Windows font scan complete: filesScanned={} facesIndexed={}", filesScanned, cached.size());
        return cached;
    }

    /**
     * Probes only common UI font files instead of every face under {@code %WINDIR%\\Fonts}.
     *
     * @return discovered faces from the known UI font list
     */
    public synchronized List<SystemFontFace> scanKnownUiFonts() {
        if (knownUiCached != null) {
            return List.copyOf(knownUiCached);
        }
        if (!isSupported()) {
            knownUiCached = List.of();
            return knownUiCached;
        }
        Path fontsDir = windowsFontsDir();
        if (fontsDir == null) {
            knownUiCached = List.of();
            return knownUiCached;
        }

        Map<String, SystemFontFace> faces = new LinkedHashMap<>();
        int filesScanned = 0;
        for (String fileName : KNOWN_UI_FONT_FILES) {
            Path file = fontsDir.resolve(fileName);
            if (!Files.isRegularFile(file)) {
                continue;
            }
            filesScanned++;
            probeFile(file).ifPresent(face ->
                    faces.putIfAbsent(SystemFonts.faceName(face.family(), face.style()), face)
            );
        }
        knownUiCached = List.copyOf(faces.values());
        log.info(
                "Windows UI font scan complete: filesScanned={} facesIndexed={}",
                filesScanned,
                knownUiCached.size()
        );
        return knownUiCached;
    }

    private Path windowsFontsDir() {
        String windir = System.getenv("WINDIR");
        if (windir == null || windir.isBlank()) {
            windir = "C:\\Windows";
        }
        Path fontsDir = Path.of(windir, "Fonts");
        if (!Files.isDirectory(fontsDir)) {
            log.warn("Windows fonts directory not found: {}", fontsDir);
            return null;
        }
        return fontsDir;
    }

    private static boolean hasFontExtension(String fileName) {
        for (String ext : EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static java.util.Optional<SystemFontFace> probeFile(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, in);
            String family = awtFont.getFamily(Locale.ROOT);
            FontStyle style = FontStyle.fromAwtFlags(awtFont.isBold(), awtFont.isItalic());
            return java.util.Optional.of(new SystemFontFace(family, style, file));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
