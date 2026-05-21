package org.llw.studio.particles.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;
import org.llw.studio.assets.AssetDatabase;
import org.llw.math.geometry.RectF;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.particles.runtime.EmitterState;
import org.llw.studio.particles.runtime.Particle;
import org.llw.studio.particles.runtime.ParticlePool;
import org.llw.studio.particles.runtime.ParticleSimulator;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.render.RenderLayers;
import org.llw.studio.render.SpritePlacement;
import org.llw.studio.render.SpriteResolve;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Draws simulated particles from a {@link ParticleWorld}.
 */
public final class ParticleDrawPass {
    private ParticleDrawPass() {
    }

    /**
     * @param world         particle simulation state
     * @param target        render target
     * @param assets        textures and sprites
     * @param shaderGraphs  optional per-particle shader programs
     */
    public static void draw(
            ParticleWorld world,
            OffscreenTarget target,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs
    ) {
        if (world == null || target == null || assets == null) {
            return;
        }
        List<DrawBatch> batches = new ArrayList<>();
        for (EmitterState state : world.allEmitters()) {
            if (state.document == null) {
                continue;
            }
            collectEmitter(batches, state, assets, shaderGraphs);
        }
        flushBatches(batches, target);
        drawTrails(world, target);
    }

    /**
     * Draws a single detached preview emitter (panel viewport).
     */
    public static void drawPreviewEmitter(
            EmitterState state,
            OffscreenTarget target,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs
    ) {
        if (state == null || state.document == null || target == null || assets == null) {
            return;
        }
        List<DrawBatch> batches = new ArrayList<>();
        collectEmitter(batches, state, assets, shaderGraphs);
        flushBatches(batches, target);
        drawEmitterTrails(state, target);
    }

    private static void flushBatches(List<DrawBatch> batches, OffscreenTarget target) {
        batches.sort(Comparator
                .comparingInt((DrawBatch batch) -> batch.layer)
                .thenComparingInt(batch -> batch.textureId)
                .thenComparingInt(batch -> batch.blend.ordinal())
                .thenComparingInt(batch -> batch.shaderId));
        for (DrawBatch batch : batches) {
            target.draw(batch.sprite, batch.state);
        }
    }

    private static void collectEmitter(
            List<DrawBatch> batches,
            EmitterState state,
            AssetDatabase assets,
            ShaderGraphProgramCache shaderGraphs
    ) {
        ParticleSystemDocument doc = state.document;
        SpriteDefinition slice = ParticleSpriteResolve.resolve(assets, doc.modules.renderer);
        if (slice == null) {
            return;
        }
        Texture2d texture = assets.texture(slice.textureGuid());
        if (texture == null) {
            return;
        }
        int tw = texture.size().width();
        int th = texture.size().height();
        BlendMode blend = ParticleSimulator.blendMode(doc.modules.renderer.blendMode);
        int layer = RenderLayers.SCENE_BASE + doc.modules.renderer.sortingOrder;
        ShaderProgram shader = null;
        if (shaderGraphs != null
                && doc.modules.renderer.shaderGraphGuid != null
                && !doc.modules.renderer.shaderGraphGuid.isBlank()) {
            shader = shaderGraphs.program(doc.modules.renderer.shaderGraphGuid);
        }
        DrawState baseState = DrawState.DEFAULT
                .withBlendMode(blend)
                .withLayer(RenderLayers.SCENE_BASE + doc.modules.renderer.sortingOrder);
        if (shader != null) {
            baseState = baseState.withShader(shader);
        }
        ParticlePool pool = state.pool;
        for (int i = 0; i < pool.capacity(); i++) {
            Particle particle = pool.particleAt(i);
            if (!particle.alive) {
                continue;
            }
            Sprite sprite = new Sprite(texture);
            RectF uv = slice.uvRect(tw, th);
            if (doc.modules.textureSheet.enabled) {
                uv = sheetUv(uv, doc.modules.textureSheet, particle.frameIndex);
            }
            sprite.setTextureRect(uv);
            SpritePlacement.applyCentered(
                    sprite,
                    slice,
                    particle.x,
                    particle.y,
                    particle.rotation,
                    particle.size / Math.max(1f, slice.width()),
                    particle.size / Math.max(1f, slice.height())
            );
            sprite.setTint(new Color(
                    Math.round(particle.r * 255),
                    Math.round(particle.g * 255),
                    Math.round(particle.b * 255),
                    Math.round(particle.a * 255)
            ));
            batches.add(new DrawBatch(sprite, baseState, texture.id(), blend, shader == null ? 0 : shader.programId(), layer));
        }
    }

    private static RectF sheetUv(RectF base, ParticleSystemDocument.TextureSheetModule sheet, int frameIndex) {
        int tilesX = Math.max(1, sheet.tilesX);
        int tilesY = Math.max(1, sheet.tilesY);
        int col = frameIndex % tilesX;
        int row = frameIndex / tilesX;
        if (row >= tilesY) {
            row = tilesY - 1;
            col = tilesX - 1;
        }
        float du = base.width / tilesX;
        float dv = base.height / tilesY;
        return new RectF(
                base.left + col * du,
                base.top + row * dv,
                du,
                dv
        );
    }

    private static void drawTrails(ParticleWorld world, OffscreenTarget target) {
        for (EmitterState state : world.allEmitters()) {
            drawEmitterTrails(state, target);
        }
    }

    private static void drawEmitterTrails(EmitterState state, OffscreenTarget target) {
        if (state == null || state.document == null) {
            return;
        }
        ParticleSystemDocument.TrailsModule trails = state.document.modules.trails;
        if (!trails.enabled) {
            return;
        }
        ParticlePool pool = state.pool;
        for (int i = 0; i < pool.capacity(); i++) {
            Particle particle = pool.particleAt(i);
            if (!particle.alive || particle.trailCount < 2) {
                continue;
            }
            for (int t = 0; t < particle.trailCount - 1; t++) {
                float alpha = 1f - (t / (float) particle.trailCount);
                float x0 = particle.trailX[t];
                float y0 = particle.trailY[t];
                float x1 = particle.trailX[t + 1];
                float y1 = particle.trailY[t + 1];
                org.llw.render.renderables.Rectangle line = new org.llw.render.renderables.Rectangle();
                line.setPosition(Math.min(x0, x1), Math.min(y0, y1));
                line.setSize(Math.max(1f, Math.abs(x1 - x0)), Math.max(1f, Math.abs(y1 - y0)));
                line.setFillColor(new Color(255, 255, 255, Math.round(alpha * particle.a * 128)));
                target.draw(line, DrawState.DEFAULT.withLayer(RenderLayers.SCENE_BASE + 1));
            }
        }
    }

    private record DrawBatch(Sprite sprite, DrawState state, int textureId, BlendMode blend, int shaderId, int layer) {
    }
}
