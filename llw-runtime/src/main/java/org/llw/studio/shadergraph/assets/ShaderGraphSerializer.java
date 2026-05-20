package org.llw.studio.shadergraph.assets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.shadergraph.model.ShaderGraphLink;
import org.llw.studio.shadergraph.model.ShaderGraphNode;
import org.llw.studio.shadergraph.model.ShaderGraphPinRef;
import org.llw.studio.shadergraph.model.ShaderNodeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON persistence for {@link ShaderGraphDocument} ({@code .shadergraph.json}).
 */
public final class ShaderGraphSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ShaderGraphSerializer() {
    }

    public static void save(Path path, ShaderGraphDocument document) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", document.version);
        root.put("previewTextureGuid", document.previewTextureGuid == null ? "" : document.previewTextureGuid);

        ArrayNode nodes = root.putArray("nodes");
        for (ShaderGraphNode node : document.nodes) {
            ObjectNode n = nodes.addObject();
            n.put("id", node.id);
            n.put("type", node.type.name());
            n.put("x", node.x);
            n.put("y", node.y);
            ObjectNode params = n.putObject("params");
            for (Map.Entry<String, Float> entry : node.params.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
        }

        ArrayNode links = root.putArray("links");
        for (ShaderGraphLink link : document.links) {
            ObjectNode l = links.addObject();
            writePinRef(l.putObject("from"), link.from);
            writePinRef(l.putObject("to"), link.to);
        }

        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

    public static ShaderGraphDocument load(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        ShaderGraphDocument document = new ShaderGraphDocument();
        document.version = root.path("version").asInt(ShaderGraphDocument.CURRENT_VERSION);
        document.previewTextureGuid = root.path("previewTextureGuid").asText("");

        for (JsonNode nodeJson : root.path("nodes")) {
            ShaderGraphNode node = new ShaderGraphNode();
            node.id = nodeJson.path("id").asText("");
            node.type = ShaderNodeType.valueOf(nodeJson.path("type").asText("Color"));
            node.x = (float) nodeJson.path("x").asDouble(0);
            node.y = (float) nodeJson.path("y").asDouble(0);
            JsonNode params = nodeJson.path("params");
            if (params.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = params.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    node.params.put(field.getKey(), (float) field.getValue().asDouble(0));
                }
            }
            document.nodes.add(node);
        }

        for (JsonNode linkJson : root.path("links")) {
            ShaderGraphLink link = new ShaderGraphLink();
            link.from = readPinRef(linkJson.path("from"));
            link.to = readPinRef(linkJson.path("to"));
            document.links.add(link);
        }
        return document;
    }

    private static void writePinRef(ObjectNode node, ShaderGraphPinRef ref) {
        node.put("node", ref.nodeId);
        node.put("pin", ref.pinId);
    }

    private static ShaderGraphPinRef readPinRef(JsonNode node) {
        return new ShaderGraphPinRef(node.path("node").asText(""), node.path("pin").asText(""));
    }

    /**
     * @return starter graph with texture sample wired to fragment output
     */
    public static ShaderGraphDocument newDefaultGraph() {
        ShaderGraphDocument document = new ShaderGraphDocument();
        ShaderGraphNode sample = new ShaderGraphNode();
        sample.id = "tex";
        sample.type = ShaderNodeType.TextureSample;
        sample.x = 80;
        sample.y = 120;
        document.nodes.add(sample);

        ShaderGraphNode output = new ShaderGraphNode();
        output.id = "out";
        output.type = ShaderNodeType.FragmentOutput;
        output.x = 360;
        output.y = 120;
        document.nodes.add(output);

        ShaderGraphLink link = new ShaderGraphLink();
        link.from = new ShaderGraphPinRef("tex", "rgba");
        link.to = new ShaderGraphPinRef("out", "color");
        document.links.add(link);
        return document;
    }
}
