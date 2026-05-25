package org.llw.studio;

import org.llw.audio.AudioContext;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;
import org.llw.resources.ResourceManager;
import org.llw.studio.editor.imgui.ImGuiContext;
import org.llw.studio.editor.launcher.ProjectLauncherScreen;
import org.llw.studio.editor.theme.GloomTheme;
import org.llw.studio.memory.StudioMemory;
import org.llw.util.log.Log;
import org.llw.util.log.LogConfig;
import org.llw.util.log.Loggers;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application entry point: creates the GLFW window, OpenGL backend, ImGui shell, and either
 * the project launcher or a directly loaded project.
 */
public final class StudioLauncher {
    /**
     * Runs the main editor loop until the window closes.
     *
     * @param args optional project path; when omitted, shows the project launcher first
     */
    public static void main(String[] args) throws Exception {
        Path bootstrapRoot = Path.of(System.getProperty("user.home"), ".llw-studio");
        Files.createDirectories(bootstrapRoot);

        Log.init(LogConfig.builder()
                .logDir(bootstrapRoot.resolve("logs"))
                .minLevel(org.llw.util.log.LogLevel.DEBUG)
                .build());
        Log.installUncaughtExceptionHandler();

        Window window = new Window(new WindowSettings().title("LLW Studio").size(1600, 900));
        OpenGlBackend backend = new OpenGlBackend();
        backend.initialize(window);
        AudioContext audio = new AudioContext();
        ResourceManager resources = new ResourceManager(backend, audio);

        ImGuiContext imgui = new ImGuiContext(window);
        GloomTheme.applyModernGrayTheme();

        StudioEditorRuntime runtime = new StudioEditorRuntime(window, backend, resources, imgui, bootstrapRoot);
        Path sampleProject = Paths.get("studio-project").toAbsolutePath().normalize();
        ProjectLauncherScreen launcher = new ProjectLauncherScreen(runtime, message -> {}, sampleProject);

        boolean showLauncher = args.length == 0;
        if (!showLauncher) {
            runtime.loadProject(Paths.get(args[0]));
        }

        var log = Log.get(Loggers.WINDOW);
        long lastTime = System.nanoTime();
        while (window.isOpen()) {
            long now = System.nanoTime();
            float delta = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            window.pollEvents();
            GL11.glViewport(0, 0, window.settings().width(), window.settings().height());
            backend.beginFrame(window.size());
            backend.setClearColor(org.llw.render.core.Color.BLACK);
            backend.clear();

            imgui.beginFrame(window);
            // Launcher and editor share one GLFW loop; only one path draws UI per frame.
            if (showLauncher && !runtime.isProjectLoaded()) {
                runtime.pollAsyncUi();
                launcher.render();
                window.takeDroppedPaths();
            } else {
                runtime.renderEditor(delta);
            }
            imgui.endFrame(window);

            GLFW.glfwSwapBuffers(window.handle());
            StudioMemory.endFrame();
        }

        runtime.dispose();
        audio.dispose();
        backend.dispose();
        window.destroy();
        log.info("LLW Studio shut down");
    }
}
