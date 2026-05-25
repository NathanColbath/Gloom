package org.llw.studio.render;

import org.llw.math.geometry.RectF;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.backend.RenderBackend;
import org.llw.render.renderables.Sprite;
import org.llw.studio.lighting.LitUniformBinder;
import org.llw.studio.lighting.LightingFrameData;
import org.llw.studio.materials.runtime.ResolvedMaterial;

import java.util.function.Consumer;

/**
 * Queued lit sprite draw executed during {@link org.llw.render.graphics.RenderTarget#flush()}.
 */
final class LitSpriteDrawable implements Renderable {
    private final Sprite sprite;
    private final ShaderProgram program;
    private final float rotationDegrees;
    private final ResolvedMaterial material;
    private final Consumer<ShaderProgram> sceneLightingHook;

    LitSpriteDrawable(
            Sprite sprite,
            ShaderProgram program,
            float rotationDegrees,
            ResolvedMaterial material,
            LightingFrameData lighting
    ) {
        this.sprite = sprite;
        this.program = program;
        this.rotationDegrees = rotationDegrees;
        this.material = material;
        this.sceneLightingHook = shader -> LitUniformBinder.applyScene(shader, lighting);
    }

    @Override
    public void render(RenderBackend backend, DrawState state) {
        Texture2d texture = sprite.getTexture();
        if (texture == null) {
            return;
        }
        org.llw.math.matrix.Matrix3x2 model = sprite.getTransform();
        float width = sprite.getTextureRect().width * texture.size().width();
        float height = sprite.getTextureRect().height * texture.size().height();
        RectF uv = sprite.getTextureRect();
        float u0 = uv.left;
        float u1 = uv.left + uv.width;
        float vTop = uv.top + uv.height;
        float vBottom = uv.top;
        ShaderProgram activeShader = state.shader() != null ? state.shader() : program;
        Consumer<ShaderProgram> hook = shader -> {
            sceneLightingHook.accept(shader);
            LitUniformBinder.applyNormalMap(
                    shader,
                    material != null ? material.normalMap : null,
                    material != null && material.useNormalMap
            );
            LitUniformBinder.applySpriteRotation(shader, rotationDegrees);
            LitUniformBinder.applyMaterialProperties(shader, material);
        };
        long batchKey = LitBatchKey.of(program, texture, material, rotationDegrees);
        backend.drawLitTexturedQuad(
                model,
                0f, 0f, width, height,
                u0, vTop, u1, vBottom,
                sprite.getTint(),
                texture,
                activeShader,
                state.blendMode() != null ? state.blendMode() : BlendMode.ALPHA,
                batchKey,
                hook
        );
    }
}
