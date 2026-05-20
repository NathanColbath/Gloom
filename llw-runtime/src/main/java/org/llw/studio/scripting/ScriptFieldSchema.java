package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Metadata for a single script inspector field.
 */
public final class ScriptFieldSchema {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Field name exposed to scripts and serialization. */
    public final String name;
    /** Field type ({@code number}, {@code boolean}, {@code string}, {@code entity}, etc.). */
    public final String type;
    /** Default value from schema extraction, or {@code null}. */
    public final JsonNode defaultValue;

    /**
     * @param name         field name
     * @param type         field type string
     * @param defaultValue default JSON value, or {@code null}
     */
    public ScriptFieldSchema(String name, String type, JsonNode defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * @param node JSON object with {@code name}, {@code type}, and optional {@code default}
     * @return parsed field schema
     */
    public static ScriptFieldSchema fromJson(JsonNode node) {
        String name = node.path("name").asText("");
        String type = node.path("type").asText("string");
        JsonNode defaultValue = node.has("default") ? node.get("default") : null;
        return new ScriptFieldSchema(name, type, defaultValue);
    }

    /**
     * @return a deep copy of the default value, or JSON null when none is defined
     */
    public JsonNode copyDefault() {
        return defaultValue == null ? MAPPER.nullNode() : defaultValue.deepCopy();
    }
}
