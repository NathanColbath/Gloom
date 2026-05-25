package org.llw.studio.ecs.components;

/**
 * 2D light source attached to an entity transform.
 */
public final class Light2DComponent implements Cloneable {
    public String type = "POINT";
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float intensity = 1f;
    public float range = 200f;
    public float innerAngle = 30f;
    public float outerAngle = 45f;
    public float falloff = 1f;
    public boolean includeInBake = true;
    public boolean castShadows;

    public Light2DComponent copy() {
        Light2DComponent copy = new Light2DComponent();
        copy.type = type;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.intensity = intensity;
        copy.range = range;
        copy.innerAngle = innerAngle;
        copy.outerAngle = outerAngle;
        copy.falloff = falloff;
        copy.includeInBake = includeInBake;
        copy.castShadows = castShadows;
        return copy;
    }

    @Override
    public Light2DComponent clone() {
        return copy();
    }
}
