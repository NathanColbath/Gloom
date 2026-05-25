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
    /** Whether the script class declares {@code onDrawGizmos}. */
    public final boolean hasDrawGizmos;
    /** Whether the script class declares {@code onDrawGizmosSelected}. */
    public final boolean hasDrawGizmosSelected;

    /**
     * @param fields field definitions; {@code null} yields an empty list
     */
    public ScriptSchema(List<ScriptFieldSchema> fields) {
        this(fields, false, false);
    }

    /**
     * @param fields                 field definitions; {@code null} yields an empty list
     * @param hasDrawGizmos          true when {@code onDrawGizmos} is present
     * @param hasDrawGizmosSelected  true when {@code onDrawGizmosSelected} is present
     */
    public ScriptSchema(List<ScriptFieldSchema> fields, boolean hasDrawGizmos, boolean hasDrawGizmosSelected) {
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        this.hasDrawGizmos = hasDrawGizmos;
        this.hasDrawGizmosSelected = hasDrawGizmosSelected;
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
        boolean hasDrawGizmos = root.path("hasDrawGizmos").asBoolean(false);
        boolean hasDrawGizmosSelected = root.path("hasDrawGizmosSelected").asBoolean(false);
        return new ScriptSchema(fields, hasDrawGizmos, hasDrawGizmosSelected);
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
        root.put("hasDrawGizmos", hasDrawGizmos);
        root.put("hasDrawGizmosSelected", hasDrawGizmosSelected);
        return root;
    }
}
