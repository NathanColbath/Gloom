package org.llw.studio.editor.shell;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import org.llw.studio.editor.StudioContext;
import org.lwjgl.glfw.GLFW;

/**
 * Global editor keyboard shortcuts (undo/redo, etc.).
 */
public final class EditorShortcuts {
    private EditorShortcuts() {
    }

    /**
     * Handles edit-mode shortcuts for the current frame.
     *
     * <p>Call after UI for the frame is built. Skips play mode and active text input only
     * ({@link ImGuiIO#getWantCaptureKeyboard()} is too broad for docked panels).
     */
    public static void handleUndoRedo(StudioContext context, EditorMenuActions actions) {
        if (context == null || context.isPlaying() || context.isPlayPreparing()) {
            return;
        }
        ImGuiIO io = ImGui.getIO();
        if (io.getWantTextInput()) {
            return;
        }
        boolean ctrl = io.getKeyCtrl();
        if (!ctrl) {
            return;
        }
        boolean shift = io.getKeyShift();
        if (pressedZ()) {
            if (shift) {
                if (actions.canRedo()) {
                    actions.redo();
                }
            } else if (actions.canUndo()) {
                actions.undo();
            }
            return;
        }
        if (!shift && pressedY() && actions.canRedo()) {
            actions.redo();
        }
    }

    private static boolean pressedZ() {
        return ImGui.isKeyPressed(ImGuiKey.Z) || ImGui.isKeyPressed(GLFW.GLFW_KEY_Z);
    }

    private static boolean pressedY() {
        return ImGui.isKeyPressed(ImGuiKey.Y) || ImGui.isKeyPressed(GLFW.GLFW_KEY_Y);
    }
}
