package org.llw.studio.editor.animation;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import org.llw.studio.animation.AnimationClip;

/**
 * Horizontal time ruler with draggable playhead.
 */
public final class AnimationTimeRuler {
    private static final float PIXELS_PER_SECOND = 100f;

    private AnimationTimeRuler() {
    }

    public static float pixelsPerSecond() {
        return PIXELS_PER_SECOND;
    }

    public static void render(AnimationEditorState state, float width) {
        AnimationClip clip = state.clip();
        if (clip == null) {
            return;
        }
        float length = Math.max(0.01f, clip.length);
        float contentWidth = length * PIXELS_PER_SECOND + 40f;
        ImGui.beginChild("anim_ruler", width, 28f, true);
        ImGui.getWindowDrawList().addText(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(), 0xFFAAAAAA, "Time");
        float originX = ImGui.getCursorScreenPosX() + 48f;
        float y = ImGui.getCursorScreenPosY() + 14f;
        for (float t = 0f; t <= length + 0.001f; t += 0.1f) {
            float x = originX + t * PIXELS_PER_SECOND;
            ImGui.getWindowDrawList().addLine(x, y - 4f, x, y + 4f, 0xFF666666, 1f);
            if (Math.abs(t - Math.round(t * 10f) / 10f) < 0.001f) {
                ImGui.getWindowDrawList().addText(x - 8f, y + 6f, 0xFF888888, String.format("%.1f", t));
            }
        }
        float playheadX = originX + state.currentTime() * PIXELS_PER_SECOND;
        ImGui.getWindowDrawList().addLine(playheadX, y - 10f, playheadX, y + 14f, 0xFF4FC3F7, 2f);
        ImGui.invisibleButton("ruler_scrub", contentWidth, 24f);
        if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            scrubFromMouse(state, originX, length);
        }
        if (ImGui.isItemActive() && ImGui.isMouseDragging(ImGuiMouseButton.Left)) {
            scrubFromMouse(state, originX, length);
        }
        ImGui.endChild();
    }

    private static void scrubFromMouse(AnimationEditorState state, float originX, float length) {
        float mouseX = ImGui.getMousePosX();
        float t = (mouseX - originX) / PIXELS_PER_SECOND;
        state.setCurrentTime(Math.max(0f, Math.min(length, t)));
        state.setPlaying(false);
    }
}
