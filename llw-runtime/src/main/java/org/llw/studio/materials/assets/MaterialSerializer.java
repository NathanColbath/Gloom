package org.llw.studio.materials.assets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.materials.model.MaterialDocument;
import org.llw.studio.materials.model.MaterialProperty;
import org.llw.studio.materials.model.MaterialPropertyType;
import org.llw.studio.materials.model.MaterialShaderSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON persistence for {@link MaterialDocument}.
 */
public final class MaterialSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MaterialSerializer() {
    }

    public static void save(Path path, MaterialDocument document) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", document.version);
        root.put("shaderSource", document.shaderSource == null ? MaterialShaderSource.BUILTIN_LIT.name() : document.shaderSource);
        root.put("shaderGraphGuid", document.shaderGraphGuid == null ? "" : document.shaderGraphGuid);
        root.put("normalMapTextureGuid", document.normalMapTextureGuid == null ? "" : document.normalMapTextureGuid);
        ArrayNode props = root.putArray("properties");
        for (MaterialProperty property : document.properties) {
            ObjectNode p = props.addObject();
            p.put("name", property.name);
            p.put("type", property.type);
            p.put("floatValue", property.floatValue);
            p.put("r", property.r);
            p.put("g", property.g);
            p.put("b", property.b);
            p.put("a", property.a);
            p.put("textureGuid", property.textureGuid == null ? "" : property.textureGuid);
        }
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

    public static MaterialDocument load(byte[] jsonBytes) throws IOException {
        JsonNode root = MAPPER.readTree(jsonBytes);
        return parseRoot(root);
    }

    public static MaterialDocument load(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        return parseRoot(root);
    }

    private static MaterialDocument parseRoot(JsonNode root) {
        MaterialDocument document = new MaterialDocument();
        document.version = root.path("version").asInt(MaterialDocument.CURRENT_VERSION);
        document.shaderSource = root.path("shaderSource").asText(MaterialShaderSource.BUILTIN_LIT.name());
        document.shaderGraphGuid = root.path("shaderGraphGuid").asText("");
        document.normalMapTextureGuid = root.path("normalMapTextureGuid").asText("");
        for (JsonNode node : root.path("properties")) {
            MaterialProperty property = new MaterialProperty();
            property.name = node.path("name").asText("");
            property.type = node.path("type").asText(MaterialPropertyType.FLOAT.name());
            property.floatValue = (float) node.path("floatValue").asDouble(0.0);
            property.r = (float) node.path("r").asDouble(1.0);
            property.g = (float) node.path("g").asDouble(1.0);
            property.b = (float) node.path("b").asDouble(1.0);
            property.a = (float) node.path("a").asDouble(1.0);
            property.textureGuid = node.path("textureGuid").asText("");
            document.properties.add(property);
        }
        return document;
    }

    public static MaterialDocument newDefaultLit() {
        MaterialDocument document = new MaterialDocument();
        document.shaderSource = MaterialShaderSource.BUILTIN_LIT.name();
        return document;
    }

    public static MaterialDocument newDefaultUnlit() {
        MaterialDocument document = new MaterialDocument();
        document.shaderSource = MaterialShaderSource.BUILTIN_UNLIT.name();
        return document;
    }
}
