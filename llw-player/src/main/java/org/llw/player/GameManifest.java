package org.llw.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parsed {@code game.manifest.json} for a published build.
 */
public final class GameManifest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String productName;
    private final String buildVersion;
    private final String startupSceneGuid;
    private final WindowSettings window;

    public GameManifest(String productName, String buildVersion, String startupSceneGuid, WindowSettings window) {
        this.productName = productName;
        this.buildVersion = buildVersion;
        this.startupSceneGuid = startupSceneGuid;
        this.window = window;
    }

    public static GameManifest read(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        JsonNode windowNode = root.path("window");
        WindowSettings window = new WindowSettings(
                windowNode.path("title").asText(root.path("productName").asText("Game")),
                windowNode.path("width").asInt(1280),
                windowNode.path("height").asInt(720),
                windowNode.path("vsync").asBoolean(true)
        );
        return new GameManifest(
                root.path("productName").asText("Game"),
                root.path("buildVersion").asText("1.0.0"),
                root.path("startupScene").asText(""),
                window
        );
    }

    public String productName() {
        return productName;
    }

    public String buildVersion() {
        return buildVersion;
    }

    public String startupSceneGuid() {
        return startupSceneGuid;
    }

    public WindowSettings window() {
        return window;
    }

    public record WindowSettings(String title, int width, int height, boolean vsync) {
    }
}
