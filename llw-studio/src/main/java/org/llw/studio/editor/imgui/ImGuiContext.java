package org.llw.studio.editor.imgui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.llw.render.window.Window;
import org.llw.studio.editor.theme.EditorFonts;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dear ImGui GLFW/OpenGL3 backend and global ini path for the studio editor.
 */
public final class ImGuiContext {
    private final ImGuiImplGlfw implGlfw;
    private final ImGuiImplGl3 implGl3;
    private final Path iniPath;

    /**
     * Creates the ImGui context, loads editor fonts, and initializes GLFW/GL3 backends.
     *
     * @param window native GLFW window
     */
    public ImGuiContext(Window window) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        iniPath = globalIniPath();
        try {
            Files.createDirectories(iniPath.getParent());
        } catch (Exception ignored) {
        }
        io.setIniFilename(iniPath.toString());

        EditorFonts.load();

        implGlfw = new ImGuiImplGlfw();
        implGl3 = new ImGuiImplGl3();
        implGlfw.init(window.handle(), true);
        implGl3.init();
    }

    /**
     * @return default path for persisted window layout ({@code ~/.llw-studio/imgui.ini})
     */
    public static Path globalIniPath() {
        return Path.of(System.getProperty("user.home"), ".llw-studio", "imgui.ini");
    }

    /** @return path passed to {@link ImGuiIO#setIniFilename(String)} */
    public Path iniPath() {
        return iniPath;
    }

    /**
     * Starts a new ImGui frame (input + layout).
     *
     * @param window GLFW window (passed to GLFW backend)
     * <p>Implementation note: Call once per application frame before any {@code ImGui.begin} UI.
     */
    public void beginFrame(Window window) {
        implGlfw.newFrame();
        ImGui.newFrame();
    }

    /**
     * Finalizes draw lists and renders ImGui via OpenGL3.
     *
     * @param window GLFW window (unused; reserved for multi-viewport)
     * <p>Implementation note: Call after all panels and shell UI for the frame; pairs with {@link #beginFrame(Window)}.
     */
    public void endFrame(Window window) {
        ImGui.render();
        implGl3.renderDrawData(ImGui.getDrawData());
    }

    /** Disposes backends and destroys the ImGui context. */
    public void dispose() {
        implGl3.dispose();
        implGlfw.dispose();
        ImGui.destroyContext();
    }
}
