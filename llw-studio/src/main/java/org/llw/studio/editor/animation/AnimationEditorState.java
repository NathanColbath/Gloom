package org.llw.studio.editor.animation;

import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationSample;
import org.llw.studio.animation.AnimationSampler;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.AnimationSystem;

/**
 * Edit-mode animation preview transport and scrub state.
 */
public final class AnimationEditorState {
    private String pinnedAnimationGuid = "";
    private String pinnedClipGuid = "";
    private String previewStateName = "";
    private EntityId previewTargetEntity = EntityId.none();
    private AnimationClip clip;
    private float currentTime;
    private boolean playing;
    private boolean loop = true;
    private float speed = 1f;
    private boolean previewInPanel = true;
    private boolean previewInScene;
    private float scrollX;
    private float pixelsPerSecond = 100f;
    private String selectedTrackPath = "";
    private int selectedKeyframeIndex = -1;
    private float dropPreviewTime = -1f;

    public String pinnedAnimationGuid() {
        return pinnedAnimationGuid;
    }

    public void pinAnimation(String animationGuid) {
        pinnedAnimationGuid = animationGuid == null ? "" : animationGuid;
    }

    public String pinnedClipGuid() {
        return pinnedClipGuid;
    }

    public void bindClip(AnimationClip clip, String clipGuid, String animationGuid) {
        this.clip = clip;
        this.pinnedClipGuid = clipGuid == null ? "" : clipGuid;
        if (animationGuid != null && !animationGuid.isBlank()) {
            pinnedAnimationGuid = animationGuid;
        }
    }

    public String clipGuid() {
        return pinnedClipGuid;
    }

    public AnimationClip clip() {
        return clip;
    }

    public String previewStateName() {
        return previewStateName;
    }

    public void setPreviewStateName(String name) {
        previewStateName = name == null ? "" : name;
    }

    public EntityId previewTargetEntity() {
        return previewTargetEntity;
    }

    public void setPreviewTargetEntity(EntityId entity) {
        previewTargetEntity = entity == null ? EntityId.none() : entity;
    }

    public float currentTime() {
        return currentTime;
    }

    public void setCurrentTime(float time) {
        if (clip == null) {
            currentTime = 0f;
            return;
        }
        currentTime = Math.max(0f, Math.min(clip.length, time));
    }

    public boolean playing() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean loop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public float speed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed <= 0f ? 1f : speed;
    }

    public boolean previewInPanel() {
        return previewInPanel;
    }

    public void setPreviewInPanel(boolean previewInPanel) {
        this.previewInPanel = previewInPanel;
    }

    public boolean previewInScene() {
        return previewInScene;
    }

    public void setPreviewInScene(boolean previewInScene) {
        this.previewInScene = previewInScene;
    }

    public float scrollX() {
        return scrollX;
    }

    public void setScrollX(float scrollX) {
        this.scrollX = Math.max(0f, scrollX);
    }

    public float pixelsPerSecond() {
        return pixelsPerSecond;
    }

    public void setPixelsPerSecond(float pixelsPerSecond) {
        this.pixelsPerSecond = Math.max(20f, pixelsPerSecond);
    }

    public String selectedTrackPath() {
        return selectedTrackPath;
    }

    public void setSelectedKeyframe(String trackPath, int keyframeIndex) {
        selectedTrackPath = trackPath == null ? "" : trackPath;
        selectedKeyframeIndex = keyframeIndex;
    }

    public int selectedKeyframeIndex() {
        return selectedKeyframeIndex;
    }

    public boolean hasSelectedKeyframe() {
        return selectedKeyframeIndex >= 0 && !selectedTrackPath.isBlank();
    }

    public void clearDropPreview() {
        dropPreviewTime = -1f;
    }

    public void setDropPreviewTime(float time) {
        if (clip == null) {
            dropPreviewTime = -1f;
            return;
        }
        dropPreviewTime = Math.max(0f, Math.min(clip.length, time));
    }

    public float dropPreviewTime() {
        return dropPreviewTime;
    }

    public boolean previewActive() {
        return clip != null && (previewInPanel || (previewInScene && !previewTargetEntity.isNone()));
    }

    public boolean shouldAdvancePanelPreview() {
        return previewInPanel && clip != null;
    }

    public boolean shouldApplyScenePreview() {
        return previewInScene && clip != null && !previewTargetEntity.isNone();
    }

    public void stopPreview() {
        playing = false;
        currentTime = 0f;
    }

    public void advancePreview(float delta) {
        if (!playing || clip == null || delta <= 0f) {
            return;
        }
        currentTime += delta * speed;
        if (currentTime > clip.length) {
            if (loop && clip.length > 0f) {
                currentTime %= clip.length;
            } else {
                currentTime = clip.length;
                playing = false;
            }
        }
    }

    public AnimationSample sampleAtCurrentTime() {
        return clip == null ? new AnimationSample() : AnimationSampler.sample(clip, currentTime);
    }

    public void applyScenePreview(Scene scene, AssetDatabase assets) {
        if (!shouldApplyScenePreview() || scene == null || assets == null) {
            return;
        }
        AnimationSampler.apply(scene.world(), previewTargetEntity, clip, currentTime, assets);
    }

    public float timeFromMouseX(float mouseX, float laneScreenX, float laneWidth) {
        if (clip == null || laneWidth <= 0f) {
            return 0f;
        }
        float localX = mouseX - laneScreenX + scrollX;
        float t = localX / pixelsPerSecond;
        return clip.snapTime(Math.max(0f, Math.min(clip.length, t)));
    }

    public float timeToX(float time) {
        return time * pixelsPerSecond - scrollX;
    }
}
