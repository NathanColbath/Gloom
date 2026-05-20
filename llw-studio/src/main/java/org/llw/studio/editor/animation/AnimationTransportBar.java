package org.llw.studio.editor.animation;

import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.llw.studio.animation.AnimationClip;

/**
 * Play / pause / stop controls and time readout for the animation panel.
 */
public final class AnimationTransportBar {
    private static final float FIELD_WIDTH = 56f;

    private AnimationTransportBar() {
    }

    public static void render(AnimationEditorState state) {
        AnimationClip clip = state.clip();
        boolean previewPanel = state.previewInPanel();
        if (ImGui.checkbox("Preview##panel", previewPanel)) {
            state.setPreviewInPanel(!previewPanel);
        }
        ImGui.sameLine(0f, 12f);
        boolean previewScene = state.previewInScene();
        if (ImGui.checkbox("Scene##preview", previewScene)) {
            state.setPreviewInScene(!previewScene);
        }
        ImGui.sameLine(0f, 16f);
        if (ImGui.button(state.playing() ? "Pause" : "Play")) {
            state.setPlaying(!state.playing());
        }
        ImGui.sameLine(0f, 4f);
        if (ImGui.button("Stop")) {
            state.setPlaying(false);
            state.setCurrentTime(0f);
        }
        ImGui.sameLine(0f, 12f);
        boolean loop = state.loop();
        if (ImGui.checkbox("Loop", loop)) {
            state.setLoop(!loop);
        }
        ImGui.sameLine(0f, 16f);
        ImGui.alignTextToFramePadding();
        ImGui.text("Speed");
        ImGui.sameLine(0f, 4f);
        float[] speed = {state.speed()};
        ImGui.setNextItemWidth(FIELD_WIDTH);
        if (ImGui.dragFloat("##speed", speed, 0.01f, 0.01f, 8f, "%.2f")) {
            state.setSpeed(speed[0]);
        }
        if (clip != null) {
            ImGui.sameLine(0f, 20f);
            int frame = Math.round(state.currentTime() * clip.frameRate);
            int total = Math.max(1, Math.round(clip.length * clip.frameRate));
            ImGui.alignTextToFramePadding();
            ImGui.text("Frame");
            ImGui.sameLine(0f, 4f);
            ImGui.text(frame + " / " + total);
            ImGui.sameLine(0f, 16f);
            ImGui.text(String.format("%.2fs / %.2fs", state.currentTime(), clip.length));
        }
    }

    public static void renderClipSettings(AnimationClip clip, java.util.function.Consumer<Runnable> withUndo) {
        if (clip == null || withUndo == null) {
            return;
        }
        ImGui.alignTextToFramePadding();
        ImGui.text("Samples");
        ImGui.sameLine(0f, 4f);
        ImInt samples = new ImInt(Math.max(1, Math.round(clip.frameRate)));
        ImGui.setNextItemWidth(FIELD_WIDTH);
        if (ImGui.inputInt("##samples", samples, 1, 120)) {
            int frameRate = Math.max(1, Math.min(120, samples.get()));
            if (frameRate != Math.round(clip.frameRate)) {
                withUndo.accept(() -> clip.frameRate = frameRate);
            }
        }
        ImGui.sameLine(0f, 20f);

        ImGui.text("Length");
        ImGui.sameLine(0f, 4f);
        ImFloat length = new ImFloat(clip.length);
        ImGui.setNextItemWidth(FIELD_WIDTH + 12f);
        if (ImGui.inputFloat("##length", length, 0f, 0f, "%.2f")) {
            float clipLength = Math.max(0.01f, Math.min(600f, length.get()));
            if (Math.abs(clipLength - clip.length) > 0.0001f) {
                withUndo.accept(() -> clip.length = clipLength);
            }
        }
    }
}
