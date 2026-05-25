package org.llw.studio.editor.panels;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the {@code [StudioPanels]} section inside {@code imgui.ini}.
 */
final class PanelVisibilityIni {
    static final String SECTION_HEADER = "[StudioPanels]";

    private PanelVisibilityIni() {}

    /**
     * @param iniPath    ImGui layout ini path
     * @param visibility panel open state to populate
     */
    static void load(Path iniPath, PanelVisibility visibility) {
        if (iniPath == null || !Files.isRegularFile(iniPath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(iniPath, StandardCharsets.UTF_8);
            Map<String, Boolean> values = parseSection(lines);
            for (Map.Entry<String, Boolean> entry : values.entrySet()) {
                visibility.setOpen(entry.getKey(), entry.getValue());
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Replaces the {@code [StudioPanels]} block without disturbing ImGui window sections.
     *
     * @param iniPath ImGui layout ini path
     * @param states  panel id to open flag
     */
    static void merge(Path iniPath, Map<String, Boolean> states) {
        if (iniPath == null || states.isEmpty()) {
            return;
        }
        try {
            Files.createDirectories(iniPath.getParent());
            List<String> lines = Files.isRegularFile(iniPath)
                    ? new ArrayList<>(Files.readAllLines(iniPath, StandardCharsets.UTF_8))
                    : new ArrayList<>();
            // Strip and re-append only [StudioPanels] so ImGui window geometry sections stay intact.
            removeSection(lines);
            appendSection(lines, states);
            Files.write(iniPath, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    /**
     * @param iniPath ImGui layout ini path
     * @return whether a {@code [StudioPanels]} block exists
     */
    static boolean hasSection(Path iniPath) {
        if (iniPath == null || !Files.isRegularFile(iniPath)) {
            return false;
        }
        try {
            for (String line : Files.readAllLines(iniPath, StandardCharsets.UTF_8)) {
                if (SECTION_HEADER.equals(line.trim())) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static Map<String, Boolean> parseSection(List<String> lines) {
        Map<String, Boolean> values = new LinkedHashMap<>();
        boolean inSection = false;
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (inSection) {
                    break;
                }
                continue;
            }
            if (line.startsWith("[")) {
                if (inSection) {
                    break;
                }
                inSection = SECTION_HEADER.equals(line);
                continue;
            }
            if (!inSection) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();
            values.put(key, parseBoolean(value));
        }
        return values;
    }

    private static void removeSection(List<String> lines) {
        int start = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (SECTION_HEADER.equals(lines.get(i).trim())) {
                start = i;
                break;
            }
        }
        if (start < 0) {
            return;
        }
        int end = start + 1;
        while (end < lines.size()) {
            String line = lines.get(end).trim();
            if (line.isEmpty()) {
                end++;
                break;
            }
            if (line.startsWith("[")) {
                break;
            }
            end++;
        }
        lines.subList(start, end).clear();
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
    }

    private static void appendSection(List<String> lines, Map<String, Boolean> states) {
        if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
            lines.add("");
        }
        lines.add(SECTION_HEADER);
        for (Map.Entry<String, Boolean> entry : states.entrySet()) {
            lines.add(entry.getKey() + "=" + (entry.getValue() ? "1" : "0"));
        }
        lines.add("");
    }

    private static boolean parseBoolean(String value) {
        return switch (value.toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            default -> false;
        };
    }
}
