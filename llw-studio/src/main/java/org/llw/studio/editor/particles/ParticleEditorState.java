package org.llw.studio.editor.particles;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.particles.ParticleSystemSerializer;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.scene.Scene;

import java.nio.file.Path;

/**
 * Active particle system being edited in {@link org.llw.studio.editor.panels.ParticlePanel}.
 */
public final class ParticleEditorState {
    private String activeGuid = "";
    private Path activePath;
    private ParticleSystemDocument document = ParticleSystemSerializer.newDefault();
    private boolean playing = true;
    private boolean loopPreview = true;
    private boolean scenePreview;
    private String selectedModule = "emission";

    public String activeGuid() {
        return activeGuid;
    }

    public Path activePath() {
        return activePath;
    }

    public ParticleSystemDocument document() {
        return document;
    }

    public boolean playing() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean loopPreview() {
        return loopPreview;
    }

    public void setLoopPreview(boolean loopPreview) {
        this.loopPreview = loopPreview;
    }

    public boolean scenePreview() {
        return scenePreview;
    }

    public void setScenePreview(boolean scenePreview) {
        this.scenePreview = scenePreview;
    }

    public String selectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(String selectedModule) {
        this.selectedModule = selectedModule == null ? "emission" : selectedModule;
    }

    public void setActiveAsset(String guid, Path path, ParticleSystemDocument loaded) {
        activeGuid = guid == null ? "" : guid;
        activePath = path;
        document = loaded == null ? ParticleSystemSerializer.newDefault() : loaded.copy();
        playing = true;
    }

    public void advancePreview(
            float deltaTime,
            ParticleWorld world,
            AssetDatabase assets,
            float centerX,
            float centerY
    ) {
        if (!playing || world == null || assets == null || activeGuid.isBlank() || activePath == null) {
            return;
        }
        EmitterState preview = world.ensurePreview(activeGuid, activePath, assets);
        preview.document = document;
        preview.playing = true;
        preview.worldX = centerX;
        preview.worldY = centerY;
        world.stepEmitter(preview, null, deltaTime, assets, null);
    }

    /**
     * Syncs scene emitters when scene preview is enabled (simulation runs in {@link ParticleWorld#syncScene}).
     */
    public void applyScenePreview(Scene scene, AssetDatabase assets, ParticleWorld world) {
        if (!scenePreview || scene == null || assets == null || world == null) {
            return;
        }
        world.syncScene(scene.world(), assets);
    }
}
