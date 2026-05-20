package org.llw.studio.project;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Locates and parses project metadata from a folder on disk.
 */
public final class ProjectDiscovery {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProjectDiscovery() {
    }

    /**
     * Builds a descriptor for an existing or minimal project folder.
     *
     * @param projectRoot directory that should contain {@code Assets} and optionally {@code *.llwproj}
     * @return parsed or default project descriptor
     * @throws IOException if {@code projectRoot} is not a directory
     */
    public static ProjectDescriptor discover(Path projectRoot) throws IOException {
        Path normalized = projectRoot.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalized)) {
            throw new IOException("Project folder does not exist: " + normalized);
        }
        Files.createDirectories(normalized.resolve("Assets"));
        Path llwproj = findProjectFile(normalized);
        if (llwproj == null) {
            String name = normalized.getFileName().toString();
            return new ProjectDescriptor(normalized, name, "Scenes/Main.scene.json");
        }
        var tree = MAPPER.readTree(llwproj.toFile());
        String name = tree.path("name").asText(normalized.getFileName().toString());
        String startupScene = tree.path("startupScene").asText("Scenes/Main.scene.json");
        return new ProjectDescriptor(normalized, name, startupScene);
    }

    private static Path findProjectFile(Path projectRoot) throws IOException {
        List<Path> matches = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(projectRoot, "*.llwproj")) {
            for (Path path : stream) {
                matches.add(path);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        matches.sort(Path::compareTo);
        return matches.get(0);
    }
}
