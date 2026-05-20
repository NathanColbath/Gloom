package org.llw.studio.shadergraph.editor;

import org.llw.studio.shadergraph.model.ShaderGraphDocument;

import java.nio.file.Path;

/**
 * Active shader graph editing session (open asset, selection, preview revision).
 */
public final class ShaderGraphEditorState {
    private String activeAssetGuid = "";
    private Path activeAssetPath;
    private ShaderGraphDocument document = new ShaderGraphDocument();
    private String selectedNodeId = "";
    private int revision;
    private boolean dirty;
    private String lastCompileError = "";
    private long lastEditMillis;

    public String activeAssetGuid() {
        return activeAssetGuid;
    }

    public void setActiveAsset(String guid, Path path, ShaderGraphDocument loaded) {
        activeAssetGuid = guid == null ? "" : guid;
        activeAssetPath = path;
        document = loaded == null ? new ShaderGraphDocument() : loaded.copy();
        selectedNodeId = "";
        dirty = false;
        lastCompileError = "";
        bumpRevision();
    }

    public Path activeAssetPath() {
        return activeAssetPath;
    }

    public ShaderGraphDocument document() {
        return document;
    }

    public String selectedNodeId() {
        return selectedNodeId;
    }

    public void setSelectedNodeId(String selectedNodeId) {
        this.selectedNodeId = selectedNodeId == null ? "" : selectedNodeId;
        lastEditMillis = System.currentTimeMillis();
        revision++;
    }

    public int revision() {
        return revision;
    }

    public void bumpRevision() {
        revision++;
        touch();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void touch() {
        dirty = true;
        lastEditMillis = System.currentTimeMillis();
        revision++;
    }

    public long lastEditMillis() {
        return lastEditMillis;
    }

    public String lastCompileError() {
        return lastCompileError;
    }

    public void setLastCompileError(String lastCompileError) {
        this.lastCompileError = lastCompileError == null ? "" : lastCompileError;
    }

    public boolean hasOpenAsset() {
        return activeAssetPath != null;
    }

    /**
     * @return node id used for preview compile (selected, or fragment output)
     */
    public String previewRootNodeId() {
        if (selectedNodeId != null && !selectedNodeId.isBlank()) {
            return selectedNodeId;
        }
        for (var node : document.nodes) {
            if (node.type == org.llw.studio.shadergraph.model.ShaderNodeType.FragmentOutput) {
                return node.id;
            }
        }
        return "";
    }
}
