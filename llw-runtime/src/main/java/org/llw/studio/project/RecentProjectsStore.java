package org.llw.studio.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists recently opened projects in {@code ~/.llw-studio/recent.json}.
 */
public final class RecentProjectsStore {
    private static final int MAX_ENTRIES = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path storePath;
    private final List<RecentProjectEntry> entries = new ArrayList<>();

    /** Loads existing entries from the user home store file, if present. */
    public RecentProjectsStore() {
        String home = System.getProperty("user.home", ".");
        storePath = Path.of(home, ".llw-studio", "recent.json");
        load();
    }

    /** @return most-recent-first list of opened projects (at most {@value #MAX_ENTRIES}) */
    public List<RecentProjectEntry> entries() {
        return List.copyOf(entries);
    }

    /**
     * Promotes a project to the top of the recent list and persists the store.
     *
     * @param descriptor project that was opened or created
     */
    public void add(ProjectDescriptor descriptor) {
        Path root = descriptor.root().toAbsolutePath().normalize();
        entries.removeIf(entry -> entry.path().equals(root));
        entries.add(0, new RecentProjectEntry(descriptor.name(), root, Instant.now()));
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        save();
    }

    private void load() {
        entries.clear();
        if (!Files.exists(storePath)) {
            return;
        }
        try {
            var root = MAPPER.readTree(storePath.toFile());
            if (!root.isArray()) {
                return;
            }
            for (var node : root) {
                String name = node.path("name").asText("");
                String path = node.path("path").asText("");
                if (path.isBlank()) {
                    continue;
                }
                entries.add(new RecentProjectEntry(name, Path.of(path), Instant.parse(node.path("openedAt").asText(Instant.EPOCH.toString()))));
            }
        } catch (Exception ignored) {
            entries.clear();
        }
    }

    private void save() {
        try {
            Files.createDirectories(storePath.getParent());
            ArrayNode array = MAPPER.createArrayNode();
            for (RecentProjectEntry entry : entries) {
                ObjectNode node = array.addObject();
                node.put("name", entry.name());
                node.put("path", entry.path().toString());
                node.put("openedAt", entry.openedAt().toString());
            }
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), array);
        } catch (IOException ignored) {
        }
    }

    /**
     * One row in the recent-projects list.
     *
     * @param name project display name
     * @param path absolute project root path
     * @param openedAt timestamp when the project was last opened
     */
    public record RecentProjectEntry(String name, Path path, Instant openedAt) {
    }
}
