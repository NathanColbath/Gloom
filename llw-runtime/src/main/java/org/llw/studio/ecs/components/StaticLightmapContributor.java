package org.llw.studio.ecs.components;

/**
 * Marks a sprite renderer as contributing to static lightmap bakes.
 */
public final class StaticLightmapContributor implements Cloneable {
    public boolean enabled = true;

    public StaticLightmapContributor copy() {
        StaticLightmapContributor copy = new StaticLightmapContributor();
        copy.enabled = enabled;
        return copy;
    }

    @Override
    public StaticLightmapContributor clone() {
        return copy();
    }
}
