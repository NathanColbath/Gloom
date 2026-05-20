package org.llw.studio.editor.prefab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.serialization.SceneObjectSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads prefab JSON into an in-memory scene for inspector editing, then writes changes back.
 */
public final class PrefabScratchScene {
    private final Scene scene = new Scene();

    /** @return in-memory scene backing prefab edits */
    public Scene scene() {
        return scene;
    }

    /**
     * Rebuilds the scratch scene from prefab JSON.
     *
     * @param document prefab root object node
     */
    public void loadFromDocument(ObjectNode document) {
        clearEntities();
        ArrayNode objects = document.withArray("objects");
        Map<Integer, GameObject> byPrefabId = new HashMap<>();
        Map<Integer, Integer> identityRemap = new HashMap<>();

        for (JsonNode objectNode : objects) {
            int prefabId = objectNode.path("id").asInt();
            identityRemap.put(prefabId, prefabId);
            GameObject object = SceneObjectSerializer.readObject(scene, objectNode, prefabId);
            byPrefabId.put(prefabId, object);
        }

        SceneObjectSerializer.remapScriptEntityRefs(byPrefabId, identityRemap);
        SceneObjectSerializer.wireParents(objects, byPrefabId);
    }

    /**
     * Writes scratch entities back into the prefab {@code objects} array.
     *
     * @param document prefab root to update
     */
    public void writeToDocument(ObjectNode document) {
        ArrayNode objects = document.putArray("objects");
        Map<Integer, Integer> identityRemap = new HashMap<>();
        List<GameObject> prefabObjects = listPrefabObjects();
        for (GameObject object : prefabObjects) {
            int id = SceneObjectIds.get(scene.world(), object.entity());
            if (id >= 0) {
                identityRemap.put(id, id);
            }
        }
        for (GameObject object : prefabObjects) {
            int id = SceneObjectIds.get(scene.world(), object.entity());
            int parentId = -1;
            GameObject parent = object.parent();
            if (parent != null && !isSceneRoot(parent)) {
                parentId = SceneObjectIds.get(scene.world(), parent.entity());
            }
            objects.add(SceneObjectSerializer.writeObject(scene, object.entity(), id, parentId, identityRemap));
        }
    }

    /** @return prefab objects sorted by prefab id (excludes scene root) */
    public List<GameObject> listPrefabObjects() {
        List<GameObject> result = new ArrayList<>();
        var names = scene.world().store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId entity = names.entityAt(i);
            if (SceneObjectIds.isSceneRoot(scene.world(), entity)) {
                continue;
            }
            GameObject object = scene.find(entity);
            if (object != null) {
                result.add(object);
            }
        }
        result.sort((a, b) -> {
            int idA = SceneObjectIds.get(scene.world(), a.entity());
            int idB = SceneObjectIds.get(scene.world(), b.entity());
            return Integer.compare(idA, idB);
        });
        return result;
    }

    private void clearEntities() {
        for (GameObject object : new ArrayList<>(listPrefabObjects())) {
            scene.world().destroyEntity(object.entity());
        }
    }

    private boolean isSceneRoot(GameObject object) {
        return SceneObjectIds.isSceneRoot(scene.world(), object.entity());
    }
}
