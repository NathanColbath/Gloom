package org.llw.render.backend;

import org.llw.render.bgfx.BgfxRenderBackend;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.window.Window;

/**
 * Creates {@link RenderBackend} instances for the requested {@link RendererType}.
 */
public final class RenderBackendFactory {
    private RenderBackendFactory() {
    }

    public static RenderBackend create(Window window, BackendInitOptions options) {
        RenderBackend backend = switch (options.rendererType()) {
            case OPENGL -> new OpenGlBackend();
            case BGFX_OPENGL, BGFX_VULKAN, BGFX_DIRECT3D11 -> new BgfxRenderBackend(options.rendererType());
        };
        backend.initialize(window, options);
        MaterialShaderTarget.setActiveRenderer(backend.rendererType());
        return backend;
    }
}
