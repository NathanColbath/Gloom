package org.llw.studio.editor.lighting;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.ecs.components.StaticLightmapContributor;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.render.SceneBounds;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CPU lightmap bake triggered from the editor (not part of the runtime play loop).
 *
 * <p>Writes a PNG for {@link SceneLightingComponent}; uses ECS contributors and 2D lights in the edit scene.
 */
public final class LightBakeService {
    private static final int MAX_SIZE = 1024;

    private LightBakeService() {
    }

    public static void bake(Scene scene, AssetDatabase assets, SceneLightingComponent settings) {
        if (scene == null || assets == null || settings == null) {
            return;
        }
        SceneBounds.Bounds bounds = SceneBounds.compute(scene, assets);
        float minX = bounds.minX;
        float minY = bounds.minY;
        float maxX = bounds.maxX;
        float maxY = bounds.maxY;
        float width = Math.max(64f, maxX - minX);
        float height = Math.max(64f, maxY - minY);
        int texelsPerUnit = 8;
        // CPU bake is editor-only preview; cap resolution so large scenes stay interactive.
        int resW = Math.min(MAX_SIZE, Math.max(32, (int) (width * texelsPerUnit)));
        int resH = Math.min(MAX_SIZE, Math.max(32, (int) (height * texelsPerUnit)));
        float[] pixels = new float[resW * resH * 3];
        float ambientR = settings.ambientR * settings.ambientIntensity;
        float ambientG = settings.ambientG * settings.ambientIntensity;
        float ambientB = settings.ambientB * settings.ambientIntensity;
        for (int i = 0; i < pixels.length; i += 3) {
            pixels[i] = ambientR;
            pixels[i + 1] = ambientG;
            pixels[i + 2] = ambientB; // Fill ambient base before accumulating dynamic lights.
        }
        ComponentStore<Light2DComponent> lights = scene.world().store(Light2DComponent.class);
        for (int i = 0; i < lights.size(); i++) {
            EntityId entity = lights.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(scene.world(), entity)) {
                continue;
            }
            Light2DComponent light = lights.componentAt(i);
            if (!light.includeInBake) {
                continue;
            }
            WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
            float lx = world != null ? world.worldX : local != null ? local.x : 0f;
            float ly = world != null ? world.worldY : local != null ? local.y : 0f;
            accumulateLight(pixels, resW, resH, minX, minY, width, height, light, lx, ly);
        }
        try {
            Path out = assets.assetsRoot().resolve("SceneLighting.lightmap.png");
            writePng(out, pixels, resW, resH);
            settings.bakedLightmapGuid = registerLightmap(assets, out);
            settings.lightmapEnabled = true;
            // Store world AABB on component so runtime shader maps world position → lightmap UV.
            settings.lightmapMinX = minX;
            settings.lightmapMinY = minY;
            settings.lightmapMaxX = maxX;
            settings.lightmapMaxY = maxY;
        } catch (IOException ignored) {
        }
    }

    private static void accumulateLight(
            float[] pixels,
            int resW,
            int resH,
            float minX,
            float minY,
            float worldW,
            float worldH,
            Light2DComponent light,
            float lx,
            float ly
    ) {
        String type = light.type == null ? "POINT" : light.type.toUpperCase();
        for (int py = 0; py < resH; py++) {
            for (int px = 0; px < resW; px++) {
                // Texel center maps scene bounds to lightmap texels (Y-down world space).
                float wx = minX + (px + 0.5f) / resW * worldW;
                float wy = minY + (py + 0.5f) / resH * worldH;
                float factor = 0f;
                if ("DIRECTIONAL".equals(type)) {
                    factor = light.intensity * 0.5f;
                } else {
                    float dx = lx - wx;
                    float dy = ly - wy;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    factor = Math.max(0f, 1f - dist / Math.max(1f, light.range));
                    factor *= factor * light.intensity;
                }
                int idx = (py * resW + px) * 3;
                pixels[idx] += light.r * factor;
                pixels[idx + 1] += light.g * factor;
                pixels[idx + 2] += light.b * factor;
            }
        }
    }

    private static String registerLightmap(AssetDatabase assets, Path path) throws IOException {
        org.llw.studio.assets.MetaFile.MetaData meta = org.llw.studio.assets.MetaFile.read(
                assets.projectRoot(),
                assets.assetsRoot(),
                path
        );
        meta.type = org.llw.studio.assets.AssetType.LIGHTMAP.name();
        org.llw.studio.assets.MetaFile.write(assets.projectRoot(), assets.assetsRoot(), path, meta);
        assets.refresh();
        return meta.guid;
    }

    private static void writePng(Path path, float[] rgb, int w, int h) throws IOException {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = (y * w + x) * 3;
                int r = Math.min(255, Math.round(rgb[idx] * 255f));
                int g = Math.min(255, Math.round(rgb[idx + 1] * 255f));
                int b = Math.min(255, Math.round(rgb[idx + 2] * 255f));
                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        Files.createDirectories(path.getParent());
        javax.imageio.ImageIO.write(image, "png", path.toFile());
    }
}
