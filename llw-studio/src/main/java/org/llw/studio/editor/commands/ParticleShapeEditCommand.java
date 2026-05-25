package org.llw.studio.editor.commands;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.particles.ParticleSystemSerializer;
import org.llw.studio.particles.model.ParticleSystemDocument;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Undoable edit of a particle system asset shape module from scene-view gizmo handles.
 */
public final class ParticleShapeEditCommand implements EditorCommand {
    private final AssetDatabase assets;
    private final String particleGuid;
    private final ParticleSystemDocument.ShapeModule before;
    private final ParticleSystemDocument.ShapeModule after;

    /**
     * @param assets        project assets
     * @param particleGuid  particle system asset GUID
     * @param before        shape before edit
     * @param after         shape after edit
     */
    public ParticleShapeEditCommand(
            AssetDatabase assets,
            String particleGuid,
            ParticleSystemDocument.ShapeModule before,
            ParticleSystemDocument.ShapeModule after
    ) {
        this.assets = assets;
        this.particleGuid = particleGuid;
        this.before = before == null ? null : before.copy();
        this.after = after == null ? null : after.copy(); // Gizmo drag mutates live doc between snapshots.
    }

    @Override
    public void execute() {
        apply(after);
    }

    @Override
    public void undo() {
        apply(before);
    }

    private void apply(ParticleSystemDocument.ShapeModule shape) {
        if (shape == null || particleGuid == null || particleGuid.isBlank()) {
            return;
        }
        var asset = assets.get(particleGuid);
        if (asset == null) {
            return;
        }
        Path path = asset.path();
        ParticleSystemDocument doc = assets.loadParticleSystem(path);
        if (doc == null) {
            return;
        }
        doc.modules.shape = shape.copy();
        try {
            ParticleSystemSerializer.save(path, doc);
            assets.bumpParticleRevision(particleGuid); // Shape lives on disk asset, not an ECS component.
        } catch (IOException ignored) {
            // Best-effort persistence for gizmo edits.
        }
    }
}
