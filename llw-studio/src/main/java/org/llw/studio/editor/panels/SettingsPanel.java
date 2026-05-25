package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import org.llw.render.backend.RenderBackend;
import org.llw.render.backend.RendererType;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.settings.StudioSettingsStore;

/**
 * Closable studio settings window (graphics / renderer).
 */
public final class SettingsPanel implements EditorPanel {
    private final PanelVisibility visibility;
    private final StudioSettingsStore settingsStore;
    private final RenderBackend backend;

    public SettingsPanel(
            PanelVisibility visibility,
            StudioSettingsStore settingsStore,
            RenderBackend backend
    ) {
        this.visibility = visibility;
        this.settingsStore = settingsStore;
        this.backend = backend;
    }

    @Override
    public String id() {
        return "studio_settings";
    }

    @Override
    public String title() {
        return "Settings";
    }

    @Override
    public void render(StudioContext context) {
        if (!visibility.isOpen(id())) {
            return;
        }
        boolean draw = visibility.begin(id(), title(), ImGuiWindowFlags.None);
        try {
            if (!draw) {
                return;
            }
            renderGraphicsSection();
        } finally {
            visibility.end();
        }
    }

    private void renderGraphicsSection() {
        ImGui.text("Graphics");
        ImGui.separator();
        RendererType active = backend.rendererType();
        ImGui.text("Active renderer: " + active.displayName());

        ImGui.text("Editor renderer: OpenGL 3.3 (fixed)");
        ImGui.spacing();
        ImGui.textDisabled("bgfx / Vulkan / D3D11 apply to the standalone player build, not the editor.");
        // Player prefs may reference bgfx; clamp so editor session never expects a non-OpenGL backend.
        if (settingsStore.settings().rendererType() != RendererType.OPENGL) {
            settingsStore.clampStudioRenderer();
        }

        if (!active.usesBgfx()) {
            ImGui.spacing();
            ImGui.textDisabled("Shader graphs and custom materials use the OpenGL backend.");
        }
        ImGui.spacing();
        ImGui.textDisabled("Config: " + org.llw.render.backend.RendererPreferences.storePath());
    }
}
