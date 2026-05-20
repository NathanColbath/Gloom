package org.llw.studio.serialization;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Registry mapping ECS component types to JSON writers and readers used by scene serialization.
 */
public final class ComponentSerializerRegistry {
    private final Map<Class<?>, BiConsumer<ObjectNode, Object>> writers = new HashMap<>();
    private final Map<String, BiConsumer<org.llw.studio.scene.GameObject, ObjectNode>> readers = new HashMap<>();

    /**
     * Registers serialization for a component type.
     *
     * @param <T> component class
     * @param type component class token
     * @param jsonKey object property name in scene JSON
     * @param writer writes component fields into a JSON object
     * @param reader builds a component instance from JSON
     * @param applier attaches the read component to a {@link org.llw.studio.scene.GameObject}
     */
    public <T> void register(
            Class<T> type,
            String jsonKey,
            BiConsumer<ObjectNode, T> writer,
            Function<ObjectNode, T> reader,
            BiConsumer<org.llw.studio.scene.GameObject, T> applier
    ) {
        writers.put(type, (node, component) -> writer.accept(node, type.cast(component)));
        readers.put(jsonKey, (object, node) -> applier.accept(object, reader.apply(node)));
    }

    /**
     * @param node target JSON object
     * @param type component class
     * @param component component instance to serialize, or {@code null} to skip
     */
    public void write(ObjectNode node, Class<?> type, Object component) {
        BiConsumer<ObjectNode, Object> writer = writers.get(type);
        if (writer != null && component != null) {
            writer.accept(node, component);
        }
    }

    /**
     * @param object game object receiving the component
     * @param key JSON property name
     * @param node component JSON subtree
     */
    public void read(org.llw.studio.scene.GameObject object, String key, ObjectNode node) {
        BiConsumer<org.llw.studio.scene.GameObject, ObjectNode> reader = readers.get(key);
        if (reader != null) {
            reader.accept(object, node);
        }
    }
}
