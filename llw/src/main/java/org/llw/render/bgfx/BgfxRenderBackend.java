package org.llw.render.bgfx;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.backend.BackendInitOptions;
import org.llw.render.backend.RenderBackend;
import org.llw.render.backend.RendererType;
import org.llw.render.bgfx.shaders.BgfxBuiltinShaders;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.Vertex;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.gl.ShaderLibrary;
import org.llw.render.window.Window;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.bgfx.BGFX;
import org.lwjgl.bgfx.BGFXInit;
import org.lwjgl.bgfx.BGFXPlatformData;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import java.util.function.Consumer;

/**
 * bgfx-backed {@link RenderBackend}: scene draws use OpenGL (offscreen-compatible); presentation
 * uses bgfx for cross-API swap chains (Vulkan, D3D11, OpenGL).
 *
 * <p>Studio keeps a dedicated {@link OpenGlBackend} for Dear ImGui ({@code ImGuiImplGl3}).
 */
public final class BgfxRenderBackend implements RenderBackend {
    private static final Logger log = Log.get(Loggers.GL);

    private final RendererType rendererType;
    private final OpenGlBackend openGl = new OpenGlBackend();
    private final BgfxSpriteRenderer sprites = new BgfxSpriteRenderer();
    private final ShaderLibrary shaderLibrary = new ShaderLibrary();
    private boolean bgfxInitialized;
    private IntSize lastViewport = new IntSize(1, 1);

    public BgfxRenderBackend(RendererType rendererType) {
        this.rendererType = rendererType;
    }

    @Override
    public void initialize(Window window, BackendInitOptions options) {
        openGl.initialize(window, options);
        shaderLibrary.loadDefaults();
        initBgfx(window, options);
        sprites.initialize(shaderLibrary);
        log.info("BgfxRenderBackend initialized type={} bgfxRenderer={}", rendererType, bgfxRendererName());
    }

    @Override
    public ShaderLibrary shaderLibrary() {
        return openGl.shaderLibrary();
    }

    @Override
    public RendererType rendererType() {
        return rendererType;
    }

    @Override
    public void setViewProjection(Matrix3x2 matrix) {
        openGl.setViewProjection(matrix);
    }

    @Override
    public void setClearColor(Color color) {
        openGl.setClearColor(color);
    }

    @Override
    public void clear() {
        openGl.clear();
    }

    @Override
    public void beginFrame(IntSize viewport) {
        lastViewport = viewport;
        openGl.beginFrame(viewport);
        if (bgfxInitialized) {
            BGFX.bgfx_reset(
                    viewport.width(),
                    viewport.height(),
                    BGFX.BGFX_RESET_VSYNC,
                    BGFX.BGFX_RESET_NONE
            );
        }
    }

    @Override
    public float shaderTimeSeconds() {
        return openGl.shaderTimeSeconds();
    }

    @Override
    public void endFrame() {
        openGl.endFrame();
    }

    /** @return whether {@code bgfx_init} succeeded and presentation uses bgfx */
    public boolean isBgfxInitialized() {
        return bgfxInitialized;
    }

    @Override
    public void drawTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        if (bgfxInitialized && useBgfxPath(shader)) {
            sprites.drawTexturedQuad(
                    model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture, shader, blendMode, lastViewport
            );
        } else {
            openGl.drawTexturedQuad(model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture, shader, blendMode);
        }
    }

    @Override
    public void drawLitTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        openGl.drawLitTexturedQuad(model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture, shader, blendMode);
    }

    @Override
    public void drawLitTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode,
            long uniformBatchKey,
            Consumer<ShaderProgram> uniformHook
    ) {
        openGl.drawLitTexturedQuad(
                model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture, shader, blendMode, uniformBatchKey, uniformHook
        );
    }

    @Override
    public void flushSprites() {
        if (sprites.hasPending()) {
            sprites.flush(lastViewport);
        }
        openGl.flushSprites();
    }

    @Override
    public void flushLitSprites(Consumer<ShaderProgram> uniformHook) {
        openGl.flushLitSprites(uniformHook);
    }

    @Override
    public void drawVertices(
            Matrix3x2 model,
            Vertex[] vertices,
            PrimitiveType primitiveType,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        openGl.drawVertices(model, vertices, primitiveType, shader, blendMode);
    }

    @Override
    public void drawText(
            Matrix3x2 model,
            Font font,
            String text,
            float x,
            float y,
            Color color,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        openGl.drawText(model, font, text, x, y, color, shader, blendMode);
    }

    @Override
    public Matrix3x2 combineMvp(Matrix3x2 model) {
        return openGl.combineMvp(model);
    }

    /**
     * Presents an OpenGL color texture to the window through bgfx (player blit path).
     */
    public void presentTexture(Texture2d texture, IntSize viewport) {
        BgfxPresentPass.present(openGl, texture, viewport, bgfxInitialized);
    }

    /** OpenGL backend used for offscreen scene rendering and ImGui interop. */
    public OpenGlBackend openGlBackend() {
        return openGl;
    }

    @Override
    public void dispose() {
        sprites.dispose();
        if (bgfxInitialized) {
            BGFX.bgfx_shutdown();
            bgfxInitialized = false;
        }
        openGl.dispose();
        shaderLibrary.dispose();
    }

    private boolean useBgfxPath(ShaderProgram shader) {
        return false;
    }

    private void initBgfx(Window window, BackendInitOptions options) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            BGFXInit init = BGFXInit.calloc(stack);
            init.type(mapRenderer(rendererType));
            init.resolution().width(window.settings().width());
            init.resolution().height(window.settings().height());
            init.resolution().reset(BGFX.BGFX_RESET_VSYNC);
            BGFXPlatformData pd = BGFXPlatformData.calloc(stack);
            configurePlatformData(pd, window);
            init.platformData(pd);
            if (!BGFX.bgfx_init(init)) {
                log.warn("bgfx_init failed; falling back to OpenGL-only presentation");
                return;
            }
            bgfxInitialized = true;
            BgfxBuiltinShaders.load();
        }
    }

    private static void configurePlatformData(BGFXPlatformData pd, Window window) {
        long handle = window.handle();
        switch (Platform.get()) {
            case WINDOWS -> pd.nwh(GLFWNativeWin32.glfwGetWin32Window(handle));
            case MACOSX -> pd.nwh(GLFWNativeCocoa.glfwGetCocoaWindow(handle));
            case LINUX -> {
                pd.ndt(GLFWNativeX11.glfwGetX11Display());
                pd.nwh(GLFWNativeX11.glfwGetX11Window(handle));
            }
            default -> pd.nwh(handle);
        }
    }

    private static int mapRenderer(RendererType type) {
        return switch (type) {
            case BGFX_VULKAN -> BGFX.BGFX_RENDERER_TYPE_VULKAN;
            case BGFX_DIRECT3D11 -> BGFX.BGFX_RENDERER_TYPE_DIRECT3D11;
            case BGFX_OPENGL, OPENGL -> BGFX.BGFX_RENDERER_TYPE_OPENGL;
        };
    }

    private String bgfxRendererName() {
        if (!bgfxInitialized) {
            return "none";
        }
        return switch (BGFX.bgfx_get_renderer_type()) {
            case BGFX.BGFX_RENDERER_TYPE_VULKAN -> "Vulkan";
            case BGFX.BGFX_RENDERER_TYPE_DIRECT3D11 -> "Direct3D11";
            case BGFX.BGFX_RENDERER_TYPE_OPENGL -> "OpenGL";
            default -> "other";
        };
    }
}
