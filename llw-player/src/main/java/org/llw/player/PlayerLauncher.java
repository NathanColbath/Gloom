package org.llw.player;

import org.llw.audio.AudioContext;
import org.llw.render.backend.BackendInitOptions;
import org.llw.render.backend.RenderBackend;
import org.llw.render.backend.RenderBackendFactory;
import org.llw.render.backend.RendererPreferences;
import org.llw.render.backend.RendererType;
import org.llw.render.bgfx.BgfxRenderBackend;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;
import org.llw.resources.ResourceManager;
import org.llw.studio.scripting.js.GraalScriptRuntime;
import org.llw.util.log.Log;
import org.llw.util.log.LogConfig;
import org.llw.util.log.LogLevel;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for published LLW player builds.
 */
public final class PlayerLauncher {
    public static void main(String[] args) throws Exception {
        Log.init(LogConfig.builder().minLevel(LogLevel.INFO).build());
        Path contentDir = resolveContentDir(args);
        GameManifest manifest = GameManifest.read(contentDir.resolve("game.manifest.json"));
        GameManifest.WindowSettings windowSettings = manifest.window();

        WindowSettings settings = new WindowSettings()
                .title(windowSettings.title())
                .size(windowSettings.width(), windowSettings.height())
                .resizable(true)
                .vsync(windowSettings.vsync());

        Window window = new Window(settings);
        BackendInitOptions backendOptions = resolveBackendOptions(args, windowSettings.vsync());
        RenderBackend backend = RenderBackendFactory.create(window, backendOptions);
        AudioContext audio = new AudioContext();
        ResourceManager resources = new ResourceManager(backend, audio);
        GraalScriptRuntime.warmupSharedEngine();
        try {
            PublishedContent content = PublishedContent.load(contentDir, resources);
            try (PlayerGameLoop loop = new PlayerGameLoop(window, backend, resources, content)) {
                loop.start();
                long lastFrame = System.nanoTime();
                while (window.isOpen()) {
                    window.pollEvents();
                    long now = System.nanoTime();
                    float delta = (now - lastFrame) / 1_000_000_000f;
                    lastFrame = now;
                    loop.frame(delta);
                    if (!usesBgfxPresentation(backend)) {
                        window.swapBuffers();
                    }
                }
            } finally {
                resources.dispose();
            }
        } finally {
            window.destroy();
        }
    }

    private static BackendInitOptions resolveBackendOptions(String[] args, boolean vsync) {
        RendererType type = RendererPreferences.load().rendererType();
        for (int i = 0; i < args.length - 1; i++) {
            if ("--renderer".equals(args[i])) {
                type = RendererType.fromEnvAlias(args[i + 1]);
            }
        }
        String env = System.getenv("GLOOM_RENDERER");
        if (env != null && !env.isBlank()) {
            type = RendererType.fromEnvAlias(env);
        }
        return new BackendInitOptions(type, vsync);
    }

    private static boolean usesBgfxPresentation(RenderBackend backend) {
        return backend instanceof BgfxRenderBackend bgfx
                && bgfx.isBgfxInitialized()
                && backend.rendererType().usesBgfx();
    }

    private static Path resolveContentDir(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--content".equals(args[i])) {
                return Path.of(args[i + 1]).toAbsolutePath().normalize();
            }
        }
        String property = System.getProperty("llw.content.dir");
        if (property != null && !property.isBlank()) {
            return Path.of(property).toAbsolutePath().normalize();
        }
        Path packaged = resolvePackagedContentDir();
        if (packaged != null) {
            return packaged;
        }
        Path sibling = Path.of("content").toAbsolutePath().normalize();
        if (Files.isDirectory(sibling)) {
            return sibling;
        }
        Path parentSibling = Path.of("../content").toAbsolutePath().normalize();
        if (Files.isDirectory(parentSibling)) {
            return parentSibling;
        }
        throw new IllegalStateException("Content directory not found. Pass --content <path> to the player.");
    }

    /**
     * Resolves {@code content/} for jpackage app-images:
     * {@code Builds/Product/Product/app/*.jar} -> {@code Builds/Product/content}.
     */
    private static Path resolvePackagedContentDir() {
        try {
            var location = PlayerLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            Path codeSource = Path.of(location.toURI());
            if (Files.isDirectory(codeSource)) {
                return null;
            }
            Path appDir = codeSource.getParent();
            if (appDir == null || !"app".equals(String.valueOf(appDir.getFileName()))) {
                return null;
            }
            Path productRoot = appDir.getParent();
            if (productRoot == null || productRoot.getParent() == null) {
                return null;
            }
            Path content = productRoot.getParent().resolve("content").normalize();
            return Files.isDirectory(content) ? content.toAbsolutePath() : null;
        } catch (URISyntaxException | SecurityException ignored) {
            return null;
        }
    }
}
