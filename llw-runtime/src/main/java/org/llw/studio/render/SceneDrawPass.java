package org.llw.studio.render;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
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
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders all active {@link SpriteRendererComponent} entities into an offscreen target,
 * sorted by {@code sortingOrder}.
 */
public final class SceneDrawPass {
    private SceneDrawPass() {
    }

    /**
     * @param scene   scene to draw
     * @param target  offscreen render target
     * @param assets  textures keyed by GUID
     */
    public static void draw(Scene scene, OffscreenTarget target, AssetDatabase assets, ShaderGraphProgramCache shaderGraphs) {
        new TransformSystem().onUpdate(scene.world(), 0f);
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
        entries.sort(Comparator.comparingInt(entry -> entry.sortingOrder));
        for (DrawEntry entry : entries) {
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
            SpriteRendererComponent renderer = entry.renderer;
            sprite.setTint(new Color(
                    (int) (renderer.r * 255),
                    (int) (renderer.g * 255),
                    (int) (renderer.b * 255),
                    (int) (renderer.a * 255)
            ));
            DrawState state = DrawState.DEFAULT.withLayer(RenderLayers.SCENE_BASE + entry.sortingOrder);
            if (shaderGraphs != null
                    && renderer.shaderGraphGuid != null
                    && !renderer.shaderGraphGuid.isBlank()) {
                ShaderProgram program = shaderGraphs.program(renderer.shaderGraphGuid);
                if (program != null) {
                    state = state.withShader(program);
                }
            }
            target.draw(sprite, state);
        }
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
