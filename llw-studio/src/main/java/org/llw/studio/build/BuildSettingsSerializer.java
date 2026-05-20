package org.llw.studio.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes build settings under {@code .studio/metadata/build-settings.json}.
 */
public final class BuildSettingsSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BuildSettingsSerializer() {
    }

    /**
     * @param projectRoot project root directory
     * @return persisted settings or defaults when missing
     * @throws IOException if the file exists but cannot be read
     */
    public static BuildSettings load(Path projectRoot, String defaultProductName) throws IOException {
        Path file = path(projectRoot);
        BuildSettings settings = defaults(defaultProductName);
        if (!Files.isRegularFile(file)) {
            return settings;
        }
        JsonNode root = MAPPER.readTree(file.toFile());
        settings.setProductName(root.path("productName").asText(settings.productName()));
        settings.setVersion(root.path("version").asText(settings.version()));
        settings.setOutputDirectory(root.path("outputDirectory").asText(settings.outputDirectory()));
        settings.setWindowTitle(root.path("windowTitle").asText(settings.windowTitle()));
        settings.setWindowWidth(root.path("windowWidth").asInt(settings.windowWidth()));
        settings.setWindowHeight(root.path("windowHeight").asInt(settings.windowHeight()));
        settings.setVsync(root.path("vsync").asBoolean(settings.vsync()));
        settings.setIconAssetGuid(root.path("iconAssetGuid").asText(settings.iconAssetGuid()));
        return settings;
    }

    /**
     * @param projectRoot project root
     * @param settings    settings to persist
     * @throws IOException if the file cannot be written
     */
    public static void save(Path projectRoot, BuildSettings settings) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("productName", settings.productName());
        root.put("version", settings.version());
        root.put("outputDirectory", settings.outputDirectory());
        root.put("windowTitle", settings.windowTitle());
        root.put("windowWidth", settings.windowWidth());
        root.put("windowHeight", settings.windowHeight());
        root.put("vsync", settings.vsync());
        root.put("iconAssetGuid", settings.iconAssetGuid());
        Path file = path(projectRoot);
        Files.createDirectories(file.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);
    }

    private static Path path(Path projectRoot) {
        return StudioProjectLayout.metadataRoot(projectRoot).resolve("build-settings.json");
    }

    private static BuildSettings defaults(String defaultProductName) {
        BuildSettings settings = new BuildSettings();
        if (defaultProductName != null && !defaultProductName.isBlank()) {
            settings.setProductName(defaultProductName);
            settings.setWindowTitle(defaultProductName);
        }
        return settings;
    }
}
