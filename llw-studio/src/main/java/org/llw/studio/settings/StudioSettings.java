package org.llw.studio.settings;

import org.llw.render.backend.RendererType;

/**
 * Persisted LLW Studio preferences (stored in {@link StudioSettingsStore}).
 */
public final class StudioSettings {
    private RendererType rendererType = RendererType.OPENGL;

    public StudioSettings() {
    }

    public StudioSettings(RendererType rendererType) {
        this.rendererType = rendererType == null ? RendererType.OPENGL : rendererType;
    }

    public RendererType rendererType() {
        return rendererType;
    }

    public void setRendererType(RendererType rendererType) {
        this.rendererType = rendererType == null ? RendererType.OPENGL : rendererType;
    }
}
