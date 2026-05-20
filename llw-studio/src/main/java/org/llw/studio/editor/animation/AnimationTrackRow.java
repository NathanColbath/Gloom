package org.llw.studio.editor.animation;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationTrack;
import org.llw.studio.animation.AnimationTrackType;
import org.llw.studio.animation.FloatKeyframe;
import org.llw.studio.animation.SpriteKeyframe;

/**
 * Single timeline track row with keyframe diamonds.
 */
public final class AnimationTrackRow {
    private AnimationTrackRow() {
    }

    public static void render(
            AnimationEditorState state,
            AnimationClip clip,
            AnimationTrack track,
            String label,
            float width,
            Runnable onChanged
    ) {
        ImGui.textUnformatted(label);
        ImGui.sameLine(120f);
        float length = Math.max(0.01f, clip.length);
        float contentWidth = length * AnimationTimeRuler.pixelsPerSecond() + 40f;
        ImGui.beginChild("track_" + label, width - 124f, 22f, true);
        float originX = ImGui.getCursorScreenPosX() + 8f;
        float y = ImGui.getCursorScreenPosY() + 11f;
        if (track.type == AnimationTrackType.SPRITE) {
            for (SpriteKeyframe key : track.spriteKeyframes) {
                drawDiamond(originX + key.time() * AnimationTimeRuler.pixelsPerSecond(), y, key.time() == state.currentTime());
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && nearKey(key.time(), state)) {
                    state.setCurrentTime(key.time());
                }
            }
        } else {
            for (FloatKeyframe key : track.floatKeyframes) {
                drawDiamond(originX + key.time() * AnimationTimeRuler.pixelsPerSecond(), y, false);
            }
        }
        ImGui.invisibleButton("track_lane_" + label, contentWidth, 18f);
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            addKeyAtPlayhead(clip, track, state.currentTime());
            track.sortKeyframes();
            onChanged.run();
        }
        ImGui.endChild();
    }

    private static void drawDiamond(float x, float y, boolean selected) {
        int color = selected ? 0xFF4FC3F7 : 0xFFE0E0E0;
        ImGui.getWindowDrawList().addQuadFilled(x, y - 5f, x + 5f, y, x, y + 5f, x - 5f, y, color);
    }

    private static boolean nearKey(float keyTime, AnimationEditorState state) {
        return Math.abs(keyTime - state.currentTime()) < 0.02f;
    }

    private static void addKeyAtPlayhead(AnimationClip clip, AnimationTrack track, float time) {
        float snapped = clip.snapTime(time);
        if (track.type == AnimationTrackType.SPRITE) {
            String sprite = track.spriteKeyframes.isEmpty() ? "" : track.spriteKeyframes.get(track.spriteKeyframes.size() - 1).spriteGuid();
            track.spriteKeyframes.add(new SpriteKeyframe(snapped, sprite));
        } else {
            float value = track.floatKeyframes.isEmpty() ? 0f : track.floatKeyframes.get(track.floatKeyframes.size() - 1).value();
            track.floatKeyframes.add(new FloatKeyframe(snapped, value));
        }
    }
}
