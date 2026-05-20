package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes the project manifest ({@code *.llwproj}) at the project root.
 */
public final class ProjectSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProjectSerializer() {
    }

    /**
     * Writes or overwrites {@code projectName.llwproj} with name and startup scene paths.
     *
     * @param projectRoot project directory
     * @param projectName display name and base name of the manifest file
     * @param startupScene project-relative path to the scene loaded on play
     * @throws IOException if the manifest cannot be written
     */
    public static void save(Path projectRoot, String projectName, String startupScene) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("name", projectName);
        root.put("startupScene", startupScene);
        Files.createDirectories(projectRoot);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(projectRoot.resolve(projectName + ".llwproj").toFile(), root);
    }

    /**
     * @param projectRoot project directory
     * @param projectName base name of the manifest file (without extension)
     * @return startup scene path from the manifest, or {@code Scenes/Main.scene.json} when missing
     * @throws IOException if the manifest exists but cannot be read
     */
    public static String startupScene(Path projectRoot, String projectName) throws IOException {
        Path file = projectRoot.resolve(projectName + ".llwproj");
        if (!Files.exists(file)) {
            return "Scenes/Main.scene.json";
        }
        return MAPPER.readTree(file.toFile()).path("startupScene").asText("Scenes/Main.scene.json");
    }
}
