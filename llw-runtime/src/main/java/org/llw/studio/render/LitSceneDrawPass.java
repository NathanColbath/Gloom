package org.llw.studio.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.backend.RenderBackend;
import org.llw.render.renderables.Sprite;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteArtFacing;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TextureImportSettings;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.lighting.LightingFrameData;
import org.llw.studio.materials.model.MaterialShaderSource;
import org.llw.studio.materials.runtime.MaterialProgramCache;
import org.llw.studio.materials.runtime.ResolvedMaterial;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders sprites with the built-in lit material path and optional shader graphs.
 */
public final class LitSceneDrawPass {
    private static final TransformSystem TRANSFORMS = new TransformSystem();

    private LitSceneDrawPass() {
    }

    public static void draw(
            Scene scene,
            OffscreenTarget target,
            AssetDatabase assets,
            MaterialProgramCache materials,
            LightingFrameData lighting,
            RenderBackend backend,
            org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache shaderGraphs
    ) {
        if (scene == null || target == null || assets == null || backend == null) {
            return;
        }
        TRANSFORMS.onUpdate(scene.world(), 0f);
        ComponentStore<SpriteRendererComponent> sprites = scene.world().store(SpriteRendererComponent.class);
        List<DrawEntry> entries = new ArrayList<>(sprites.size());
        for (int i = 0; i < sprites.size(); i++) {
            EntityId entity = sprites.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            SpriteRendererComponent renderer = sprites.componentAt(i);
            SpriteDefinition slice = SpriteResolve.resolve(assets, renderer);
            if (slice == null) {
                continue;
            }
            Texture2d texture = assets.texture(slice.textureGuid());
            if (texture == null) {
                continue;
            }
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            if (local == null) {
                continue;
            }
            float x = world != null ? world.worldX : local.x;
            float y = world != null ? world.worldY : local.y;
            float rotation = world != null ? world.worldRotation : local.rotation;
            rotation = applyArtFacing(assets, slice, rotation);
            float scaleX = world != null ? world.worldScaleX : local.scaleX;
            float scaleY = world != null ? world.worldScaleY : local.scaleY;
            entries.add(new DrawEntry(renderer.sortingOrder, texture, slice, x, y, rotation, scaleX, scaleY, renderer));
        }
        entries.sort(Comparator
                .comparingInt((DrawEntry entry) -> entry.sortingOrder)
                .thenComparingLong(entry -> litSortKey(entry, materials, shaderGraphs, backend)));
        for (DrawEntry entry : entries) {
            ResolvedMaterial material = resolveMaterial(
                    rendererMaterialGuid(entry.renderer),
                    materials,
                    entry.renderer,
                    shaderGraphs
            );
            ShaderProgram program = material != null && material.program != null
                    ? material.program
                    : backend.shaderLibrary().litSpriteShader();
            boolean litPath = material == null || material.isLit() || material.shaderSource == MaterialShaderSource.SHADER_GRAPH;
            Sprite sprite = new Sprite(entry.texture);
            int tw = entry.texture.size().width();
            int th = entry.texture.size().height();
            sprite.setTextureRect(entry.slice.uvRect(tw, th));
            SpritePlacement.applyCentered(
                    sprite,
                    entry.slice,
                    entry.x,
                    entry.y,
                    entry.rotation,
                    entry.scaleX,
                    entry.scaleY
            );
            sprite.setTint(new Color(
                    (int) (entry.renderer.r * 255),
                    (int) (entry.renderer.g * 255),
                    (int) (entry.renderer.b * 255),
                    (int) (entry.renderer.a * 255)
            ));
            int layer = RenderLayers.SCENE_BASE + entry.sortingOrder;
            if (litPath && lighting != null && lighting.useLighting) {
                target.draw(
                        new LitSpriteDrawable(sprite, program, entry.rotation, material, lighting),
                        DrawState.DEFAULT.withLayer(layer).withShader(program)
                );
            } else {
                DrawState state = DrawState.DEFAULT.withLayer(layer).withShader(program);
                target.draw(sprite, state);
            }
        }
    }

    private static long litSortKey(
            DrawEntry entry,
            MaterialProgramCache materials,
            org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache shaderGraphs,
            RenderBackend backend
    ) {
        ResolvedMaterial material = resolveMaterial(
                rendererMaterialGuid(entry.renderer),
                materials,
                entry.renderer,
                shaderGraphs
        );
        ShaderProgram program = material != null && material.program != null
                ? material.program
                : backend.shaderLibrary().litSpriteShader();
        return LitBatchKey.of(program, entry.texture, material, entry.rotation);
    }

    private static String rendererMaterialGuid(SpriteRendererComponent renderer) {
        if (renderer.materialGuid != null && !renderer.materialGuid.isBlank()) {
            return renderer.materialGuid;
        }
        return "";
    }

    private static ResolvedMaterial resolveMaterial(
            String materialGuid,
            MaterialProgramCache materials,
            SpriteRendererComponent renderer,
            org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache shaderGraphs
    ) {
        if (materials != null && materialGuid != null && !materialGuid.isBlank()) {
            ResolvedMaterial resolved = materials.resolve(materialGuid);
            if (resolved != null) {
                return resolved;
            }
        }
        if (shaderGraphs != null
                && renderer.shaderGraphGuid != null
                && !renderer.shaderGraphGuid.isBlank()) {
            ShaderProgram program = shaderGraphs.program(renderer.shaderGraphGuid);
            if (program != null) {
                return new ResolvedMaterial(MaterialShaderSource.SHADER_GRAPH, program, null, false);
            }
        }
        return null;
    }

    private static float applyArtFacing(AssetDatabase assets, SpriteDefinition slice, float rotationDegrees) {
        if (assets == null || slice == null || slice.textureGuid() == null || slice.textureGuid().isBlank()) {
            return rotationDegrees;
        }
        StudioAsset textureAsset = assets.get(slice.textureGuid());
        if (textureAsset == null) {
            return rotationDegrees;
        }
        TextureImportSettings settings = assets.readTextureImportSettings(textureAsset.path());
        SpriteArtFacing facing = settings.artFacing == null ? SpriteArtFacing.RIGHT : settings.artFacing;
        return facing.applyToRotation(rotationDegrees);
    }

    private record DrawEntry(
            int sortingOrder,
            Texture2d texture,
            SpriteDefinition slice,
            float x,
            float y,
            float rotation,
            float scaleX,
            float scaleY,
            SpriteRendererComponent renderer
    ) {
    }
}
