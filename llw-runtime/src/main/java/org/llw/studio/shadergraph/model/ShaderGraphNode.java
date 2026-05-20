package org.llw.studio.shadergraph.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single node in a shader graph with editor position and typed parameters.
 */
public final class ShaderGraphNode {
    public String id = "";
    public ShaderNodeType type = ShaderNodeType.Color;
    public float x;
    public float y;
    /** Parameter values keyed by name (e.g. {@code r}, {@code g}, {@code value}). */
    public Map<String, Float> params = new LinkedHashMap<>();

    public ShaderGraphNode copy() {
        ShaderGraphNode copy = new ShaderGraphNode();
        copy.id = id;
        copy.type = type;
        copy.x = x;
        copy.y = y;
        copy.params = new LinkedHashMap<>(params);
        return copy;
    }

    public float param(String key, float defaultValue) {
        Float value = params.get(key);
        return value == null ? defaultValue : value;
    }
}
