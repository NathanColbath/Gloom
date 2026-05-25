package org.llw.render.backend;

/**
 * Options passed when initializing a {@link RenderBackend}.
 *
 * @param rendererType API selection (OpenGL or bgfx-backed)
 * @param vsync        whether presentation should wait for v-sync
 */
public record BackendInitOptions(RendererType rendererType, boolean vsync) {
    public static BackendInitOptions defaults() {
        return withEnvOverride(RendererPreferences.load().rendererType(), true);
    }

    public static BackendInitOptions fromPreferences(boolean vsync) {
        return new BackendInitOptions(RendererPreferences.load().rendererType(), vsync);
    }

    public static BackendInitOptions withEnvOverride(RendererType base, boolean vsync) {
        String env = System.getenv("GLOOM_RENDERER");
        RendererType type = env != null && !env.isBlank()
                ? RendererType.fromEnvAlias(env)
                : base;
        return new BackendInitOptions(type, vsync);
    }

    public static BackendInitOptions opengl(boolean vsync) {
        return new BackendInitOptions(RendererType.OPENGL, vsync);
    }
}
