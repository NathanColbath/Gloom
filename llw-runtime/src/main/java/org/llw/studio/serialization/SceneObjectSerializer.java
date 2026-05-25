package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.ecs.components.StaticLightmapContributor;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.tilemap.TilemapCellKey;
import org.llw.studio.physics.PhysicsBodyType;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts individual {@link GameObject} instances to and from JSON object nodes within scene and prefab files.
 */
public final class SceneObjectSerializer {
    private SceneObjectSerializer() {
    }

    /**
     * @param scene scene containing the entity
     * @param entity entity to serialize
     * @param id stable scene object id
     * @param parentId parent's scene object id, or {@code -1} for roots
     * @return JSON object node for one game object
     */
    public static ObjectNode writeObject(
            Scene scene,
            EntityId entity,
            int id,
            int parentId
    ) {
        return writeObject(scene, entity, id, parentId, null);
    }

    /**
     * @param scene scene containing the entity
     * @param entity entity to serialize
     * @param id stable scene or prefab-local object id
     * @param parentId parent's object id, or {@code -1} for roots
     * @param entityRefRemap optional map remapping script entity reference scene ids when exporting prefabs
     * @return JSON object node for one game object
     */
    public static ObjectNode writeObject(
            Scene scene,
            EntityId entity,
            int id,
            int parentId,
            Map<Integer, Integer> entityRefRemap
    ) {
        ObjectNode node = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        NameComponent name = scene.world().getComponent(entity, NameComponent.class);
        node.put("id", id);
        node.put("name", name == null ? "GameObject" : name.name());
        node.put("tag", name == null ? "" : name.tag());
        node.put("parentId", parentId);
        ActiveComponent active = scene.world().getComponent(entity, ActiveComponent.class);
        node.put("active", active == null || active.selfActive);
        writeTransform(node, scene.world().getComponent(entity, Transform2DComponent.class));
        writeSprite(node, scene.world().getComponent(entity, SpriteRendererComponent.class));
        writeLight2D(node, scene.world().getComponent(entity, Light2DComponent.class));
        writeSceneLighting(node, scene.world().getComponent(entity, SceneLightingComponent.class));
        writeStaticLightmapContributor(node, scene.world().getComponent(entity, StaticLightmapContributor.class));
        writeTilemap(node, scene.world().getComponent(entity, TilemapComponent.class));
        writeAnimation2D(node, scene.world().getComponent(entity, Animation2DComponent.class));
        writeParticleEmitter(node, scene.world().getComponent(entity, ParticleEmitterComponent.class));
        writeScript(node, scene.world().getComponent(entity, ScriptComponent.class), entityRefRemap);
        writeCamera(node, scene.world().getComponent(entity, Camera2DComponent.class));
        writeAudio(node, scene.world().getComponent(entity, AudioSourceComponent.class));
        writeRigidbody(node, scene.world().getComponent(entity, Rigidbody2DComponent.class));
        writeBoxCollider(node, scene.world().getComponent(entity, BoxCollider2DComponent.class));
        writeCircleCollider(node, scene.world().getComponent(entity, CircleCollider2DComponent.class));
        writeEdgeCollider(node, scene.world().getComponent(entity, EdgeCollider2DComponent.class));
        writeUiCanvas(node, scene.world().getComponent(entity, UICanvasComponent.class));
        writeUiLabel(node, scene.world().getComponent(entity, UILabelComponent.class));
        writeUiButton(node, scene.world().getComponent(entity, UIButtonComponent.class));
        writeUiToggle(node, scene.world().getComponent(entity, UIToggleComponent.class));
        writeUiTextField(node, scene.world().getComponent(entity, UITextFieldComponent.class));
        return node;
    }

    /**
     * @param scene scene that will own the new object
     * @param objectNode JSON subtree for one game object
     * @param sceneId scene object id to assign
     * @return created game object with components attached (parent not wired yet)
     */
    public static GameObject readObject(Scene scene, JsonNode objectNode, int sceneId) {
        GameObject object = scene.createGameObject(objectNode.path("name").asText("GameObject"));
        object.setTag(objectNode.path("tag").asText(""));
        SceneObjectIds.assign(scene, object.entity(), sceneId);
        ActiveComponent active = object.getComponent(ActiveComponent.class);
        if (active != null) {
            active.selfActive = objectNode.path("active").asBoolean(true);
        }
        Transform2DComponent transform = object.transform();
        JsonNode t = objectNode.path("transform");
        transform.x = (float) t.path("x").asDouble();
        transform.y = (float) t.path("y").asDouble();
        transform.rotation = (float) t.path("rotation").asDouble();
        transform.scaleX = (float) t.path("scaleX").asDouble(1.0);
        transform.scaleY = (float) t.path("scaleY").asDouble(1.0);
        if (objectNode.has("animation2D")) {
            Animation2DComponent anim = new Animation2DComponent();
            JsonNode a = objectNode.path("animation2D");
            anim.animationGuid = a.path("animationGuid").asText("");
            anim.defaultState = a.path("defaultState").asText("Idle");
            anim.currentState = a.path("currentState").asText(anim.defaultState);
            anim.clipGuid = a.path("clipGuid").asText("");
            anim.playOnStart = a.path("playOnStart").asBoolean(true);
            anim.speed = (float) a.path("speed").asDouble(1.0);
            anim.loop = a.path("loop").asBoolean(true);
            object.addComponent(Animation2DComponent.class, anim);
        }
        if (objectNode.has("particleEmitter")) {
            ParticleEmitterComponent emitter = new ParticleEmitterComponent();
            JsonNode p = objectNode.path("particleEmitter");
            emitter.particleSystemGuid = p.path("particleSystemGuid").asText("");
            emitter.playOnAwake = p.path("playOnAwake").asBoolean(true);
            emitter.looping = p.path("looping").asBoolean(true);
            emitter.sortingOrder = p.path("sortingOrder").asInt();
            emitter.emitting = p.path("emitting").asBoolean(true);
            object.addComponent(ParticleEmitterComponent.class, emitter);
        }
        if (objectNode.has("spriteRenderer")) {
            SpriteRendererComponent sprite = new SpriteRendererComponent();
            JsonNode s = objectNode.path("spriteRenderer");
            sprite.spriteGuid = s.path("spriteGuid").asText("");
            sprite.textureGuid = s.path("textureGuid").asText("");
            sprite.materialGuid = s.path("materialGuid").asText("");
            sprite.shaderGraphGuid = s.path("shaderGraphGuid").asText("");
            sprite.sortingOrder = s.path("sortingOrder").asInt();
            sprite.r = (float) s.path("r").asDouble(1.0);
            sprite.g = (float) s.path("g").asDouble(1.0);
            sprite.b = (float) s.path("b").asDouble(1.0);
            sprite.a = (float) s.path("a").asDouble(1.0);
            object.addComponent(SpriteRendererComponent.class, sprite);
        }
        if (objectNode.has("light2D")) {
            Light2DComponent light = new Light2DComponent();
            JsonNode l = objectNode.path("light2D");
            light.type = l.path("type").asText("POINT");
            light.r = (float) l.path("r").asDouble(1.0);
            light.g = (float) l.path("g").asDouble(1.0);
            light.b = (float) l.path("b").asDouble(1.0);
            light.intensity = (float) l.path("intensity").asDouble(1.0);
            light.range = (float) l.path("range").asDouble(200.0);
            light.innerAngle = (float) l.path("innerAngle").asDouble(30.0);
            light.outerAngle = (float) l.path("outerAngle").asDouble(45.0);
            light.falloff = (float) l.path("falloff").asDouble(1.0);
            light.includeInBake = l.path("includeInBake").asBoolean(true);
            light.castShadows = l.path("castShadows").asBoolean(false);
            object.addComponent(Light2DComponent.class, light);
        }
        if (objectNode.has("sceneLighting")) {
            SceneLightingComponent lighting = new SceneLightingComponent();
            JsonNode sl = objectNode.path("sceneLighting");
            lighting.ambientR = (float) sl.path("ambientR").asDouble(0.15);
            lighting.ambientG = (float) sl.path("ambientG").asDouble(0.15);
            lighting.ambientB = (float) sl.path("ambientB").asDouble(0.18);
            lighting.ambientIntensity = (float) sl.path("ambientIntensity").asDouble(1.0);
            lighting.bakedLightmapGuid = sl.path("bakedLightmapGuid").asText("");
            lighting.lightmapEnabled = sl.path("lightmapEnabled").asBoolean(false);
            lighting.lightmapMinX = (float) sl.path("lightmapMinX").asDouble(0.0);
            lighting.lightmapMinY = (float) sl.path("lightmapMinY").asDouble(0.0);
            lighting.lightmapMaxX = (float) sl.path("lightmapMaxX").asDouble(1024.0);
            lighting.lightmapMaxY = (float) sl.path("lightmapMaxY").asDouble(1024.0);
            object.addComponent(SceneLightingComponent.class, lighting);
        }
        if (objectNode.has("staticLightmapContributor")) {
            StaticLightmapContributor contributor = new StaticLightmapContributor();
            contributor.enabled = objectNode.path("staticLightmapContributor").path("enabled").asBoolean(true);
            object.addComponent(StaticLightmapContributor.class, contributor);
        }
        if (objectNode.has("tilemap")) {
            object.addComponent(TilemapComponent.class, readTilemap(objectNode.path("tilemap")));
        }
        readScripts(object, objectNode);
        if (objectNode.has("camera2D")) {
            Camera2DComponent camera = new Camera2DComponent();
            JsonNode c = objectNode.path("camera2D");
            camera.orthographicSize = (float) c.path("orthographicSize").asDouble(360.0);
            camera.depth = (float) c.path("depth").asDouble();
            camera.mainCamera = c.path("mainCamera").asBoolean(true);
            camera.backgroundR = (float) c.path("r").asDouble(camera.backgroundR);
            camera.backgroundG = (float) c.path("g").asDouble(camera.backgroundG);
            camera.backgroundB = (float) c.path("b").asDouble(camera.backgroundB);
            camera.backgroundA = (float) c.path("a").asDouble(camera.backgroundA);
            object.addComponent(Camera2DComponent.class, camera);
        }
        if (objectNode.has("audioSource")) {
            AudioSourceComponent audio = new AudioSourceComponent();
            JsonNode a = objectNode.path("audioSource");
            audio.clipGuid = a.path("clipGuid").asText("");
            audio.volume = (float) a.path("volume").asDouble(1.0);
            audio.playOnStart = a.path("playOnStart").asBoolean(false);
            object.addComponent(AudioSourceComponent.class, audio);
        }
        readRigidbody(object, objectNode.path("rigidbody2D"));
        readBoxCollider(object, objectNode.path("boxCollider2D"));
        readCircleCollider(object, objectNode.path("circleCollider2D"));
        readEdgeCollider(object, objectNode.path("edgeCollider2D"));
        if (objectNode.has("uiCanvas")) {
            object.addComponent(UICanvasComponent.class, readUiCanvas(objectNode.path("uiCanvas")));
        }
        if (objectNode.has("uiLabel")) {
            object.addComponent(UILabelComponent.class, readUiLabel(objectNode.path("uiLabel")));
        }
        if (objectNode.has("uiButton")) {
            object.addComponent(UIButtonComponent.class, readUiButton(objectNode.path("uiButton")));
        }
        if (objectNode.has("uiToggle")) {
            object.addComponent(UIToggleComponent.class, readUiToggle(objectNode.path("uiToggle")));
        }
        if (objectNode.has("uiTextField")) {
            object.addComponent(UITextFieldComponent.class, readUiTextField(objectNode.path("uiTextField")));
        }
        return object;
    }

    /**
     * Links parent/child relationships from {@code parentId} fields after all objects are created.
     *
     * @param objectNodes serialized object nodes
     * @param objectsById map from scene object id to created {@link GameObject}
     */
    public static void wireParents(Iterable<JsonNode> objectNodes, Map<Integer, GameObject> objectsById) {
        for (JsonNode objectNode : objectNodes) {
            int parentId = objectNode.path("parentId").asInt(-1);
            if (parentId < 0) {
                continue;
            }
            GameObject child = objectsById.get(objectNode.path("id").asInt());
            GameObject parent = objectsById.get(parentId);
            if (child != null && parent != null) {
                child.setParent(parent, false);
            }
        }
    }

    /**
     * Rewrites script entity-reference fields using a prefab-to-scene id map after instantiation.
     *
     * @param prefabIdToObject objects keyed by prefab-local ids
     * @param idRemap mapping from prefab-local ids to scene object ids
     */
    public static void remapScriptEntityRefs(Map<Integer, GameObject> prefabIdToObject, Map<Integer, Integer> idRemap) {
        for (GameObject object : prefabIdToObject.values()) {
            ScriptComponent script = object.getComponent(ScriptComponent.class);
            if (script == null) {
                continue;
            }
            for (ScriptAttachment attachment : script.attachments) {
                if (attachment.fields.isEmpty()) {
                    continue;
                }
                remapAttachmentEntityRefs(attachment, idRemap);
            }
        }
    }

    private static void remapAttachmentEntityRefs(ScriptAttachment attachment, Map<Integer, Integer> idRemap) {
        for (String key : new java.util.ArrayList<>(attachment.fields.keySet())) {
            JsonNode value = attachment.fields.get(key);
            if (value == null || !value.isObject()) {
                continue;
            }
            if (!value.has("sceneId")) {
                continue;
            }
            int oldId = value.path("sceneId").asInt(-1);
            Integer newId = idRemap.get(oldId);
            if (newId != null) {
                attachment.setEntityField(key, newId);
            } else {
                attachment.setEntityField(key, -1);
            }
        }
    }

    /**
     * @param node target object JSON node
     * @param transform component to serialize, or {@code null} to skip
     */
    public static void writeTransform(ObjectNode node, Transform2DComponent transform) {
        if (transform == null) {
            return;
        }
        ObjectNode t = node.putObject("transform");
        t.put("x", transform.x);
        t.put("y", transform.y);
        t.put("rotation", transform.rotation);
        t.put("scaleX", transform.scaleX);
        t.put("scaleY", transform.scaleY);
    }

    /**
     * @param node target object JSON node
     * @param sprite component to serialize, or {@code null} to skip
     */
    public static void writeAnimation2D(ObjectNode node, Animation2DComponent anim) {
        if (anim == null) {
            return;
        }
        ObjectNode a = node.putObject("animation2D");
        a.put("animationGuid", anim.animationGuid == null ? "" : anim.animationGuid);
        a.put("defaultState", anim.defaultState == null ? "" : anim.defaultState);
        a.put("currentState", anim.currentState == null ? "" : anim.currentState);
        if (anim.clipGuid != null && !anim.clipGuid.isBlank()) {
            a.put("clipGuid", anim.clipGuid);
        }
        a.put("playOnStart", anim.playOnStart);
        a.put("speed", anim.speed);
        a.put("loop", anim.loop);
    }

    public static void writeLight2D(ObjectNode node, Light2DComponent light) {
        if (light == null) {
            return;
        }
        ObjectNode l = node.putObject("light2D");
        l.put("type", light.type == null ? "POINT" : light.type);
        l.put("r", light.r);
        l.put("g", light.g);
        l.put("b", light.b);
        l.put("intensity", light.intensity);
        l.put("range", light.range);
        l.put("innerAngle", light.innerAngle);
        l.put("outerAngle", light.outerAngle);
        l.put("falloff", light.falloff);
        l.put("includeInBake", light.includeInBake);
        l.put("castShadows", light.castShadows);
    }

    public static void writeSceneLighting(ObjectNode node, SceneLightingComponent lighting) {
        if (lighting == null) {
            return;
        }
        ObjectNode sl = node.putObject("sceneLighting");
        sl.put("ambientR", lighting.ambientR);
        sl.put("ambientG", lighting.ambientG);
        sl.put("ambientB", lighting.ambientB);
        sl.put("ambientIntensity", lighting.ambientIntensity);
        sl.put("bakedLightmapGuid", lighting.bakedLightmapGuid == null ? "" : lighting.bakedLightmapGuid);
        sl.put("lightmapEnabled", lighting.lightmapEnabled);
        sl.put("lightmapMinX", lighting.lightmapMinX);
        sl.put("lightmapMinY", lighting.lightmapMinY);
        sl.put("lightmapMaxX", lighting.lightmapMaxX);
        sl.put("lightmapMaxY", lighting.lightmapMaxY);
    }

    public static void writeStaticLightmapContributor(ObjectNode node, StaticLightmapContributor contributor) {
        if (contributor == null) {
            return;
        }
        node.putObject("staticLightmapContributor").put("enabled", contributor.enabled);
    }

    public static void writeParticleEmitter(ObjectNode node, ParticleEmitterComponent emitter) {
        if (emitter == null) {
            return;
        }
        ObjectNode p = node.putObject("particleEmitter");
        p.put("particleSystemGuid", emitter.particleSystemGuid == null ? "" : emitter.particleSystemGuid);
        p.put("playOnAwake", emitter.playOnAwake);
        p.put("looping", emitter.looping);
        p.put("sortingOrder", emitter.sortingOrder);
        p.put("emitting", emitter.emitting);
    }

    public static void writeSprite(ObjectNode node, SpriteRendererComponent sprite) {
        if (sprite == null) {
            return;
        }
        ObjectNode s = node.putObject("spriteRenderer");
        s.put("spriteGuid", sprite.spriteGuid == null ? "" : sprite.spriteGuid);
        if (sprite.textureGuid != null && !sprite.textureGuid.isBlank()) {
            s.put("textureGuid", sprite.textureGuid);
        }
        s.put("sortingOrder", sprite.sortingOrder);
        if (sprite.materialGuid != null && !sprite.materialGuid.isBlank()) {
            s.put("materialGuid", sprite.materialGuid);
        }
        if (sprite.shaderGraphGuid != null && !sprite.shaderGraphGuid.isBlank()) {
            s.put("shaderGraphGuid", sprite.shaderGraphGuid);
        }
        s.put("r", sprite.r);
        s.put("g", sprite.g);
        s.put("b", sprite.b);
        s.put("a", sprite.a);
    }

    public static void writeTilemap(ObjectNode node, TilemapComponent tilemap) {
        if (tilemap == null) {
            return;
        }
        ObjectNode t = node.putObject("tilemap");
        t.put("tilesetTextureGuid", tilemap.tilesetTextureGuid == null ? "" : tilemap.tilesetTextureGuid);
        t.put("cellWidth", tilemap.cellWidth);
        t.put("cellHeight", tilemap.cellHeight);
        var layers = t.putArray("layers");
        for (TilemapLayer layer : tilemap.layers) {
            ObjectNode layerNode = layers.addObject();
            layerNode.put("name", layer.name == null ? "Layer" : layer.name);
            layerNode.put("enabled", layer.enabled);
            layerNode.put("sortingOrder", layer.sortingOrder);
            var cells = layerNode.putArray("cells");
            for (var entry : layer.cells.entrySet()) {
                ObjectNode cellNode = cells.addObject();
                cellNode.put("x", TilemapCellKey.unpackX(entry.getKey()));
                cellNode.put("y", TilemapCellKey.unpackY(entry.getKey()));
                TilemapCell cell = entry.getValue();
                cellNode.put("spriteGuid", cell.spriteGuid == null ? "" : cell.spriteGuid);
                cellNode.put("flags", cell.flags);
            }
        }
    }

    public static TilemapComponent readTilemap(JsonNode node) {
        TilemapComponent tilemap = new TilemapComponent();
        tilemap.tilesetTextureGuid = node.path("tilesetTextureGuid").asText("");
        tilemap.cellWidth = (float) node.path("cellWidth").asDouble(32.0);
        tilemap.cellHeight = (float) node.path("cellHeight").asDouble(32.0);
        tilemap.layers.clear();
        JsonNode layers = node.path("layers");
        if (!layers.isArray() || layers.isEmpty()) {
            tilemap.ensureDefaultLayer();
            return tilemap;
        }
        for (JsonNode layerNode : layers) {
            TilemapLayer layer = new TilemapLayer();
            layer.name = layerNode.path("name").asText("Layer");
            layer.enabled = layerNode.path("enabled").asBoolean(true);
            layer.sortingOrder = layerNode.path("sortingOrder").asInt();
            JsonNode cells = layerNode.path("cells");
            if (cells.isArray()) {
                for (JsonNode cellNode : cells) {
                    TilemapCell cell = new TilemapCell();
                    cell.spriteGuid = cellNode.path("spriteGuid").asText("");
                    cell.flags = (byte) cellNode.path("flags").asInt(0);
                    int cx = cellNode.path("x").asInt();
                    int cy = cellNode.path("y").asInt();
                    layer.setCell(cx, cy, cell);
                }
            }
            tilemap.layers.add(layer);
        }
        tilemap.ensureDefaultLayer();
        return tilemap;
    }

    /**
     * @param node target object JSON node
     * @param script component to serialize, or skipped when absent or without a script reference
     */
    public static void writeScript(ObjectNode node, ScriptComponent script) {
        writeScript(node, script, null);
    }

    /**
     * @param node target object JSON node
     * @param script component to serialize
     * @param entityRefRemap optional map remapping entity-reference scene ids in serialized fields
     */
    public static void writeScript(ObjectNode node, ScriptComponent script, Map<Integer, Integer> entityRefRemap) {
        if (script == null || script.attachments.isEmpty()) {
            return;
        }
        boolean anyWritten = false;
        var scriptsArray = node.putArray("scripts");
        for (ScriptAttachment attachment : script.attachments) {
            if (!attachment.hasScriptReference() && attachment.fields.isEmpty()) {
                continue;
            }
            ObjectNode entry = scriptsArray.addObject();
            entry.put("slotId", attachment.slotId);
            writeAttachment(entry, attachment, entityRefRemap);
            anyWritten = true;
        }
        if (!anyWritten) {
            node.remove("scripts");
        }
    }

    private static void writeAttachment(
            ObjectNode entry,
            ScriptAttachment attachment,
            Map<Integer, Integer> entityRefRemap
    ) {
        entry.put("scriptGuid", attachment.scriptGuid);
        entry.put("enabled", attachment.enabled);
        if (attachment.scriptClassName != null && !attachment.scriptClassName.isBlank()) {
            entry.put("className", attachment.scriptClassName);
        }
        if (!attachment.fields.isEmpty()) {
            ObjectNode fields = entry.putObject("fields");
            attachment.fields.forEach((key, value) -> {
                if (entityRefRemap != null && value != null && value.isObject() && value.has("sceneId")) {
                    int sceneId = value.path("sceneId").asInt(-1);
                    Integer remapped = entityRefRemap.get(sceneId);
                    if (remapped != null) {
                        ObjectNode copy = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                        copy.put("sceneId", remapped);
                        fields.set(key, copy);
                        return;
                    }
                }
                fields.set(key, value == null ? null : value.deepCopy());
            });
        }
    }

    private static void readScripts(GameObject object, JsonNode objectNode) {
        if (objectNode.has("scripts") && objectNode.path("scripts").isArray()) {
            ScriptComponent container = new ScriptComponent();
            int maxSlot = 0;
            for (JsonNode scriptNode : objectNode.path("scripts")) {
                ScriptAttachment attachment = readAttachment(scriptNode);
                if (attachment.slotId <= 0) {
                    attachment.slotId = container.attachments.size() + 1;
                }
                container.attachments.add(attachment);
                maxSlot = Math.max(maxSlot, attachment.slotId);
            }
            container.syncNextSlotId(maxSlot);
            if (!container.attachments.isEmpty()) {
                object.addComponent(ScriptComponent.class, container);
            }
            return;
        }
        if (objectNode.has("script")) {
            ScriptComponent container = new ScriptComponent();
            ScriptAttachment attachment = readAttachment(objectNode.path("script"));
            if (attachment.slotId <= 0) {
                attachment.slotId = 1;
            }
            container.attachments.add(attachment);
            container.syncNextSlotId(attachment.slotId);
            object.addComponent(ScriptComponent.class, container);
        }
    }

    private static ScriptAttachment readAttachment(JsonNode scriptNode) {
        ScriptAttachment attachment = new ScriptAttachment();
        attachment.slotId = scriptNode.path("slotId").asInt(0);
        attachment.scriptGuid = scriptNode.path("scriptGuid").asText("");
        attachment.enabled = scriptNode.path("enabled").asBoolean(true);
        attachment.scriptClassName = scriptNode.path("className").asText("");
        readScriptFields(attachment, scriptNode.path("fields"));
        return attachment;
    }

    /**
     * @param attachment attachment receiving serialized fields
     * @param fieldsNode JSON object of field name to value nodes
     */
    public static void readScriptFields(ScriptAttachment attachment, JsonNode fieldsNode) {
        if (fieldsNode == null || !fieldsNode.isObject()) {
            return;
        }
        fieldsNode.fields().forEachRemaining(entry -> attachment.fields.put(entry.getKey(), entry.getValue().deepCopy()));
    }

    /**
     * @param node target object JSON node
     * @param camera component to serialize, or {@code null} to skip
     */
    public static void writeCamera(ObjectNode node, Camera2DComponent camera) {
        if (camera == null) {
            return;
        }
        ObjectNode c = node.putObject("camera2D");
        c.put("orthographicSize", camera.orthographicSize);
        c.put("depth", camera.depth);
        c.put("mainCamera", camera.mainCamera);
        c.put("r", camera.backgroundR);
        c.put("g", camera.backgroundG);
        c.put("b", camera.backgroundB);
        c.put("a", camera.backgroundA);
    }

    /**
     * @param node target object JSON node
     * @param audio component to serialize, or {@code null} to skip
     */
    public static void writeAudio(ObjectNode node, AudioSourceComponent audio) {
        if (audio == null) {
            return;
        }
        ObjectNode a = node.putObject("audioSource");
        a.put("clipGuid", audio.clipGuid);
        a.put("volume", audio.volume);
        a.put("playOnStart", audio.playOnStart);
    }

    public static void writeRigidbody(ObjectNode node, Rigidbody2DComponent rb) {
        if (rb == null) {
            return;
        }
        ObjectNode r = node.putObject("rigidbody2D");
        r.put("bodyType", rb.bodyType.name());
        r.put("mass", rb.mass);
        r.put("gravityScale", rb.gravityScale);
        r.put("linearDrag", rb.linearDrag);
        r.put("angularDrag", rb.angularDrag);
        r.put("freezeRotation", rb.freezeRotation);
        r.put("simulated", rb.simulated);
    }

    public static void writeBoxCollider(ObjectNode node, BoxCollider2DComponent collider) {
        if (collider == null) {
            return;
        }
        ObjectNode c = node.putObject("boxCollider2D");
        c.put("sizeX", collider.sizeX);
        c.put("sizeY", collider.sizeY);
        c.put("offsetX", collider.offsetX);
        c.put("offsetY", collider.offsetY);
        c.put("isTrigger", collider.isTrigger);
        c.put("layer", collider.layer);
        c.put("layerMask", collider.layerMask);
    }

    public static void writeCircleCollider(ObjectNode node, CircleCollider2DComponent collider) {
        if (collider == null) {
            return;
        }
        ObjectNode c = node.putObject("circleCollider2D");
        c.put("radius", collider.radius);
        c.put("offsetX", collider.offsetX);
        c.put("offsetY", collider.offsetY);
        c.put("isTrigger", collider.isTrigger);
        c.put("layer", collider.layer);
        c.put("layerMask", collider.layerMask);
    }

    public static void writeEdgeCollider(ObjectNode node, EdgeCollider2DComponent collider) {
        if (collider == null) {
            return;
        }
        ObjectNode c = node.putObject("edgeCollider2D");
        c.put("isTrigger", collider.isTrigger);
        c.put("layer", collider.layer);
        c.put("layerMask", collider.layerMask);
        var points = c.putArray("points");
        if (collider.points != null) {
            for (float value : collider.points) {
                points.add(value);
            }
        }
    }

    private static void readRigidbody(GameObject object, JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        Rigidbody2DComponent rb = new Rigidbody2DComponent();
        rb.bodyType = parseBodyType(node.path("bodyType").asText("DYNAMIC"));
        rb.mass = (float) node.path("mass").asDouble(1.0);
        rb.gravityScale = (float) node.path("gravityScale").asDouble(1.0);
        rb.linearDrag = (float) node.path("linearDrag").asDouble(0.0);
        rb.angularDrag = (float) node.path("angularDrag").asDouble(0.0);
        rb.freezeRotation = node.path("freezeRotation").asBoolean(false);
        rb.simulated = node.path("simulated").asBoolean(true);
        object.addComponent(Rigidbody2DComponent.class, rb);
    }

    private static void readBoxCollider(GameObject object, JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        BoxCollider2DComponent collider = new BoxCollider2DComponent();
        collider.sizeX = (float) node.path("sizeX").asDouble(1.0);
        collider.sizeY = (float) node.path("sizeY").asDouble(1.0);
        collider.offsetX = (float) node.path("offsetX").asDouble(0.0);
        collider.offsetY = (float) node.path("offsetY").asDouble(0.0);
        collider.isTrigger = node.path("isTrigger").asBoolean(false);
        collider.layer = node.path("layer").asInt(0);
        collider.layerMask = node.path("layerMask").asInt(0xFFFF_FFFF);
        object.addComponent(BoxCollider2DComponent.class, collider);
    }

    private static void readCircleCollider(GameObject object, JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        CircleCollider2DComponent collider = new CircleCollider2DComponent();
        collider.radius = (float) node.path("radius").asDouble(0.5);
        collider.offsetX = (float) node.path("offsetX").asDouble(0.0);
        collider.offsetY = (float) node.path("offsetY").asDouble(0.0);
        collider.isTrigger = node.path("isTrigger").asBoolean(false);
        collider.layer = node.path("layer").asInt(0);
        collider.layerMask = node.path("layerMask").asInt(0xFFFF_FFFF);
        object.addComponent(CircleCollider2DComponent.class, collider);
    }

    private static void readEdgeCollider(GameObject object, JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        EdgeCollider2DComponent collider = new EdgeCollider2DComponent();
        collider.isTrigger = node.path("isTrigger").asBoolean(false);
        collider.layer = node.path("layer").asInt(0);
        collider.layerMask = node.path("layerMask").asInt(0xFFFF_FFFF);
        JsonNode pointsNode = node.path("points");
        if (pointsNode.isArray() && pointsNode.size() >= 4) {
            float[] points = new float[pointsNode.size()];
            for (int i = 0; i < pointsNode.size(); i++) {
                points[i] = (float) pointsNode.get(i).asDouble();
            }
            collider.points = points;
        }
        object.addComponent(EdgeCollider2DComponent.class, collider);
    }

    public static void writeUiCanvas(ObjectNode node, UICanvasComponent canvas) {
        if (canvas == null) {
            return;
        }
        ObjectNode c = node.putObject("uiCanvas");
        c.put("sortingOrder", canvas.sortingOrder);
        c.put("enabled", canvas.enabled);
        if (canvas.renderMode != null) {
            c.put("renderMode", canvas.renderMode.id());
        }
        c.put("referenceWidth", canvas.referenceWidth);
        c.put("referenceHeight", canvas.referenceHeight);
    }

    public static UICanvasComponent readUiCanvas(JsonNode node) {
        UICanvasComponent canvas = new UICanvasComponent();
        canvas.sortingOrder = node.path("sortingOrder").asInt();
        canvas.enabled = node.path("enabled").asBoolean(true);
        canvas.renderMode = org.llw.studio.ui.UiCanvasRenderMode.fromId(node.path("renderMode").asText(null));
        if (node.has("referenceWidth")) {
            canvas.referenceWidth = Math.max(1, node.path("referenceWidth").asInt(1920));
        }
        if (node.has("referenceHeight")) {
            canvas.referenceHeight = Math.max(1, node.path("referenceHeight").asInt(1080));
        }
        return canvas;
    }

    public static void writeUiLabel(ObjectNode node, UILabelComponent label) {
        if (label == null) {
            return;
        }
        ObjectNode l = node.putObject("uiLabel");
        l.put("text", label.text == null ? "" : label.text);
        l.put("width", label.width);
        l.put("height", label.height);
        l.put("fontSize", label.fontSize);
        l.put("r", label.r);
        l.put("g", label.g);
        l.put("b", label.b);
        l.put("a", label.a);
        l.put("alignment", label.alignment);
    }

    public static UILabelComponent readUiLabel(JsonNode node) {
        UILabelComponent label = new UILabelComponent();
        label.text = node.path("text").asText("Label");
        label.width = (float) node.path("width").asDouble(120.0);
        label.height = (float) node.path("height").asDouble(32.0);
        label.fontSize = node.path("fontSize").asInt(16);
        label.r = (float) node.path("r").asDouble(1.0);
        label.g = (float) node.path("g").asDouble(1.0);
        label.b = (float) node.path("b").asDouble(1.0);
        label.a = (float) node.path("a").asDouble(1.0);
        label.alignment = node.path("alignment").asInt(0);
        return label;
    }

    public static void writeUiButton(ObjectNode node, UIButtonComponent button) {
        if (button == null) {
            return;
        }
        ObjectNode b = node.putObject("uiButton");
        b.put("label", button.label == null ? "" : button.label);
        b.put("width", button.width);
        b.put("height", button.height);
        b.put("fontSize", button.fontSize);
        b.put("r", button.r);
        b.put("g", button.g);
        b.put("b", button.b);
        b.put("a", button.a);
        b.put("hoverR", button.hoverR);
        b.put("hoverG", button.hoverG);
        b.put("hoverB", button.hoverB);
        b.put("hoverA", button.hoverA);
        b.put("pressedR", button.pressedR);
        b.put("pressedG", button.pressedG);
        b.put("pressedB", button.pressedB);
        b.put("pressedA", button.pressedA);
        b.put("textR", button.textR);
        b.put("textG", button.textG);
        b.put("textB", button.textB);
        b.put("textA", button.textA);
        b.put("interactable", button.interactable);
    }

    public static UIButtonComponent readUiButton(JsonNode node) {
        UIButtonComponent button = new UIButtonComponent();
        button.label = node.path("label").asText("Button");
        button.width = (float) node.path("width").asDouble(120.0);
        button.height = (float) node.path("height").asDouble(36.0);
        button.fontSize = node.path("fontSize").asInt(16);
        button.r = (float) node.path("r").asDouble(0.25);
        button.g = (float) node.path("g").asDouble(0.45);
        button.b = (float) node.path("b").asDouble(0.85);
        button.a = (float) node.path("a").asDouble(1.0);
        button.hoverR = (float) node.path("hoverR").asDouble(button.hoverR);
        button.hoverG = (float) node.path("hoverG").asDouble(button.hoverG);
        button.hoverB = (float) node.path("hoverB").asDouble(button.hoverB);
        button.hoverA = (float) node.path("hoverA").asDouble(button.hoverA);
        button.pressedR = (float) node.path("pressedR").asDouble(button.pressedR);
        button.pressedG = (float) node.path("pressedG").asDouble(button.pressedG);
        button.pressedB = (float) node.path("pressedB").asDouble(button.pressedB);
        button.pressedA = (float) node.path("pressedA").asDouble(button.pressedA);
        button.textR = (float) node.path("textR").asDouble(1.0);
        button.textG = (float) node.path("textG").asDouble(1.0);
        button.textB = (float) node.path("textB").asDouble(1.0);
        button.textA = (float) node.path("textA").asDouble(1.0);
        button.interactable = node.path("interactable").asBoolean(true);
        return button;
    }

    public static void writeUiToggle(ObjectNode node, UIToggleComponent toggle) {
        if (toggle == null) {
            return;
        }
        ObjectNode t = node.putObject("uiToggle");
        t.put("label", toggle.label == null ? "" : toggle.label);
        t.put("isOn", toggle.isOn);
        t.put("width", toggle.width);
        t.put("height", toggle.height);
        t.put("boxSize", toggle.boxSize);
        t.put("fontSize", toggle.fontSize);
        t.put("r", toggle.r);
        t.put("g", toggle.g);
        t.put("b", toggle.b);
        t.put("a", toggle.a);
        t.put("onR", toggle.onR);
        t.put("onG", toggle.onG);
        t.put("onB", toggle.onB);
        t.put("onA", toggle.onA);
        t.put("textR", toggle.textR);
        t.put("textG", toggle.textG);
        t.put("textB", toggle.textB);
        t.put("textA", toggle.textA);
        t.put("interactable", toggle.interactable);
    }

    public static UIToggleComponent readUiToggle(JsonNode node) {
        UIToggleComponent toggle = new UIToggleComponent();
        toggle.label = node.path("label").asText("Toggle");
        toggle.isOn = node.path("isOn").asBoolean(false);
        toggle.width = (float) node.path("width").asDouble(160.0);
        toggle.height = (float) node.path("height").asDouble(28.0);
        toggle.boxSize = (float) node.path("boxSize").asDouble(18.0);
        toggle.fontSize = node.path("fontSize").asInt(16);
        toggle.r = (float) node.path("r").asDouble(0.2);
        toggle.g = (float) node.path("g").asDouble(0.2);
        toggle.b = (float) node.path("b").asDouble(0.2);
        toggle.a = (float) node.path("a").asDouble(1.0);
        toggle.onR = (float) node.path("onR").asDouble(toggle.onR);
        toggle.onG = (float) node.path("onG").asDouble(toggle.onG);
        toggle.onB = (float) node.path("onB").asDouble(toggle.onB);
        toggle.onA = (float) node.path("onA").asDouble(toggle.onA);
        toggle.textR = (float) node.path("textR").asDouble(1.0);
        toggle.textG = (float) node.path("textG").asDouble(1.0);
        toggle.textB = (float) node.path("textB").asDouble(1.0);
        toggle.textA = (float) node.path("textA").asDouble(1.0);
        toggle.interactable = node.path("interactable").asBoolean(true);
        return toggle;
    }

    public static void writeUiTextField(ObjectNode node, UITextFieldComponent field) {
        if (field == null) {
            return;
        }
        ObjectNode f = node.putObject("uiTextField");
        f.put("value", field.value == null ? "" : field.value);
        f.put("placeholder", field.placeholder == null ? "" : field.placeholder);
        f.put("width", field.width);
        f.put("height", field.height);
        f.put("fontSize", field.fontSize);
        f.put("maxLength", field.maxLength);
        f.put("r", field.r);
        f.put("g", field.g);
        f.put("b", field.b);
        f.put("a", field.a);
        f.put("borderR", field.borderR);
        f.put("borderG", field.borderG);
        f.put("borderB", field.borderB);
        f.put("borderA", field.borderA);
        f.put("textR", field.textR);
        f.put("textG", field.textG);
        f.put("textB", field.textB);
        f.put("textA", field.textA);
        f.put("placeholderR", field.placeholderR);
        f.put("placeholderG", field.placeholderG);
        f.put("placeholderB", field.placeholderB);
        f.put("placeholderA", field.placeholderA);
        f.put("interactable", field.interactable);
    }

    public static UITextFieldComponent readUiTextField(JsonNode node) {
        UITextFieldComponent field = new UITextFieldComponent();
        field.value = node.path("value").asText("");
        field.placeholder = node.path("placeholder").asText("Enter text...");
        field.width = (float) node.path("width").asDouble(200.0);
        field.height = (float) node.path("height").asDouble(32.0);
        field.fontSize = node.path("fontSize").asInt(16);
        field.maxLength = node.path("maxLength").asInt(128);
        field.r = (float) node.path("r").asDouble(0.12);
        field.g = (float) node.path("g").asDouble(0.12);
        field.b = (float) node.path("b").asDouble(0.12);
        field.a = (float) node.path("a").asDouble(1.0);
        field.borderR = (float) node.path("borderR").asDouble(field.borderR);
        field.borderG = (float) node.path("borderG").asDouble(field.borderG);
        field.borderB = (float) node.path("borderB").asDouble(field.borderB);
        field.borderA = (float) node.path("borderA").asDouble(field.borderA);
        field.textR = (float) node.path("textR").asDouble(1.0);
        field.textG = (float) node.path("textG").asDouble(1.0);
        field.textB = (float) node.path("textB").asDouble(1.0);
        field.textA = (float) node.path("textA").asDouble(1.0);
        field.placeholderR = (float) node.path("placeholderR").asDouble(field.placeholderR);
        field.placeholderG = (float) node.path("placeholderG").asDouble(field.placeholderG);
        field.placeholderB = (float) node.path("placeholderB").asDouble(field.placeholderB);
        field.placeholderA = (float) node.path("placeholderA").asDouble(field.placeholderA);
        field.interactable = node.path("interactable").asBoolean(true);
        return field;
    }

    private static PhysicsBodyType parseBodyType(String value) {
        try {
            return PhysicsBodyType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return PhysicsBodyType.DYNAMIC;
        }
    }
}
