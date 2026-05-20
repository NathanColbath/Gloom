package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes prefab documents ({@code .prefab.json}) from scene object subtrees.
 */
public final class PrefabSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int VERSION = 1;

    private PrefabSerializer() {
    }

    /**
     * Parsed prefab JSON ready for instantiation.
     *
     * @param objects array of serialized game objects
     * @param exportRootId prefab root object id within {@code objects}
     */
    public record PrefabData(ArrayNode objects, int exportRootId) {
        /** @return iterable view of object nodes in the prefab */
        public Iterable<JsonNode> objectNodes() {
            return objects;
        }
    }

    /**
     * Exports the hierarchy rooted at {@code rootEntity} to a prefab file.
     *
     * @param scene scene containing the subtree
     * @param rootEntity entity at the prefab root
     * @param path destination {@code .prefab.json} path
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if {@code rootEntity} is not found in the scene
     */
    public static void saveSubtree(Scene scene, EntityId rootEntity, Path path) throws IOException {
        GameObject root = scene.find(rootEntity);
        if (root == null) {
            throw new IllegalArgumentException("Prefab root entity not found");
        }
        List<GameObject> subtree = collectSubtree(root);
        Map<EntityId, Integer> prefabIds = new HashMap<>();
        int nextId = 1;
        for (GameObject object : subtree) {
            prefabIds.put(object.entity(), nextId++);
        }
        Map<Integer, Integer> sceneToPrefab = new HashMap<>();
        for (GameObject object : subtree) {
            int sceneId = org.llw.studio.scene.SceneObjectIds.get(scene.world(), object.entity());
            if (sceneId >= 0) {
                sceneToPrefab.put(sceneId, prefabIds.get(object.entity()));
            }
        }
        ObjectNode rootJson = MAPPER.createObjectNode();
        rootJson.put("version", VERSION);
        ArrayNode objects = rootJson.putArray("objects");
        for (GameObject object : subtree) {
            int id = prefabIds.get(object.entity());
            int parentId = -1;
            GameObject parent = object.parent();
            if (parent != null && prefabIds.containsKey(parent.entity())) {
                parentId = prefabIds.get(parent.entity());
            }
            objects.add(SceneObjectSerializer.writeObject(scene, object.entity(), id, parentId, sceneToPrefab));
        }
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), rootJson);
        ensureAssetMeta(path);
    }

    /**
     * @param path path to a {@code .prefab.json} file
     * @return parsed object array and detected export root id
     * @throws IOException if the file cannot be read
     */
    public static PrefabData load(Path path) throws IOException {
        return loadJson(Files.readString(path));
    }

    /**
     * @param json prefab JSON text
     * @return parsed object array and detected export root id
     * @throws IOException if the JSON cannot be parsed
     */
    public static PrefabData loadJson(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        ArrayNode objects = (ArrayNode) root.path("objects");
        int exportRootId = -1;
        for (JsonNode node : objects) {
            if (node.path("parentId").asInt(-1) < 0) {
                if (exportRootId < 0) {
                    exportRootId = node.path("id").asInt(1);
                }
            }
        }
        if (exportRootId < 0 && objects.size() > 0) {
            exportRootId = objects.get(0).path("id").asInt(1);
        }
        return new PrefabData(objects, exportRootId);
    }

    /**
     * Writes a prefab JSON document and ensures a meta sidecar exists.
     *
     * @param path destination file
     * @param document prefab JSON tree
     * @throws IOException if the file cannot be written
     */
    public static void saveDocument(Path path, JsonNode document) throws IOException {
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), document);
        ensureAssetMeta(path);
    }

    private static void ensureAssetMeta(Path path) throws IOException {
        Path assetsRoot = StudioProjectLayout.tryFindAssetsRoot(path);
        if (assetsRoot == null) {
            return;
        }
        Path projectRoot = assetsRoot.getParent();
        if (projectRoot == null) {
            return;
        }
        MetaFile.read(projectRoot, assetsRoot, path);
    }

    /**
     * @param root root game object
     * @return depth-first list of {@code root} and all descendants
     */
    public static List<GameObject> collectSubtree(GameObject root) {
        List<GameObject> result = new ArrayList<>();
        collectRecursive(root, result);
        return result;
    }

    private static void collectRecursive(GameObject object, List<GameObject> result) {
        result.add(object);
        for (GameObject child : object.children()) {
            collectRecursive(child, result);
        }
    }
}
