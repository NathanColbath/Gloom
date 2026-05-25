package org.llw.studio.lighting;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gathers active lights and scene lighting settings into {@link LightingFrameData}.
 */
public final class LightingSystem {
    private LightingSystem() {
    }

    public static LightingFrameData gather(Scene scene, AssetDatabase assets) {
        LightingFrameData frame = new LightingFrameData();
        if (scene == null) {
            return frame;
        }
        World world = scene.world();
        SceneLightingComponent settings = firstSceneLighting(world);
        if (settings != null) {
            frame.ambientR = settings.ambientR;
            frame.ambientG = settings.ambientG;
            frame.ambientB = settings.ambientB;
            frame.ambientIntensity = settings.ambientIntensity;
            if (settings.lightmapEnabled
                    && settings.bakedLightmapGuid != null
                    && !settings.bakedLightmapGuid.isBlank()
                    && assets != null) {
                frame.useLightmap = true;
                frame.lightmapTexture = assets.texture(settings.bakedLightmapGuid);
                frame.lightmapMinX = settings.lightmapMinX;
                frame.lightmapMinY = settings.lightmapMinY;
                frame.lightmapWidth = Math.max(1f, settings.lightmapMaxX - settings.lightmapMinX);
                frame.lightmapHeight = Math.max(1f, settings.lightmapMaxY - settings.lightmapMinY);
            }
        }
        List<LightEntry> lights = collectLights(world);
        if (lights.isEmpty() && !frame.useLightmap) {
            frame.useLighting = settings != null;
            return frame;
        }
        frame.useLighting = true;
        packLights(frame, lights);
        return frame;
    }

    private static SceneLightingComponent firstSceneLighting(World world) {
        ComponentStore<SceneLightingComponent> store = world.store(SceneLightingComponent.class);
        if (store.size() == 0) {
            return null;
        }
        return store.componentAt(0);
    }

    private static List<LightEntry> collectLights(World world) {
        List<LightEntry> entries = new ArrayList<>();
        ComponentStore<Light2DComponent> lights = world.store(Light2DComponent.class);
        for (int i = 0; i < lights.size(); i++) {
            EntityId entity = lights.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(world, entity)) {
                continue;
            }
            Light2DComponent light = lights.componentAt(i);
            if ("GLOBAL".equalsIgnoreCase(light.type)) {
                continue;
            }
            WorldTransformComponent worldTransform = world.getComponent(entity, WorldTransformComponent.class);
            Transform2DComponent local = world.getComponent(entity, Transform2DComponent.class);
            float x = worldTransform != null ? worldTransform.worldX : local != null ? local.x : 0f;
            float y = worldTransform != null ? worldTransform.worldY : local != null ? local.y : 0f;
            float rotation = worldTransform != null ? worldTransform.worldRotation : local != null ? local.rotation : 0f;
            float score = light.intensity * light.range;
            entries.add(new LightEntry(light, x, y, rotation, score));
        }
        entries.sort(Comparator.comparingDouble((LightEntry e) -> e.score).reversed());
        return entries;
    }

    private static void packLights(LightingFrameData frame, List<LightEntry> entries) {
        int directional = 0;
        int point = 0;
        int spot = 0;
        int idx = 4;
        for (LightEntry entry : entries) {
            Light2DComponent light = entry.light;
            String type = light.type == null ? "POINT" : light.type.toUpperCase();
            if ("DIRECTIONAL".equals(type) && directional < LightingFrameData.MAX_DIRECTIONAL) {
                float rad = (float) Math.toRadians(entry.rotation - 90f);
                frame.lightData[idx++] = (float) Math.cos(rad);
                frame.lightData[idx++] = (float) Math.sin(rad);
                frame.lightData[idx++] = light.r;
                frame.lightData[idx++] = light.g;
                frame.lightData[idx++] = light.b;
                frame.lightData[idx++] = light.intensity;
                directional++;
            } else if ("POINT".equals(type) && point < LightingFrameData.MAX_POINT) {
                frame.lightData[idx++] = entry.x;
                frame.lightData[idx++] = entry.y;
                frame.lightData[idx++] = light.r;
                frame.lightData[idx++] = light.g;
                frame.lightData[idx++] = light.b;
                frame.lightData[idx++] = light.intensity;
                frame.lightData[idx++] = light.range;
                point++;
            } else if ("SPOT".equals(type) && spot < LightingFrameData.MAX_SPOT) {
                frame.lightData[idx++] = entry.x;
                frame.lightData[idx++] = entry.y;
                frame.lightData[idx++] = light.r;
                frame.lightData[idx++] = light.g;
                frame.lightData[idx++] = light.b;
                frame.lightData[idx++] = light.intensity;
                frame.lightData[idx++] = light.range;
                float rad = (float) Math.toRadians(entry.rotation - 90f);
                frame.lightData[idx++] = (float) Math.cos(rad);
                frame.lightData[idx++] = (float) Math.sin(rad);
                float innerHalf = Math.max(0.5f, light.innerAngle * 0.5f);
                float outerHalf = Math.max(innerHalf + 0.5f, light.outerAngle * 0.5f);
                frame.lightData[idx++] = (float) Math.cos(Math.toRadians(innerHalf));
                frame.lightData[idx++] = (float) Math.cos(Math.toRadians(outerHalf));
                spot++;
            }
            if (directional >= LightingFrameData.MAX_DIRECTIONAL
                    && point >= LightingFrameData.MAX_POINT
                    && spot >= LightingFrameData.MAX_SPOT) {
                break;
            }
        }
        frame.lightData[0] = directional;
        frame.lightData[1] = point;
        frame.lightData[2] = spot;
    }

    private record LightEntry(Light2DComponent light, float x, float y, float rotation, float score) {
    }
}
