package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.OpenGlBackend;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.shadergraph.assets.ShaderGraphSerializer;
import org.llw.studio.shadergraph.editor.ShaderGraphCanvas;
import org.llw.studio.shadergraph.editor.ShaderGraphEditorState;
import org.llw.studio.shadergraph.editor.ShaderGraphNodeInspector;
import org.llw.studio.shadergraph.editor.ShaderGraphPreviewService;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Shader graph authoring panel with node canvas and live GPU preview.
 */
public final class ShaderGraphPanel implements EditorPanel, AutoCloseable {
    private static final int PREVIEW_DEBOUNCE_MS = 120;

    private final OpenGlBackend backend;
    private final AssetDatabase assets;
    private final PanelVisibility visibility;
    private final ShaderGraphEditorState graphState;
    private final ShaderGraphProgramCache shaderGraphCache;
    private final ShaderGraphPreviewService previewService = new ShaderGraphPreviewService();
    private final ShaderGraphCanvas canvas = new ShaderGraphCanvas();
    private OffscreenTarget previewTarget;
    private int previewWidth = 256;
    private int previewHeight = 256;
    private long lastPreviewCompileMs;

    public ShaderGraphPanel(
            OpenGlBackend backend,
            AssetDatabase assets,
            EditorSession session,
            PanelVisibility visibility,
            ShaderGraphProgramCache shaderGraphCache
    ) {
        this.backend = backend;
        this.assets = assets;
        this.visibility = visibility;
        this.shaderGraphCache = shaderGraphCache;
        this.graphState = new ShaderGraphEditorState();
        session.setShaderGraphEditorState(graphState);
        previewTarget = new OffscreenTarget(backend, new IntSize(previewWidth, previewHeight));
    }

    @Override
    public String id() {
        return "shader_graph";
    }

    @Override
    public String title() {
        return "Shader Graph";
    }

    public ShaderGraphEditorState graphState() {
        return graphState;
    }

    /**
     * Opens a shader graph asset in this panel.
     */
    public void openAsset(String guid, Path path) {
        try {
            var document = ShaderGraphSerializer.load(path);
            graphState.setActiveAsset(guid, path, document);
            if (!visibility.isOpen(id())) {
                visibility.setOpen(id(), true);
            }
            visibility.focus(id());
        } catch (IOException ex) {
            graphState.setLastCompileError(ex.getMessage());
        }
    }

    @Override
    public void render(StudioContext context) {
        if (!visibility.isOpen(id())) {
            return;
        }
        boolean draw = visibility.begin(id(), title());
        try {
            if (!draw) {
                return;
            }
            syncFromEditor();
            if (!graphState.hasOpenAsset()) {
                ImGui.textDisabled("Create or open a .shadergraph.json asset from the Project panel.");
                return;
            }
            StudioAsset asset = assets.get(graphState.activeAssetGuid());
            String label = asset == null ? "Shader Graph" : asset.displayName();
            if (graphState.isDirty()) {
                label += " *";
            }
            ImGui.text(label);
            ImGui.sameLine();
            if (ImGui.button("Save")) {
                saveGraph();
            }
            ImGui.sameLine();
            if (ImGui.button("Compile")) {
                graphState.bumpRevision();
            }

            float split = ImGui.getContentRegionAvailX() * 0.62f;
            ImGui.beginChild("##ShaderGraphLeft", split, 0f, true);
            canvas.render(graphState);
            ImGui.endChild();

            ImGui.sameLine();
            ImGui.beginChild("##ShaderGraphRight", 0f, 0f, true);
            renderPreview();
            ImGui.separator();
            ShaderGraphNodeInspector.render(graphState, assets);
            ImGui.endChild();
        } finally {
            visibility.end();
        }
    }

    private void syncFromEditor() {
        StudioAsset selected = assets.selected();
        if (selected == null || selected.type() != AssetType.SHADER_GRAPH || selected.isFolder()) {
            return;
        }
        if (!selected.guid().equals(graphState.activeAssetGuid())) {
            openAsset(selected.guid(), selected.path());
        }
    }

    private void renderPreview() {
        ImGui.text("Preview");
        long now = System.currentTimeMillis();
        // Debounce compiles while typing; still refresh periodically if edits stall mid-keystroke.
        if (now - graphState.lastEditMillis() >= PREVIEW_DEBOUNCE_MS
                || now - lastPreviewCompileMs > 500) {
            previewService.ensureCompiled(graphState);
            lastPreviewCompileMs = now;
        }
        if (!graphState.lastCompileError().isBlank()) {
            ImGui.textColored(1f, 0.35f, 0.35f, 1f, graphState.lastCompileError());
        }
        float availX = ImGui.getContentRegionAvailX();
        float availY = Math.max(120f, ImGui.getContentRegionAvailY() * 0.45f);
        int w = Math.max(64, (int) availX);
        int h = Math.max(64, (int) availY);
        if (w != previewWidth || h != previewHeight) {
            previewTarget.dispose();
            previewWidth = w;
            previewHeight = h;
            previewTarget = new OffscreenTarget(backend, new IntSize(w, h));
        }
        Texture2d texture = resolvePreviewTexture();
        if (texture != null) {
            previewService.render(backend, previewTarget, texture, w, h);
            ImGui.image(previewTarget.colorTexture().id(), availX, availY, 0f, 1f, 1f, 0f);
        } else {
            ImGui.textDisabled("Assign a preview texture");
        }
    }

    private Texture2d resolvePreviewTexture() {
        String guid = graphState.document().previewTextureGuid;
        if (guid == null || guid.isBlank()) {
            for (StudioAsset asset : assets.allAssets()) {
                if (asset.type() == org.llw.studio.assets.AssetType.TEXTURE) {
                    return assets.texture(asset.guid());
                }
            }
            return null;
        }
        StudioAsset asset = assets.get(guid);
        if (asset == null) {
            return null;
        }
        return assets.texture(asset.guid());
    }

    private void saveGraph() {
        Path path = graphState.activeAssetPath();
        if (path == null) {
            return;
        }
        try {
            ShaderGraphSerializer.save(path, graphState.document());
            graphState.setDirty(false);
            assets.bumpShaderGraphRevision(graphState.activeAssetGuid());
            if (shaderGraphCache != null) {
                shaderGraphCache.invalidate(graphState.activeAssetGuid());
            }
            assets.refresh();
            graphState.bumpRevision();
        } catch (IOException ex) {
            graphState.setLastCompileError(ex.getMessage());
        }
    }

    @Override
    public void close() {
        previewService.dispose();
        if (previewTarget != null) {
            previewTarget.dispose();
        }
    }
}
