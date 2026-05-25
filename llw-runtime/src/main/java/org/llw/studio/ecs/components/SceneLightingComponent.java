package org.llw.studio.ecs.components;

/**
 * Scene-wide ambient and baked lightmap settings (attach to a single manager object).
 */
public final class SceneLightingComponent implements Cloneable {
    public float ambientR = 0.15f;
    public float ambientG = 0.15f;
    public float ambientB = 0.18f;
    public float ambientIntensity = 1f;
    public String bakedLightmapGuid = "";
    public boolean lightmapEnabled;
    public float lightmapMinX;
    public float lightmapMinY;
    public float lightmapMaxX = 1024f;
    public float lightmapMaxY = 1024f;

    public SceneLightingComponent copy() {
        SceneLightingComponent copy = new SceneLightingComponent();
        copy.ambientR = ambientR;
        copy.ambientG = ambientG;
        copy.ambientB = ambientB;
        copy.ambientIntensity = ambientIntensity;
        copy.bakedLightmapGuid = bakedLightmapGuid;
        copy.lightmapEnabled = lightmapEnabled;
        copy.lightmapMinX = lightmapMinX;
        copy.lightmapMinY = lightmapMinY;
        copy.lightmapMaxX = lightmapMaxX;
        copy.lightmapMaxY = lightmapMaxY;
        return copy;
    }

    @Override
    public SceneLightingComponent clone() {
        return copy();
    }
}
