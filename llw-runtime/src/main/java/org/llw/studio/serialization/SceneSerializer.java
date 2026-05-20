package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializes {@link Scene} graphs to {@code .scene.json} and bootstraps default scenes for new projects.
 */
public final class SceneSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int VERSION = 2;

    private SceneSerializer() {
    }

    /**
     * Writes the scene hierarchy and components to {@code path}.
     *
     * @param scene scene to serialize
     * @param path destination {@code .scene.json} file
     * @throws IOException if the file cannot be written
     */
    public static void save(Scene scene, Path path) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", VERSION);
        root.put("name", scene.name());
        ArrayNode objects = root.putArray("objects");
        Map<EntityId, Integer> idMap = new HashMap<>();
        var names = scene.world().store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId entity = names.entityAt(i);
            NameComponent name = names.componentAt(i);
            if ("Scene Root".equals(name.name())) {
                continue;
            }
            int sceneId = SceneObjectIds.get(scene.world(), entity);
            if (sceneId < 0) {
                sceneId = SceneObjectIds.allocate(scene);
                SceneObjectIds.assign(scene, entity, sceneId);
            }
            idMap.put(entity, sceneId);
        }
        for (int i = 0; i < names.size(); i++) {
            EntityId entity = names.entityAt(i);
            NameComponent name = names.componentAt(i);
            if ("Scene Root".equals(name.name())) {
                continue;
            }
            HierarchyComponent hierarchy = scene.world().getComponent(entity, HierarchyComponent.class);
            int parentId = -1;
            if (hierarchy != null && hierarchy.parentIndex >= 0) {
                EntityId parentEntity = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
                parentId = idMap.getOrDefault(parentEntity, -1);
            }
            objects.add(SceneObjectSerializer.writeObject(scene, entity, idMap.get(entity), parentId));
        }
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

    /**
     * @param path path to a {@code .scene.json} file
     * @return deserialized scene with hierarchy wired and a main camera ensured
     * @throws IOException if the file cannot be read
     */
    public static Scene load(Path path) throws IOException {
        return loadJson(Files.readString(path));
    }

    /**
     * @param json scene JSON text
     * @return deserialized scene with hierarchy wired and a main camera ensured
     * @throws IOException if the JSON cannot be parsed
     */
    public static Scene loadJson(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        Scene scene = new Scene();
        scene.setName(root.path("name").asText("Untitled"));
        Map<Integer, GameObject> objectsById = new HashMap<>();
        for (JsonNode objectNode : root.path("objects")) {
            int sceneId = objectNode.path("id").asInt();
            GameObject object = SceneObjectSerializer.readObject(scene, objectNode, sceneId);
            objectsById.put(sceneId, object);
        }
        SceneObjectSerializer.wireParents(root.path("objects"), objectsById);
        ensureMainCamera(scene);
        return scene;
    }

    /**
     * Guarantees exactly one {@link Camera2DComponent} is marked as the main camera.
     *
     * @param scene scene to inspect or modify
     */
    public static void ensureMainCamera(Scene scene) {
        var cameras = scene.world().store(Camera2DComponent.class);
        for (int i = 0; i < cameras.size(); i++) {
            if (cameras.componentAt(i).mainCamera) {
                return;
            }
        }
        if (cameras.size() > 0) {
            cameras.componentAt(0).mainCamera = true;
            return;
        }
        GameObject mainCamera = scene.createGameObject("Main Camera");
        Camera2DComponent cameraComponent = new Camera2DComponent();
        cameraComponent.mainCamera = true;
        mainCamera.addComponent(Camera2DComponent.class, cameraComponent);
    }
}
