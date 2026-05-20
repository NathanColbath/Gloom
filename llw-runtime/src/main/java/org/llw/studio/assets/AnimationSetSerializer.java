package org.llw.studio.assets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves parent animation set assets ({@code .animation.json}).
 */
public final class AnimationSetSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AnimationSetSerializer() {
    }

    public static AnimationSetDefinition load(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        AnimationSetDefinition set = new AnimationSetDefinition();
        set.version = root.path("version").asInt(1);
        set.defaultState = root.path("defaultState").asText("Idle");
        JsonNode states = root.path("states");
        if (states.isArray()) {
            for (JsonNode node : states) {
                set.states.add(new AnimationStateDefinition(
                        node.path("name").asText(""),
                        node.path("clipGuid").asText("")
                ));
            }
        }
        JsonNode clips = root.path("clips");
        if (clips.isArray()) {
            for (JsonNode node : clips) {
                set.clips.add(new AnimationClipEntry(
                        node.path("guid").asText(""),
                        node.path("name").asText(""),
                        node.path("path").asText("")
                ));
            }
        }
        return set;
    }

    public static AnimationSetDefinition loadJson(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        AnimationSetDefinition set = new AnimationSetDefinition();
        set.version = root.path("version").asInt(1);
        set.defaultState = root.path("defaultState").asText("Idle");
        JsonNode states = root.path("states");
        if (states.isArray()) {
            for (JsonNode node : states) {
                set.states.add(new AnimationStateDefinition(
                        node.path("name").asText(""),
                        node.path("clipGuid").asText("")
                ));
            }
        }
        JsonNode clips = root.path("clips");
        if (clips.isArray()) {
            for (JsonNode node : clips) {
                set.clips.add(new AnimationClipEntry(
                        node.path("guid").asText(""),
                        node.path("name").asText(""),
                        node.path("path").asText("")
                ));
            }
        }
        return set;
    }

    public static void save(Path path, AnimationSetDefinition set) throws IOException {
        Files.createDirectories(path.getParent());
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", set.version);
        root.put("defaultState", set.defaultState == null ? "" : set.defaultState);
        ArrayNode states = root.putArray("states");
        for (AnimationStateDefinition state : set.states) {
            ObjectNode node = states.addObject();
            node.put("name", state.name());
            node.put("clipGuid", state.clipGuid());
        }
        ArrayNode clips = root.putArray("clips");
        for (AnimationClipEntry clip : set.clips) {
            ObjectNode node = clips.addObject();
            node.put("guid", clip.guid());
            node.put("name", clip.name());
            node.put("path", clip.path());
        }
        Files.writeString(path, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }
}
