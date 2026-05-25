package org.llw.studio.settings;

import org.llw.render.backend.RendererPreferences;
import org.llw.render.backend.RendererType;

/**
 * Loads and saves studio settings via {@link RendererPreferences} ({@code ~/.llw-studio/settings.json}).
 */
public final class StudioSettingsStore {
    private final StudioSettings settings;

    public StudioSettingsStore() {
        RendererPreferences prefs = RendererPreferences.load();
        settings = new StudioSettings(prefs.rendererType());
    }

    public StudioSettings settings() {
        return settings;
    }

    public void setRendererType(RendererType rendererType) {
        settings.setRendererType(rendererType);
        RendererPreferences prefs = new RendererPreferences(rendererType);
        prefs.save();
    }

    public void save() {
        RendererPreferences prefs = new RendererPreferences(settings.rendererType());
        prefs.save();
    }

    /**
     * Ensures persisted renderer choice is safe for the editor (OpenGL + ImGui). Rewrites
     * {@code ~/.llw-studio/settings.json} when a bgfx-only value was saved from an older build.
     */
    public void clampStudioRenderer() {
        // Studio requires OpenGL for ImGui; rewrite persisted settings if an older build saved bgfx-only.
        if (!settings.rendererType().usesBgfx()) {
            return;
        }
        settings.setRendererType(RendererType.OPENGL);
        save();
    }
}
