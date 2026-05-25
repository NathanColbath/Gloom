package org.llw.render.gl;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.Vertex;
import org.llw.render.backend.BackendInitOptions;
import org.llw.render.backend.RenderBackend;
import org.llw.render.backend.RendererType;
import org.llw.render.window.Window;
import org.llw.util.log.EnvironmentLog;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

/**
 * Central OpenGL rendering facade for 2D batched sprites, immediate shapes, and text.
 *
 * <p>Call {@link #initialize(Window)} once per GLFW context, then drive frames with
 * {@link #beginFrame(IntSize)}, draw calls, and {@link #endFrame()}. The view-projection
 * matrix set via {@link #setViewProjection(Matrix3x2)} is combined with per-draw model
 * matrices before geometry reaches the GPU.
 */
public final class OpenGlBackend implements RenderBackend {
    private static final Logger log = Log.get(Loggers.GL);

    private final ShaderLibrary shaderLibrary = new ShaderLibrary();
    private final GlStateTracker stateTracker = new GlStateTracker();
    private SpriteBatch spriteBatch;
    private LitSpriteBatch litSpriteBatch;
    private ShapeRenderer shapeRenderer;
    private TextRenderer textRenderer;
    private final Matrix3x2 viewProjection = new Matrix3x2().identity();
    private final Matrix3x2 scratchMvp = new Matrix3x2();
    private Color clearColor = Color.BLACK;
    private Texture2d lastBatchTexture;
    private Texture2d lastLitBatchTexture;
    private long currentLitUniformKey = LitUniformKeyState.INVALID;
    private Consumer<ShaderProgram> pendingLitUniformHook;
    private float shaderTimeSeconds;
    private boolean initialized;

    private static final class LitUniformKeyState {
        static final long INVALID = -1L;
    }

    /**
     * Makes the GLFW context current, configures vsync, creates OpenGL capabilities, loads
     * default shaders, and allocates internal renderers.
     *
     * @param window GLFW window whose context is activated; must not be {@code null}
     */
    @Override
    public void initialize(Window window, BackendInitOptions options) {
        initialize(window);
    }

    /**
     * Makes the GLFW context current, configures vsync, creates OpenGL capabilities, loads
     * default shaders, and allocates internal renderers.
     */
    public void initialize(Window window) {
        GLFW.glfwMakeContextCurrent(window.handle());
        if (window.settings().vsync()) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        shaderLibrary.loadDefaults();
        spriteBatch = new SpriteBatch();
        litSpriteBatch = new LitSpriteBatch();
        shapeRenderer = new ShapeRenderer();
        textRenderer = new TextRenderer(this);
        initialized = true;
        log.info("OpenGL backend initialized vsync={}", window.settings().vsync());
        if (log.isDebugEnabled()) {
            EnvironmentLog.logOpenGl(
                    GL11.glGetString(GL11.GL_VENDOR),
                    GL11.glGetString(GL11.GL_RENDERER),
                    GL11.glGetString(GL11.GL_VERSION),
                    GL20.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION),
                    GLFW.glfwGetVersionString()
            );
        }
    }

    /**
     * Returns the shader registry owned by this backend.
     *
     * @return library containing default sprite, shape, and text programs
     */
    @Override
    public ShaderLibrary shaderLibrary() {
        return shaderLibrary;
    }

    @Override
    public RendererType rendererType() {
        return RendererType.OPENGL;
    }

    /**
     * Replaces the view-projection matrix applied to all subsequent draws until changed again.
     *
     * @param matrix new view-projection matrix; must not be {@code null}
     */
    public void setViewProjection(Matrix3x2 matrix) {
        viewProjection.set(matrix);
    }

    /**
     * Sets the color used by {@link #clear()}.
     *
     * @param color clear color in RGBA byte components; must not be {@code null}
     */
    public void setClearColor(Color color) {
        clearColor = color;
    }

    /**
     * Clears the color buffer to the color previously set with {@link #setClearColor(Color)}.
     */
    public void clear() {
        GL11.glClearColor(clearColor.rNorm(), clearColor.gNorm(), clearColor.bNorm(), clearColor.aNorm());
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Begins a frame by setting the viewport and resetting internal GL state tracking.
     *
     * @param viewport target dimensions in pixels
     */
    public void beginFrame(IntSize viewport) {
        GL11.glViewport(0, 0, viewport.width(), viewport.height());
        stateTracker.reset();
        shaderTimeSeconds = (float) GLFW.glfwGetTime();
    }

    /**
     * @return elapsed seconds from GLFW, updated each {@link #beginFrame(IntSize)}
     */
    public float shaderTimeSeconds() {
        return shaderTimeSeconds;
    }

    /**
     * Ends a frame by flushing any active sprite batch and clearing batch texture state.
     */
    public void endFrame() {
        flushLitSprites(pendingLitUniformHook);
        pendingLitUniformHook = null;
        currentLitUniformKey = LitUniformKeyState.INVALID;
        flushSprites();
        lastBatchTexture = null;
        lastLitBatchTexture = null;
    }

    /**
     * Enqueues a textured axis-aligned quad for batched sprite rendering.
     *
     * <p>The batch is started or restarted when the shader, blend mode, or texture changes.
     * Call {@link #flushSprites()} to submit pending quads before non-sprite draws.
     *
     * @param model      local model matrix; may be {@code null} for identity
     * @param x0         local X of the first corner
     * @param y0         local Y of the first corner
     * @param x1         local X of the opposite corner
     * @param y1         local Y of the opposite corner
     * @param u0         normalized U at {@code (x0, y0)}
     * @param v0         normalized V at {@code (x0, y0)}
     * @param u1         normalized U at {@code (x1, y1)}
     * @param v1         normalized V at {@code (x1, y1)}
     * @param color      per-vertex tint in RGBA byte components
     * @param texture    texture bound for sampling; may be {@code null}
     * @param shader     shader program; {@code null} selects the default sprite shader
     * @param blendMode  blending mode for the active batch
     */
    public void drawTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        ShaderProgram activeShader = shader != null ? shader : shaderLibrary.spriteShader();
        ensureSpriteBatch(activeShader, blendMode, texture);
        spriteBatch.drawQuad(model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture);
    }

    /**
     * Submits any quads accumulated in the active sprite batch to the GPU.
     */
    public void flushSprites() {
        flushLitSprites(null);
        if (spriteBatch != null && spriteBatch.isActive()) {
            spriteBatch.setShaderTimeSeconds(shaderTimeSeconds);
            spriteBatch.flush(stateTracker);
        }
    }

    /**
     * Enqueues a quad for the lit sprite batch (per-vertex world position).
     */
    public void drawLitTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        drawLitTexturedQuad(
                model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture, shader, blendMode,
                LitUniformKeyState.INVALID, null
        );
    }

    /**
     * Enqueues a lit quad; flushes the lit batch when {@code uniformBatchKey} changes.
     */
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
        if (uniformBatchKey != LitUniformKeyState.INVALID
                && litSpriteBatch.isActive()
                && uniformBatchKey != currentLitUniformKey) {
            flushLitSprites(pendingLitUniformHook);
        }
        pendingLitUniformHook = uniformHook;
        if (uniformBatchKey != LitUniformKeyState.INVALID) {
            currentLitUniformKey = uniformBatchKey;
        }
        ShaderProgram activeShader = shader != null ? shader : shaderLibrary.litSpriteShader();
        ensureLitSpriteBatch(activeShader, blendMode, texture);
        litSpriteBatch.drawQuad(model, x0, y0, x1, y1, u0, v0, u1, v1, color, texture);
    }

    /**
     * Flushes the lit sprite batch, optionally applying per-frame lighting uniforms.
     */
    public void flushLitSprites(Consumer<ShaderProgram> uniformHook) {
        if (litSpriteBatch != null && litSpriteBatch.isActive()) {
            litSpriteBatch.flush(stateTracker, uniformHook);
        }
    }

    /**
     * Draws untextured vertices immediately, flushing any active sprite batch first.
     *
     * @param model          local model matrix combined with the current view-projection
     * @param vertices       vertex attributes to upload for this draw
     * @param primitiveType  OpenGL primitive topology
     * @param shader         shader program; {@code null} selects the default shape shader
     * @param blendMode      blending mode for this draw
     */
    public void drawVertices(
            Matrix3x2 model,
            Vertex[] vertices,
            PrimitiveType primitiveType,
            ShaderProgram shader,
            BlendMode blendMode
    ) {
        flushSprites();
        ShaderProgram activeShader = shader != null ? shader : shaderLibrary.shapeShader();
        shapeRenderer.draw(stateTracker, activeShader, blendMode, combineMvp(model), vertices, primitiveType);
    }

    /**
     * Lays out and draws a string as batched glyph quads, flushing any active sprite batch first.
     *
     * @param model      local model matrix applied to each glyph quad
     * @param font       bitmap font providing glyphs and atlas texture
     * @param text       string to draw
     * @param x          starting X in local space
     * @param y          starting Y in local space
     * @param color      glyph tint in RGBA byte components
     * @param shader     shader program; {@code null} selects the default text shader
     * @param blendMode  blending mode for glyph quads
     */
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
        flushSprites();
        ShaderProgram activeShader = shader != null ? shader : shaderLibrary.textShader();
        textRenderer.draw(stateTracker, activeShader, blendMode, model, font, text, x, y, color);
    }

    /**
     * Multiplies the current view-projection matrix by {@code model}.
     *
     * @param model optional local model matrix; ignored when {@code null}
     * @return a new matrix containing view-projection × model
     */
    public Matrix3x2 combineMvp(Matrix3x2 model) {
        scratchMvp.set(viewProjection);
        if (model != null) {
            scratchMvp.multiply(model);
        }
        return scratchMvp;
    }

    /**
     * Releases GPU resources owned by this backend if {@link #initialize(Window)} completed.
     *
     * <p>Safe to call when not initialized; subsequent draw calls require initialization again.
     */
    public void dispose() {
        if (!initialized) {
            return;
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        shapeRenderer.dispose();
        shaderLibrary.dispose();
        initialized = false;
        log.info("OpenGL backend disposed");
    }

    private void ensureLitSpriteBatch(ShaderProgram shader, BlendMode blendMode, Texture2d texture) {
        boolean textureChanged = texture != null
                && lastLitBatchTexture != null
                && lastLitBatchTexture.id() != texture.id();
        boolean needsRestart = !litSpriteBatch.isActive()
                || litSpriteBatch.currentShader() != shader
                || litSpriteBatch.currentBlend() != blendMode
                || textureChanged;

        if (needsRestart) {
            if (litSpriteBatch.isActive()) {
                flushLitSprites(pendingLitUniformHook);
            } else {
                flushSprites();
            }
            litSpriteBatch.begin(shader, blendMode, viewProjection);
            lastLitBatchTexture = texture;
        }
    }

    private void ensureSpriteBatch(ShaderProgram shader, BlendMode blendMode, Texture2d texture) {
        flushLitSprites(pendingLitUniformHook);
        pendingLitUniformHook = null;
        currentLitUniformKey = LitUniformKeyState.INVALID;
        boolean textureChanged = texture != null
                && lastBatchTexture != null
                && lastBatchTexture.id() != texture.id();
        boolean needsRestart = !spriteBatch.isActive()
                || spriteBatch.currentShader() != shader
                || spriteBatch.currentBlend() != blendMode
                || textureChanged;

        if (needsRestart) {
            flushSprites();
            spriteBatch.begin(shader, blendMode, viewProjection);
            lastBatchTexture = texture;
        }
    }
}
