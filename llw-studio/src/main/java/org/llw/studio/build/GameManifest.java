package org.llw.studio.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Describes a built player layout written to the output directory.
 */
public final class GameManifest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public final int version;
    public final String productName;
    public final String buildVersion;
    public final String startupSceneGuid;
    public final String[] packs;
    public final WindowSettings window;

    public GameManifest(
            int version,
            String productName,
            String buildVersion,
            String startupSceneGuid,
            String[] packs,
            WindowSettings window
    ) {
        this.version = version;
        this.productName = productName;
        this.buildVersion = buildVersion;
        this.startupSceneGuid = startupSceneGuid;
        this.packs = packs.clone();
        this.window = window;
    }

    public record WindowSettings(String title, int width, int height, boolean vsync) {
    }

    /**
     * @return JSON text for {@code game.manifest.json}
     */
    public String toJson() {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("version", version);
            root.put("productName", productName);
            root.put("buildVersion", buildVersion);
            root.put("startupScene", startupSceneGuid);
            root.putPOJO("packs", packs);
            ObjectNode windowNode = root.putObject("window");
            windowNode.put("title", window.title());
            windowNode.put("width", window.width());
            windowNode.put("height", window.height());
            windowNode.put("vsync", window.vsync());
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize game manifest", ex);
        }
    }
}
