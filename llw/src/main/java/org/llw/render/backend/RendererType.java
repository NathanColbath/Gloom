package org.llw.render.backend;

/**
 * Selects which GPU backend and underlying API to use.
 */
public enum RendererType {
    OPENGL,
    BGFX_OPENGL,
    BGFX_VULKAN,
    BGFX_DIRECT3D11;

    public String displayName() {
        return switch (this) {
            case OPENGL -> "OpenGL 3.3";
            case BGFX_OPENGL -> "bgfx — OpenGL";
            case BGFX_VULKAN -> "bgfx — Vulkan";
            case BGFX_DIRECT3D11 -> "bgfx — Direct3D 11";
        };
    }

    public String settingId() {
        return name();
    }

    public static RendererType fromSettingId(String id) {
        if (id == null || id.isBlank()) {
            return OPENGL;
        }
        String normalized = id.trim();
        return switch (normalized.toLowerCase()) {
            case "opengl", "open_gl" -> OPENGL;
            case "bgfx-opengl", "bgfx_opengl", "bgfx" -> BGFX_OPENGL;
            case "bgfx-vulkan", "bgfx_vulkan", "vulkan" -> BGFX_VULKAN;
            case "bgfx-d3d11", "bgfx_d3d11", "d3d11", "direct3d11" -> BGFX_DIRECT3D11;
            default -> {
                try {
                    yield valueOf(normalized.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    yield OPENGL;
                }
            }
        };
    }

    public static RendererType fromEnvAlias(String env) {
        return fromSettingId(env == null ? "" : env.replace('-', '_'));
    }

    public boolean usesBgfx() {
        return this != OPENGL;
    }
}
