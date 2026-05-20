package org.llw.studio.editor.animation;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationTrack;
import org.llw.studio.animation.AnimationTrackPaths;
import org.llw.studio.animation.AnimationTrackType;
import org.llw.studio.animation.SpriteKeyframe;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;

/**
 * Sprite keyframe dopesheet for the animation panel.
 */
public final class AnimationTimelineView {
    private static final float ROW_HEIGHT = 24f;
    private static final float RULER_HEIGHT = 28f;
    private static final int GRID_COLOR = 0xFF2A2A2A;
    private static final int DROP_LINE_COLOR = 0xFF4FC3F7;
    private static final int DROP_DIAMOND_COLOR = 0x994FC3F7;

    private AnimationTimelineView() {
    }

    public static void render(
            AnimationEditorState state,
            AnimationClip clip,
            AssetDatabase assets,
            java.util.function.Consumer<Runnable> withUndo
    ) {
        if (clip == null) {
            return;
        }
        state.clearDropPreview();
        state.setScrollX(0f);
        clip.ensureDefaultTracks();
        float sheetViewportW = Math.max(120f, ImGui.getContentRegionAvailX());
        float availH = ImGui.getContentRegionAvailY();
        float length = Math.max(0.01f, clip.length);
        float fitPps = Math.max(20f, (sheetViewportW - 32f) / length);
        state.setPixelsPerSecond(fitPps);

        renderRulerRow(state, clip, sheetViewportW);
        renderKeyframeToolbar(state, withUndo, clip);

        float toolbarH = state.hasSelectedKeyframe() ? ImGui.getFrameHeightWithSpacing() : 0f;
        float bodyHeight = ROW_HEIGHT + 2f;
        int childFlags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        ImGui.beginChild("tl_sheet", sheetViewportW, bodyHeight, false, childFlags);
        renderSheetColumn(state, clip, sheetViewportW, assets, withUndo);
        if (ImGui.isWindowFocused()) {
            handleDeleteKey(state, clip, withUndo);
        }
        ImGui.endChild();

        ImGui.dummy(0f, Math.max(0f, availH - RULER_HEIGHT - toolbarH - bodyHeight));
    }

    private static void renderKeyframeToolbar(
            AnimationEditorState state,
            java.util.function.Consumer<Runnable> withUndo,
            AnimationClip clip
    ) {
        if (!state.hasSelectedKeyframe()) {
            return;
        }
        ImGui.alignTextToFramePadding();
        ImGui.textDisabled("Keyframe selected");
        ImGui.sameLine(0f, 8f);
        if (ImGui.button("Delete Keyframe")) {
            withUndo.accept(() -> deleteSelectedKeyframe(clip, state));
        }
        ImGui.sameLine(0f, 12f);
        ImGui.textDisabled("(Del)");
    }

    public static void deleteSelectedKeyframe(AnimationClip clip, AnimationEditorState state) {
        if (state.selectedKeyframeIndex() < 0 || state.selectedTrackPath().isBlank()) {
            return;
        }
        AnimationTrack track = clip.findTrack(state.selectedTrackPath());
        if (track == null || track.type != AnimationTrackType.SPRITE) {
            return;
        }
        if (state.selectedKeyframeIndex() < track.spriteKeyframes.size()) {
            track.spriteKeyframes.remove(state.selectedKeyframeIndex());
        }
        state.setSelectedKeyframe("", -1);
    }

    private static void handleDeleteKey(
            AnimationEditorState state,
            AnimationClip clip,
            java.util.function.Consumer<Runnable> withUndo
    ) {
        if (!state.hasSelectedKeyframe()) {
            return;
        }
        if (ImGui.isKeyPressed(imgui.flag.ImGuiKey.Delete) || ImGui.isKeyPressed(imgui.flag.ImGuiKey.Backspace)) {
            withUndo.accept(() -> deleteSelectedKeyframe(clip, state));
        }
    }

    private static void renderRulerRow(AnimationEditorState state, AnimationClip clip, float sheetViewportW) {
        ImGui.beginChild("tl_ruler", sheetViewportW, RULER_HEIGHT, false, ImGuiWindowFlags.NoScrollbar);
        float originX = ImGui.getCursorScreenPosX();
        float topY = ImGui.getCursorScreenPosY();
        float y = topY + RULER_HEIGHT * 0.55f;
        ImGui.getWindowDrawList().addRectFilled(
                originX,
                topY,
                originX + sheetViewportW,
                topY + RULER_HEIGHT,
                0xFF1E1E1E
        );
        float length = Math.max(0.01f, clip.length);
        float majorStep = rulerLabelStep(length, state.pixelsPerSecond(), sheetViewportW);
        float minorStep = majorStep * 0.25f;
        if (minorStep < 0.001f) {
            minorStep = majorStep;
        }
        for (float t = 0f; t <= length + 0.0001f; t += minorStep) {
            float x = originX + AnimationTimelineMath.timeToX(t, state.pixelsPerSecond(), 0f);
            if (x < originX - 1f || x > originX + sheetViewportW + 1f) {
                continue;
            }
            boolean major = isMultipleOf(t, majorStep);
            float tickH = major ? 8f : 4f;
            ImGui.getWindowDrawList().addLine(x, y - tickH, x, y + tickH, major ? 0xFF888888 : 0xFF444444, 1f);
            if (major) {
                ImGui.getWindowDrawList().addText(x + 3f, topY + 2f, 0xFFBBBBBB, formatTimeLabel(t));
            }
        }
        drawDropPreviewOnRuler(state, clip, originX, y);
        float playX = originX + AnimationTimelineMath.timeToX(state.currentTime(), state.pixelsPerSecond(), 0f);
        ImGui.getWindowDrawList().addLine(playX, topY, playX, topY + RULER_HEIGHT, 0xFFFFFFFF, 2f);
        ImGui.setCursorScreenPos(originX, topY);
        ImGui.invisibleButton("ruler_scrub", sheetViewportW, RULER_HEIGHT);
        if (ImGui.isItemActive() || (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Left))) {
            float t = AnimationTimelineMath.xToTime(
                    ImGui.getMousePosX() - originX,
                    state.pixelsPerSecond(),
                    0f,
                    clip.length
            );
            state.setCurrentTime(clip.snapTime(t));
            state.setPlaying(false);
        }
        ImGui.endChild();
    }

    private static void renderSheetColumn(
            AnimationEditorState state,
            AnimationClip clip,
            float sheetViewportW,
            AssetDatabase assets,
            java.util.function.Consumer<Runnable> withUndo
    ) {
        float originX = ImGui.getCursorScreenPosX();
        float topY = ImGui.getCursorScreenPosY();
        float rowY = topY;
        ImGui.getWindowDrawList().addRectFilled(
                originX,
                rowY,
                originX + sheetViewportW,
                rowY + ROW_HEIGHT,
                0xFF181818
        );
        drawRowGrid(originX, rowY, sheetViewportW, state, clip);
        renderSpriteLane(state, clip, originX, rowY, sheetViewportW, assets, withUndo);
        drawPlayhead(state, clip, originX, rowY, ROW_HEIGHT);
        drawDropPreview(state, clip, originX, rowY, ROW_HEIGHT);
        ImGui.dummy(sheetViewportW, ROW_HEIGHT);
    }

    private static void drawRowGrid(
            float originX,
            float rowY,
            float sheetW,
            AnimationEditorState state,
            AnimationClip clip
    ) {
        float length = Math.max(0.01f, clip.length);
        float majorStep = rulerLabelStep(length, state.pixelsPerSecond(), sheetW);
        float minorStep = majorStep * 0.25f;
        if (minorStep < 0.001f) {
            minorStep = majorStep;
        }
        float y0 = rowY;
        float y1 = rowY + ROW_HEIGHT;
        for (float t = 0f; t <= length + 0.0001f; t += minorStep) {
            float x = originX + AnimationTimelineMath.timeToX(t, state.pixelsPerSecond(), 0f);
            if (x < originX - 1f || x > originX + sheetW + 1f) {
                continue;
            }
            ImGui.getWindowDrawList().addLine(
                    x,
                    y0,
                    x,
                    y1,
                    isMultipleOf(t, majorStep) ? GRID_COLOR : 0xFF222222,
                    1f
            );
        }
        ImGui.getWindowDrawList().addLine(originX, y1, originX + sheetW, y1, GRID_COLOR, 1f);
    }

    private static void drawPlayhead(AnimationEditorState state, AnimationClip clip, float originX, float topY, float height) {
        float playX = originX + AnimationTimelineMath.timeToX(state.currentTime(), state.pixelsPerSecond(), 0f);
        ImGui.getWindowDrawList().addLine(playX, topY, playX, topY + height, 0xCCFFFFFF, 1.5f);
    }

    private static void drawDropPreview(
            AnimationEditorState state,
            AnimationClip clip,
            float originX,
            float topY,
            float height
    ) {
        if (state.dropPreviewTime() < 0f) {
            return;
        }
        float x = originX + AnimationTimelineMath.timeToX(state.dropPreviewTime(), state.pixelsPerSecond(), 0f);
        ImGui.getWindowDrawList().addLine(x, topY, x, topY + height, DROP_LINE_COLOR, 2f);
        int frame = Math.round(state.dropPreviewTime() * clip.frameRate);
        String label = String.format("%.2fs  f%d", state.dropPreviewTime(), frame);
        ImGui.getWindowDrawList().addText(x + 6f, topY + 2f, DROP_LINE_COLOR, label);
    }

    private static void drawDropPreviewOnRuler(AnimationEditorState state, AnimationClip clip, float originX, float y) {
        if (state.dropPreviewTime() < 0f) {
            return;
        }
        float x = originX + AnimationTimelineMath.timeToX(state.dropPreviewTime(), state.pixelsPerSecond(), 0f);
        ImGui.getWindowDrawList().addLine(x, y - 12f, x, y + 12f, DROP_LINE_COLOR, 2f);
    }

    private static void renderSpriteLane(
            AnimationEditorState state,
            AnimationClip clip,
            float originX,
            float rowY,
            float sheetW,
            AssetDatabase assets,
            java.util.function.Consumer<Runnable> withUndo
    ) {
        AnimationTrack track = clip.findTrack(AnimationTrackPaths.SPRITE);
        if (track == null) {
            return;
        }
        float centerY = rowY + ROW_HEIGHT * 0.5f;
        int keyIndex = 0;
        for (SpriteKeyframe key : track.spriteKeyframes) {
            float x = originX + AnimationTimelineMath.timeToX(key.time(), state.pixelsPerSecond(), 0f);
            boolean selected = AnimationTrackPaths.SPRITE.equals(state.selectedTrackPath())
                    && keyIndex == state.selectedKeyframeIndex();
            drawDiamond(x, centerY, selected ? 0xFFFFD54F : 0xFFE0E0E0);
            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)
                    && Math.abs(ImGui.getMousePosX() - x) < 8f
                    && Math.abs(ImGui.getMousePosY() - centerY) < 8f) {
                state.setSelectedKeyframe(AnimationTrackPaths.SPRITE, keyIndex);
                state.setCurrentTime(key.time());
            }
            keyIndex++;
        }
        ImGui.setCursorScreenPos(originX, rowY);
        ImGui.invisibleButton("lane_sprite", sheetW, ROW_HEIGHT);
        if (ImGui.beginDragDropTarget()) {
            float time = timeAtMouse(state, clip, originX);
            state.setDropPreviewTime(time);
            drawDiamond(
                    originX + AnimationTimelineMath.timeToX(time, state.pixelsPerSecond(), 0f),
                    centerY,
                    DROP_DIAMOND_COLOR
            );
            String payload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (payload != null) {
                StudioAsset asset = assets.get(payload);
                if (asset != null && (asset.type() == AssetType.SPRITE || asset.type() == AssetType.TEXTURE)) {
                    String spriteGuid = resolveSpriteGuid(assets, asset);
                    withUndo.accept(() -> {
                        addOrReplaceSpriteKey(clip, track, time, spriteGuid);
                        track.sortKeyframes();
                    });
                }
            }
            ImGui.endDragDropTarget();
        }
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            float time = timeAtMouse(state, clip, originX);
            withUndo.accept(() -> {
                addSpriteKeyAtTime(clip, track, time);
                track.sortKeyframes();
            });
        }
    }

    private static float timeAtMouse(AnimationEditorState state, AnimationClip clip, float originX) {
        float t = AnimationTimelineMath.xToTime(
                ImGui.getMousePosX() - originX,
                state.pixelsPerSecond(),
                0f,
                clip.length
        );
        return clip.snapTime(t);
    }

    private static float rulerLabelStep(float length, float pixelsPerSecond, float viewportWidth) {
        float minPixels = 64f;
        float[] candidates = {0.01f, 0.02f, 0.05f, 0.1f, 0.25f, 0.5f, 1f, 2f, 5f, 10f, 30f, 60f};
        for (int i = candidates.length - 1; i >= 0; i--) {
            if (candidates[i] * pixelsPerSecond >= minPixels) {
                return candidates[i];
            }
        }
        return Math.max(0.1f, length / Math.max(1f, viewportWidth / minPixels));
    }

    private static boolean isMultipleOf(float time, float step) {
        if (step <= 0f) {
            return false;
        }
        float ratio = time / step;
        return Math.abs(ratio - Math.round(ratio)) < 0.05f;
    }

    private static String formatTimeLabel(float time) {
        if (time < 0.05f) {
            return "0";
        }
        return String.format("%.1f", time);
    }

    private static String resolveSpriteGuid(AssetDatabase assets, StudioAsset asset) {
        if (asset.type() == AssetType.SPRITE) {
            return asset.guid();
        }
        var children = assets.spriteChildren(asset.guid());
        return children.isEmpty() ? "" : children.get(0).guid();
    }

    private static void addOrReplaceSpriteKey(AnimationClip clip, AnimationTrack track, float time, String spriteGuid) {
        float snapped = clip.snapTime(time);
        for (int i = 0; i < track.spriteKeyframes.size(); i++) {
            if (Math.abs(track.spriteKeyframes.get(i).time() - snapped) < 0.001f) {
                track.spriteKeyframes.set(i, new SpriteKeyframe(snapped, spriteGuid));
                return;
            }
        }
        track.spriteKeyframes.add(new SpriteKeyframe(snapped, spriteGuid));
        if (snapped > clip.length) {
            clip.length = snapped;
        }
    }

    private static void addSpriteKeyAtTime(AnimationClip clip, AnimationTrack track, float time) {
        float snapped = clip.snapTime(time);
        String sprite = track.spriteKeyframes.isEmpty()
                ? ""
                : track.spriteKeyframes.get(track.spriteKeyframes.size() - 1).spriteGuid();
        track.spriteKeyframes.add(new SpriteKeyframe(snapped, sprite));
        if (snapped > clip.length) {
            clip.length = snapped;
        }
    }

    private static void drawDiamond(float x, float y, int color) {
        ImGui.getWindowDrawList().addQuadFilled(x, y - 5f, x + 5f, y, x, y + 5f, x - 5f, y, color);
    }
}
