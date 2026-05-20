package org.llw.studio.scripting.js;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.graalvm.polyglot.Value;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptFieldSchema;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

/**
 * Applies serialized {@link ScriptAttachment} field values onto a Graal script instance at spawn time.
 */
public final class ScriptFieldApplicator {
    private ScriptFieldApplicator() {
    }

    /**
     * @param instance  Graal script instance
     * @param hostApi   play-mode host API for entity wrapping
     * @param world     play-mode ECS world
     * @param attachment serialized script attachment
     * @param schema    extracted field schema
     */
    public static void applySerializedFields(
            Value instance,
            ScriptHostApi hostApi,
            World world,
            ScriptAttachment attachment,
            ScriptSchema schema
    ) {
        for (ScriptFieldSchema field : schema.fields) {
            JsonNode value = attachment.fields.getOrDefault(field.name, field.copyDefault());
            applyField(instance, hostApi, world, field, value);
        }
        // Legacy entity references saved before schema extraction; never apply orphan keys to
        // private / removed inspector fields.
        for (var entry : attachment.fields.entrySet()) {
            if (schema.field(entry.getKey()) != null) {
                continue;
            }
            JsonNode value = entry.getValue();
            if (value != null
                    && value.isObject()
                    && (value.has("sceneId") || value.has("prefab") || value.has("targetId"))) {
                applyUntypedField(instance, hostApi, world, entry.getKey(), value);
            }
        }
    }

    /**
     * @param instance Graal script instance
     * @param hostApi  play-mode host API
     * @param world    play-mode ECS world
     * @param field    field schema
     * @param value    serialized JSON value
     */
    public static void applyField(
            Value instance,
            ScriptHostApi hostApi,
            World world,
            ScriptFieldSchema field,
            JsonNode value
    ) {
        if (value == null || value instanceof NullNode || value.isNull()) {
            if ("entity".equals(field.type)) {
                instance.putMember(field.name, (Object) null);
            }
            return;
        }
        switch (field.type) {
            case "number" -> instance.putMember(field.name, value.asDouble());
            case "boolean" -> instance.putMember(field.name, value.asBoolean());
            case "string" -> instance.putMember(field.name, value.asText(""));
            case "vector2" -> {
                double x = value.path("x").asDouble(0);
                double y = value.path("y").asDouble(0);
                instance.putMember(field.name, hostApi.createVec2(x, y));
            }
            case "entity" -> {
                String prefabGuid = entityFieldPrefabGuid(value);
                if (prefabGuid != null && !prefabGuid.isBlank()) {
                    instance.putMember(field.name, hostApi.wrapPrefabTemplate(prefabGuid));
                    return;
                }
                int sceneId = entitySceneId(value);
                if (sceneId < 0) {
                    instance.putMember(field.name, (Object) null);
                } else {
                    EntityId target = SceneObjectIds.findBySceneId(world, sceneId);
                    if (target.isNone()) {
                        instance.putMember(field.name, (Object) null);
                    } else {
                        instance.putMember(
                                field.name,
                                hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), target))
                        );
                    }
                }
            }
            default -> instance.putMember(field.name, value.asText(""));
        }
    }

    private static void applyUntypedField(
            Value instance,
            ScriptHostApi hostApi,
            World world,
            String name,
            JsonNode value
    ) {
        if (value == null || value.isNull()) {
            return;
        }
        if (value.isNumber()) {
            instance.putMember(name, value.asDouble());
        } else if (value.isBoolean()) {
            instance.putMember(name, value.asBoolean());
        } else if (value.isTextual()) {
            instance.putMember(name, value.asText(""));
        } else if (value.isObject() && value.has("prefab")) {
            String prefabGuid = value.path("prefab").asText("");
            if (prefabGuid.isBlank()) {
                instance.putMember(name, (Object) null);
            } else {
                instance.putMember(name, hostApi.wrapPrefabTemplate(prefabGuid));
            }
        } else if (value.isObject() && value.has("sceneId")) {
            int sceneId = value.path("sceneId").asInt(-1);
            EntityId target = SceneObjectIds.findBySceneId(world, sceneId);
            if (target.isNone()) {
                instance.putMember(name, (Object) null);
            } else {
                instance.putMember(
                        name,
                        hostApi.wrapEntity(hostApi.createEntityBinding(hostApi.scriptContext(), target))
                );
            }
        }
    }

    /**
     * @param value serialized entity field JSON
     * @return prefab GUID when present, otherwise empty string
     */
    public static String entityFieldPrefabGuid(JsonNode value) {
        if (value == null || value.isNull() || !value.isObject()) {
            return "";
        }
        return value.path("prefab").asText("");
    }

    /**
     * @param value serialized entity field JSON
     * @return scene object id, or {@code -1} when not an entity reference
     */
    public static int entitySceneId(JsonNode value) {
        if (value == null || value.isNull()) {
            return -1;
        }
        if (value.isNumber()) {
            return value.asInt(-1);
        }
        if (value.isObject()) {
            if (value.has("prefab")) {
                return -1;
            }
            return value.path("sceneId").asInt(value.path("targetId").asInt(-1));
        }
        return -1;
    }
}
