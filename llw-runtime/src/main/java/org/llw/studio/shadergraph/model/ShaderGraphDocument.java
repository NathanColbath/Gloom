package org.llw.studio.shadergraph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable shader graph asset (nodes, links, preview settings).
 */
public final class ShaderGraphDocument {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public String previewTextureGuid = "";
    public final List<ShaderGraphNode> nodes = new ArrayList<>();
    public final List<ShaderGraphLink> links = new ArrayList<>();

    public ShaderGraphDocument copy() {
        ShaderGraphDocument copy = new ShaderGraphDocument();
        copy.version = version;
        copy.previewTextureGuid = previewTextureGuid;
        for (ShaderGraphNode node : nodes) {
            copy.nodes.add(node.copy());
        }
        for (ShaderGraphLink link : links) {
            copy.links.add(link.copy());
        }
        return copy;
    }

    public ShaderGraphNode nodeById(String id) {
        if (id == null) {
            return null;
        }
        for (ShaderGraphNode node : nodes) {
            if (id.equals(node.id)) {
                return node;
            }
        }
        return null;
    }

    public int revisionHash() {
        int hash = version;
        hash = 31 * hash + (previewTextureGuid == null ? 0 : previewTextureGuid.hashCode());
        hash = 31 * hash + nodes.hashCode();
        hash = 31 * hash + links.hashCode();
        return hash;
    }
}
