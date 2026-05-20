package org.llw.studio.editor.animation;

import org.junit.jupiter.api.Test;
import org.llw.studio.animation.AnimationClip;

import static org.junit.jupiter.api.Assertions.*;

class AnimationEditorStateTest {
    @Test
    void previewActiveWithoutEntityWhenPanelPreviewOn() {
        AnimationEditorState state = new AnimationEditorState();
        AnimationClip clip = new AnimationClip();
        state.bindClip(clip, "clip-guid", "anim-guid");
        state.setPreviewInPanel(true);
        state.setPreviewInScene(false);
        assertTrue(state.previewActive());
        assertTrue(state.shouldAdvancePanelPreview());
        assertFalse(state.shouldApplyScenePreview());
    }
}
