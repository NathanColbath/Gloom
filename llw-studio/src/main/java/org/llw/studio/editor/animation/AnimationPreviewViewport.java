package org.llw.studio.editor.animation;

import imgui.ImGui;
import org.llw.studio.animation.AnimationSample;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.editor.widgets.SpriteSlicePreview;

/**
 * In-panel sprite preview for the active animation clip at the current time.
 */
public final class AnimationPreviewViewport {
    private static final float VIEW_SIZE = 160f;

    private AnimationPreviewViewport() {
    }

    public static void render(AnimationEditorState state, AssetDatabase assets) {
        ImGui.alignTextToFramePadding();
        ImGui.text("Preview");
        if (!state.previewInPanel() || state.clip() == null) {
            ImGui.textDisabled("Preview disabled");
            return;
        }
        AnimationSample sample = state.sampleAtCurrentTime();
        if (!sample.hasSprite) {
            ImGui.textDisabled("No sprite key at playhead");
            return;
        }
        SpriteDefinition slice = assets.sprite(sample.spriteGuid);
        var texture = assets.textureForSprite(sample.spriteGuid);
        if (slice == null || texture == null) {
            ImGui.textDisabled("Sprite unavailable");
            return;
        }
        float padX = Math.max(0f, (ImGui.getContentRegionAvailX() - VIEW_SIZE) * 0.5f);
        if (padX > 0f) {
            ImGui.setCursorPosX(ImGui.getCursorPosX() + padX);
        }
        SpriteSlicePreview.draw(texture, slice, VIEW_SIZE);
    }
}
