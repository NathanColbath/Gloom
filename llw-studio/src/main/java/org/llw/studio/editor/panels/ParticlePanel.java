package org.llw.studio.editor.panels;

import imgui.ImGui;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.OpenGlBackend;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.editor.particles.ParticleEditorState;
import org.llw.studio.editor.particles.ParticleModuleInspector;
import org.llw.studio.editor.particles.ParticlePreviewService;
import org.llw.studio.particles.ParticleSystemSerializer;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Particle system authoring panel with module properties and live GPU preview.
 */
public final class ParticlePanel implements EditorPanel, AutoCloseable {
    private final OpenGlBackend backend;
    private final AssetDatabase assets;
    private final SelectionService selection;
    private final PanelVisibility visibility;
    private final ParticleEditorState particleState;
    private final ParticleWorld particleWorld;
    private final ShaderGraphProgramCache shaderGraphCache;
    private final ParticlePreviewService previewService = new ParticlePreviewService();
    private OffscreenTarget previewTarget;
    private int previewWidth = 320;
    private int previewHeight = 240;
    private String lastSaveMessage = "";

    public ParticlePanel(
            OpenGlBackend backend,
            AssetDatabase assets,
            SelectionService selection,
            EditorSession session,
            PanelVisibility visibility,
            ParticleWorld particleWorld,
            ShaderGraphProgramCache shaderGraphCache
    ) {
        this.backend = backend;
        this.assets = assets;
        this.selection = selection;
        this.visibility = visibility;
        this.particleWorld = particleWorld;
        this.shaderGraphCache = shaderGraphCache;
        this.particleState = new ParticleEditorState();
        session.setParticleEditorState(particleState);
        previewTarget = new OffscreenTarget(backend, new IntSize(previewWidth, previewHeight));
    }

    @Override
    public String id() {
        return "particle_system";
    }

    @Override
    public String title() {
        return "Particles";
    }

    public ParticleEditorState particleState() {
        return particleState;
    }

    public void openAsset(String guid, Path path) {
        try {
            var document = ParticleSystemSerializer.load(path);
            particleState.setActiveAsset(guid, path, document);
            particleWorld.ensurePreview(guid, path, assets);
            lastSaveMessage = "";
            if (!visibility.isOpen(id())) {
                visibility.setOpen(id(), true);
            }
            visibility.focus(id());
        } catch (IOException ex) {
            lastSaveMessage = "Failed to open: " + ex.getMessage();
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
            syncFromEditor(context);
            if (particleState.activePath() == null) {
                ImGui.textWrapped(
                        "Select a particle asset in the Project panel, double-click it, or click Edit on a Particle Emitter.");
                if (!lastSaveMessage.isBlank()) {
                    ImGui.textColored(1f, 0.35f, 0.35f, 1f, lastSaveMessage);
                }
                return;
            }
            if (ImGui.button(particleState.playing() ? "Pause" : "Play")) {
                particleState.setPlaying(!particleState.playing());
            }
            ImGui.sameLine();
            if (ImGui.button("Restart")) {
                particleWorld.removePreview(particleState.activeGuid());
                float cx = previewWidth * 0.5f;
                float cy = previewHeight * 0.5f;
                EmitterState restarted = particleWorld.ensurePreview(
                        particleState.activeGuid(),
                        particleState.activePath(),
                        assets
                );
                restarted.worldX = cx;
                restarted.worldY = cy;
            }
            ImGui.sameLine();
            if (ImGui.button("Save")) {
                saveActive();
            }
            ImGui.sameLine();
            if (ImGui.checkbox("Loop Preview", particleState.loopPreview())) {
                particleState.setLoopPreview(!particleState.loopPreview());
            }
            ImGui.sameLine();
            if (ImGui.checkbox("Scene Preview", particleState.scenePreview())) {
                particleState.setScenePreview(!particleState.scenePreview());
            }
            if (!lastSaveMessage.isBlank()) {
                ImGui.textWrapped(lastSaveMessage);
            }
            float split = 0.28f;
            ImGui.beginChild("particle_modules", ImGui.getContentRegionAvailX() * split, 0f, true);
            ParticleModuleInspector.drawModuleList(particleState);
            ImGui.endChild();
            ImGui.sameLine();
            ImGui.beginChild("particle_main", 0f, 0f, true);
            float previewH = Math.max(120f, ImGui.getContentRegionAvailY() * 0.45f);
            ImGui.beginChild("particle_preview", 0f, previewH, true);
            renderPreview(context);
            ImGui.endChild();
            ImGui.beginChild("particle_props", 0f, 0f, true);
            ParticleModuleInspector.drawProperties(particleState, assets, this);
            ImGui.endChild();
            ImGui.endChild();
        } finally {
            visibility.end();
        }
    }

    private void syncFromEditor(StudioContext context) {
        StudioAsset selected = assets.selected();
        if (selected != null && selected.type() == AssetType.PARTICLE_SYSTEM && !selected.isFolder()) {
            if (!selected.guid().equals(particleState.activeGuid())) {
                openAsset(selected.guid(), selected.path());
            }
            return;
        }
        if (selected != null) {
            return;
        }
        EntityId entity = selection.selected();
        if (entity.isNone()) {
            return;
        }
        ParticleEmitterComponent emitter = context.editScene().world().getComponent(entity, ParticleEmitterComponent.class);
        if (emitter == null || emitter.particleSystemGuid == null || emitter.particleSystemGuid.isBlank()) {
            return;
        }
        if (emitter.particleSystemGuid.equals(particleState.activeGuid())) {
            return;
        }
        StudioAsset asset = assets.get(emitter.particleSystemGuid);
        if (asset != null && asset.type() == AssetType.PARTICLE_SYSTEM) {
            openAsset(asset.guid(), asset.path());
        }
    }

    private void renderPreview(StudioContext context) {
        int w = Math.max(64, (int) ImGui.getContentRegionAvailX());
        int h = Math.max(64, (int) ImGui.getContentRegionAvailY());
        if (w != previewWidth || h != previewHeight) {
            previewWidth = w;
            previewHeight = h;
            previewTarget.dispose();
            previewTarget = new OffscreenTarget(backend, new IntSize(w, h));
        }
        float centerX = previewWidth * 0.5f;
        float centerY = previewHeight * 0.5f;
        float delta = ImGui.getIO().getDeltaTime();
        if (particleState.playing()) {
            particleState.advancePreview(delta, particleWorld, assets, centerX, centerY);
        } else {
            // Paused: keep emitter at preview center without simulating so layout matches the image.
            EmitterState preview = particleWorld.previewEmitter(particleState.activeGuid());
            if (preview != null) {
                preview.worldX = centerX;
                preview.worldY = centerY;
            }
        }
        previewService.render(previewTarget, particleWorld, particleState.activeGuid(), assets, shaderGraphCache);
        Texture2d texture = previewTarget.colorTexture();
        if (texture != null) {
            ImGui.image(texture.id(), w, h, 0f, 1f, 1f, 0f);
        }
        EmitterStats stats = stats();
        ImGui.text("Alive: " + stats.alive + " / " + stats.capacity);
    }

    private EmitterStats stats() {
        var preview = particleWorld.previewEmitter(particleState.activeGuid());
        if (preview == null) {
            return new EmitterStats(0, 0);
        }
        return new EmitterStats(preview.pool.aliveCount(), preview.pool.capacity());
    }

    private record EmitterStats(int alive, int capacity) {
    }

    private void saveActive() {
        Path path = particleState.activePath();
        if (path == null) {
            return;
        }
        try {
            ParticleSystemSerializer.save(path, particleState.document());
            assets.bumpParticleRevision(particleState.activeGuid());
            assets.refresh();
            lastSaveMessage = "Saved " + path.getFileName();
        } catch (IOException ex) {
            lastSaveMessage = "Save failed: " + ex.getMessage();
        }
    }

    @Override
    public void close() {
        previewTarget.dispose();
    }
}
