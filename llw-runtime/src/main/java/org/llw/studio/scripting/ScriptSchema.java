package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inspector field metadata for a script asset, deserialized from {@code .studio/script-schemas}.
 */
public final class ScriptSchema {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Declared inspector fields for the script. */
    public final List<ScriptFieldSchema> fields;

    /**
     * @param fields field definitions; {@code null} yields an empty list
     */
    public ScriptSchema(List<ScriptFieldSchema> fields) {
        this.fields = fields == null ? List.of() : List.copyOf(fields);
    }

    /**
     * @return an empty schema with no fields
     */
    public static ScriptSchema empty() {
        return new ScriptSchema(List.of());
    }

    /**
     * @param root JSON root object with a {@code fields} array
     * @return parsed schema, or {@link #empty()} when invalid
     */
    public static ScriptSchema fromJson(JsonNode root) {
        if (root == null || !root.isObject()) {
            return empty();
        }
        JsonNode fieldsNode = root.path("fields");
        if (!fieldsNode.isArray()) {
            return empty();
        }
        List<ScriptFieldSchema> fields = new ArrayList<>();
        for (JsonNode fieldNode : fieldsNode) {
            fields.add(ScriptFieldSchema.fromJson(fieldNode));
        }
        return new ScriptSchema(fields);
    }

    /**
     * @param name field name
     * @return the matching field schema, or {@code null}
     */
    public ScriptFieldSchema field(String name) {
        for (ScriptFieldSchema field : fields) {
            if (field.name.equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * @return JSON representation suitable for persistence
     */
    public JsonNode toJson() {
        var root = MAPPER.createObjectNode();
        var array = root.putArray("fields");
        for (ScriptFieldSchema field : fields) {
            var node = array.addObject();
            node.put("name", field.name);
            node.put("type", field.type);
            if (field.defaultValue != null) {
                node.set("default", field.defaultValue.deepCopy());
            }
        }
        return root;
    }
}
