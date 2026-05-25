package org.llw.render.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RendererTypeTest {
    @Test
    void settingIdRoundTrip() {
        for (RendererType type : RendererType.values()) {
            assertEquals(type, RendererType.fromSettingId(type.settingId()));
        }
    }

    @Test
    void envAliases() {
        assertEquals(RendererType.BGFX_VULKAN, RendererType.fromEnvAlias("bgfx-vulkan"));
        assertEquals(RendererType.BGFX_DIRECT3D11, RendererType.fromEnvAlias("d3d11"));
        assertEquals(RendererType.BGFX_OPENGL, RendererType.fromEnvAlias("bgfx"));
    }
}
