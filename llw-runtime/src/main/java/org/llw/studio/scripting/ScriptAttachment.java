package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One script asset attachment on an entity, stored inside {@link ScriptComponent}.
 */
public final class ScriptAttachment implements Cloneable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Stable id within the parent {@link ScriptComponent}. */
    public int slotId;
    /** Asset GUID of the script to run. */
    public String scriptGuid = "";
    /**
     * @deprecated legacy Java FQCN; use {@link #scriptGuid} instead
     */
    @Deprecated
    public String scriptClassName = "";
    /** When {@code false}, the script lifecycle is skipped. */
    public boolean enabled = true;
    /** Serialized inspector field values keyed by field name. */
    public final Map<String, JsonNode> fields = new LinkedHashMap<>();

    /**
     * @return a deep copy of this attachment
     */
    public ScriptAttachment copy() {
        ScriptAttachment copy = new ScriptAttachment();
        copy.slotId = slotId;
        copy.scriptGuid = scriptGuid == null ? "" : scriptGuid;
        copy.scriptClassName = scriptClassName == null ? "" : scriptClassName;
        copy.enabled = enabled;
        for (Map.Entry<String, JsonNode> entry : fields.entrySet()) {
            copy.fields.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().deepCopy());
        }
        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public ScriptAttachment clone() {
        return copy();
    }

    /**
     * @return {@code true} when {@link #scriptGuid} is non-blank
     */
    public boolean hasScriptReference() {
        return scriptGuid != null && !scriptGuid.isBlank();
    }

    /**
     * @param name field name
     * @return the serialized field value, or {@code null} if absent
     */
    public JsonNode field(String name) {
        return fields.get(name);
    }

    /**
     * Sets or removes a serialized field.
     *
     * @param name  field name; blank names are ignored
     * @param value field value; {@code null} removes the field
     */
    public void setField(String name, JsonNode value) {
        if (name == null || name.isBlank()) {
            return;
        }
        if (value == null || value.isNull()) {
            fields.remove(name);
            return;
        }
        fields.put(name, value);
    }

    /**
     * @param name  field name
     * @param value numeric value
     */
    public void setNumberField(String name, double value) {
        fields.put(name, MAPPER.getNodeFactory().numberNode(value));
    }

    /**
     * @param name  field name
     * @param value boolean value
     */
    public void setBooleanField(String name, boolean value) {
        fields.put(name, MAPPER.getNodeFactory().booleanNode(value));
    }

    /**
     * @param name  field name
     * @param value text value; {@code null} is stored as empty string
     */
    public void setTextField(String name, String value) {
        fields.put(name, MAPPER.getNodeFactory().textNode(value == null ? "" : value));
    }

    /**
     * @param name field name
     * @param x    X component
     * @param y    Y component
     */
    public void setVector2Field(String name, float x, float y) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("x", x);
        node.put("y", y);
        fields.put(name, node);
    }

    /**
     * Stores an entity reference by scene object id.
     *
     * @param name          field name
     * @param sceneObjectId target scene object id
     */
    public void setEntityField(String name, int sceneObjectId) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("sceneId", sceneObjectId);
        fields.put(name, node);
    }

    /**
     * Stores a prefab asset reference.
     *
     * @param name       field name
     * @param prefabGuid prefab asset GUID; blank removes the field
     */
    public void setPrefabField(String name, String prefabGuid) {
        if (prefabGuid == null || prefabGuid.isBlank()) {
            fields.remove(name);
            return;
        }
        ObjectNode node = MAPPER.createObjectNode();
        node.put("prefab", prefabGuid);
        fields.put(name, node);
    }

    /**
     * @param name entity field name
     */
    public void clearEntityField(String name) {
        fields.remove(name);
    }

    /**
     * @param name entity field name
     * @return scene object id, or {@code -1} when missing or referencing a prefab
     */
    public int entityFieldSceneId(String name) {
        JsonNode node = fields.get(name);
        if (node == null || !node.isObject()) {
            return -1;
        }
        if (node.has("prefab")) {
            return -1;
        }
        return node.path("sceneId").asInt(-1);
    }

    /**
     * @param name entity field name
     * @return prefab GUID when the field references a prefab, otherwise empty string
     */
    public String entityFieldPrefabGuid(String name) {
        JsonNode node = fields.get(name);
        if (node == null || !node.isObject() || !node.has("prefab")) {
            return "";
        }
        return node.path("prefab").asText("");
    }
}
