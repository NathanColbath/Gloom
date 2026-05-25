package org.llw.player;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.backend.RenderBackend;
import org.llw.render.bgfx.BgfxRenderBackend;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.render.window.Window;
import org.llw.resources.ResourceManager;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.playmode.PlayModeRunner;
import org.llw.studio.materials.runtime.MaterialProgramCache;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.render.PlaySceneRenderPasses;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.PlayCameraBridge;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.ui.UiLayoutContext;
import org.lwjgl.opengl.GL30;

/**
 * Standalone game loop for published player builds.
 */
public final class PlayerGameLoop implements AutoCloseable {
    private final Window window;
    private final RenderBackend backend;
    private final ResourceManager resources;
    private final PublishedContent content;
    private final PlayModeRunner playMode = new PlayModeRunner();
    private final ShaderGraphProgramCache shaderGraphs;
    private final MaterialProgramCache materials;
    private OffscreenTarget target;
    private Scene playScene;
    private final Camera2d blitCamera = new Camera2d();
    private final Matrix3x2 blitIdentity = new Matrix3x2().identity();

    public PlayerGameLoop(Window window, RenderBackend backend, ResourceManager resources, PublishedContent content) {
        this.window = window;
        this.backend = backend;
        this.resources = resources;
        this.content = content;
        this.shaderGraphs = new ShaderGraphProgramCache(backend.shaderLibrary(), content.assets());
        this.materials = new MaterialProgramCache(content.assets(), backend.shaderLibrary(), shaderGraphs);
        GameManifest.WindowSettings settings = content.manifest().window();
        int width = Math.max(320, settings.width());
        int height = Math.max(240, settings.height());
        this.target = new OffscreenTarget(backend, new IntSize(width, height));
    }

    public void start() throws Exception {
        Scene startupScene = content.loadStartupScene();
        playMode.setWindowHandle(window.handle());
        StudioLogSink log = (level, message) -> System.out.println("[" + level + "] " + message);
        var prepared = playMode.prepareScene(
                startupScene,
                content.contentDir(),
                content.assets(),
                log
        );
        playScene = playMode.activate(prepared, content.assets(), resources);
        PlayCameraBridge.setViewportScreenRect(0f, 0f);
        PlayUiInputBridge.setViewportSize(target.getSize().width(), target.getSize().height());
    }

    public void frame(float deltaTime) {
        if (playScene == null) {
            return;
        }
        IntSize windowSize = window.size();
        int width = Math.max(1, windowSize.width());
        int height = Math.max(1, windowSize.height());
        if (width != target.getSize().width() || height != target.getSize().height()) {
            target.dispose();
            target = new OffscreenTarget(backend, new IntSize(width, height));
            PlayUiInputBridge.setViewportSize(width, height);
        }
        PlayUiInputBridge.setGameViewFocused(true);
        PlayCameraBridge.syncScene(playScene, width, height);
        playMode.update(playScene, deltaTime, true);
        PlayCameraBridge.applyTo(target.getCamera());
        target.clear(new Color(
                Math.round(PlayCameraBridge.backgroundR() * 255f),
                Math.round(PlayCameraBridge.backgroundG() * 255f),
                Math.round(PlayCameraBridge.backgroundB() * 255f),
                Math.round(PlayCameraBridge.backgroundA() * 255f)
        ));
        PlaySceneRenderPasses.draw(
                playScene,
                target,
                content.assets(),
                materials,
                shaderGraphs,
                backend,
                playMode.particleWorld(),
                content.assets().uiFontCache(),
                UiLayoutContext.forPlay()
        );
        blitToWindow(target.colorTexture(), width, height);
    }

    private void blitToWindow(Texture2d texture, int width, int height) {
        IntSize viewport = new IntSize(width, height);
        if (backend instanceof BgfxRenderBackend bgfx && bgfx.isBgfxInitialized()) {
            bgfx.presentTexture(texture, viewport);
            return;
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        backend.beginFrame(viewport);
        blitCamera.setCenter(width * 0.5f, height * 0.5f);
        blitCamera.setSize(width, height);
        backend.setViewProjection(blitCamera.getViewProjection(viewport));
        backend.setClearColor(Color.BLACK);
        backend.clear();
        backend.drawTexturedQuad(
                blitIdentity,
                0f,
                0f,
                width,
                height,
                0f,
                1f,
                1f,
                0f,
                Color.WHITE,
                texture,
                null,
                BlendMode.ALPHA
        );
        backend.flushSprites();
        backend.endFrame();
    }

    @Override
    public void close() {
        playMode.stop();
        target.dispose();
        shaderGraphs.invalidateAll();
    }
}
