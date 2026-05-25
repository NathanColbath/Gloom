package org.llw.studio.lighting;

import org.llw.render.graphics.Texture2d;

/**
 * Packed GPU light uniforms for one frame.
 */
public final class LightingFrameData {
    public static final int MAX_FLOATS = 64;
    public static final int MAX_DIRECTIONAL = 1;
    public static final int MAX_POINT = 8;
    public static final int MAX_SPOT = 4;

    public final float[] lightData = new float[MAX_FLOATS];
    public float ambientR = 0.15f;
    public float ambientG = 0.15f;
    public float ambientB = 0.18f;
    public float ambientIntensity = 1f;
    public boolean useLightmap;
    public Texture2d lightmapTexture;
    public float lightmapMinX;
    public float lightmapMinY;
    public float lightmapWidth = 1f;
    public float lightmapHeight = 1f;
    public boolean useLighting;
}
