package org.llw.studio.shadergraph.model;

/**
 * Endpoint of a graph link (node id + pin id).
 */
public final class ShaderGraphPinRef {
    public String nodeId = "";
    public String pinId = "";

    public ShaderGraphPinRef() {
    }

    public ShaderGraphPinRef(String nodeId, String pinId) {
        this.nodeId = nodeId;
        this.pinId = pinId;
    }

    public ShaderGraphPinRef copy() {
        return new ShaderGraphPinRef(nodeId, pinId);
    }
}
