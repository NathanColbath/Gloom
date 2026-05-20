package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.animation.AnimationClipActions;
import org.llw.studio.editor.animation.AnimationEditorState;
import org.llw.studio.editor.animation.AnimationPreviewViewport;
import org.llw.studio.editor.animation.AnimationSetActions;
import org.llw.studio.editor.animation.AnimationTimelineView;
import org.llw.studio.editor.animation.AnimationTransportBar;
import org.llw.studio.editor.animation.CreateStateDialog;
import org.llw.studio.editor.commands.AnimationClipEditCommand;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unity-style animation window with transport, dopesheet, and in-panel preview.
 */
public final class AnimationPanel implements EditorPanel {
    private final SelectionService selection;
    private final AssetDatabase assets;
    private final UndoStack undoStack;
    private final PanelVisibility visibility;
    private final AnimationEditorState animationState;
    private final CreateStateDialog createStateDialog = new CreateStateDialog();
    private String lastSaveMessage = "";
    private boolean pendingCreateState;

    public AnimationPanel(
            SelectionService selection,
            AssetDatabase assets,
            EditorSession session,
            UndoStack undoStack,
            PanelVisibility visibility
    ) {
        this.selection = selection;
        this.assets = assets;
        this.undoStack = undoStack;
        this.visibility = visibility;
        this.animationState = new AnimationEditorState();
        session.setAnimationEditorState(animationState);
    }

    @Override
    public String id() {
        return "animation";
    }

    @Override
    public String title() {
        return "Animation";
    }

    @Override
    public void render(StudioContext context) {
        if (!visibility.isOpen(id())) {
            return;
        }
        boolean draw = visibility.begin(id(), title(), ImGuiWindowFlags.None);
        try {
            if (!draw) {
                return;
            }
            if (context.isPlaying()) {
                ImGui.textDisabled("Animation editing is disabled during Play mode.");
                return;
            }
            handleCreateStateDialog(context);
            syncFromEditor(context);
            AnimationClip clip = animationState.clip();
            if (clip == null) {
                ImGui.textWrapped("Select an Animation asset or a state in the Project panel, or assign Animation 2D on an entity.");
                return;
            }
            clip.ensureDefaultTracks();
            java.util.function.Consumer<Runnable> withUndo = edit -> {
                AnimationClip before = clip.copy();
                edit.run();
                undoStack.execute(new AnimationClipEditCommand(clip, before, clip.copy()));
                try {
                    saveClip(clip);
                } catch (IOException ignored) {
                }
            };
            drawToolbar(context, clip, withUndo);
            if (!lastSaveMessage.isBlank()) {
                ImGui.sameLine(0f, 12f);
                ImGui.textColored(0.4f, 0.85f, 0.5f, 1f, lastSaveMessage);
            }
            ImGui.separator();
            AnimationTransportBar.render(animationState);
            AnimationTransportBar.renderClipSettings(clip, withUndo);
            ImGui.separator();
            float previewWidth = 180f;
            ImGui.beginChild("anim_left", previewWidth, ImGui.getContentRegionAvailY(), true);
            AnimationPreviewViewport.render(animationState, assets);
            drawPreviewTargetDropdown(context);
            ImGui.endChild();
            ImGui.sameLine();
            ImGui.beginChild("anim_right", 0f, ImGui.getContentRegionAvailY(), false);
            AnimationTimelineView.render(animationState, clip, assets, withUndo);
            ImGui.endChild();
        } finally {
            visibility.end();
        }
    }

    private void handleCreateStateDialog(StudioContext context) {
        if (pendingCreateState) {
            createStateDialog.open();
            pendingCreateState = false;
        }
        String newName = createStateDialog.render();
        if (newName == null) {
            return;
        }
        String animGuid = animationState.pinnedAnimationGuid();
        if (animGuid.isBlank()) {
            return;
        }
        try {
            String clipGuid = AnimationSetActions.createState(assets, animGuid, newName);
            AnimationClip newClip = assets.animationClip(clipGuid);
            if (newClip != null) {
                animationState.bindClip(newClip, clipGuid, animGuid);
                animationState.setPreviewStateName(newName);
            }
            assets.refresh();
            syncFromEditor(context);
            lastSaveMessage = "State created";
        } catch (IOException ex) {
            lastSaveMessage = ex.getMessage();
        }
    }

    private void syncFromEditor(StudioContext context) {
        StudioAsset selected = assets.selected();
        if (selected != null && selected.type() == AssetType.ANIMATION_CLIP) {
            AnimationClip clip = assets.animationClip(selected.guid());
            String parentAnim = selected.parentAnimationGuid();
            if (clip != null) {
                animationState.bindClip(clip, selected.guid(), parentAnim == null ? "" : parentAnim);
                if (parentAnim != null && !parentAnim.isBlank()) {
                    AnimationSetDefinition set = assets.animationSet(parentAnim);
                    if (set != null) {
                        for (AnimationStateDefinition state : set.states) {
                            if (state.clipGuid().equals(selected.guid())) {
                                animationState.setPreviewStateName(state.name());
                                break;
                            }
                        }
                    }
                }
            }
            return;
        }
        if (selected != null && selected.type() == AssetType.ANIMATION) {
            animationState.pinAnimation(selected.guid());
            AnimationSetDefinition set = assets.animationSet(selected.guid());
            if (set != null && !set.states.isEmpty()) {
                String stateName = animationState.previewStateName();
                if (stateName.isBlank()) {
                    stateName = set.defaultState;
                }
                selectPreviewState(selected.guid(), set, stateName);
            }
            return;
        }
        EntityId entity = selection.selected();
        if (!entity.isNone()) {
            Animation2DComponent anim = context.editScene().world().getComponent(entity, Animation2DComponent.class);
            if (anim != null) {
                animationState.setPreviewTargetEntity(entity);
                if (anim.animationGuid != null && !anim.animationGuid.isBlank()) {
                    animationState.pinAnimation(anim.animationGuid);
                    String stateName = anim.currentState;
                    if (stateName == null || stateName.isBlank()) {
                        stateName = anim.defaultState;
                    }
                    String clipGuid = assets.stateClipGuid(anim.animationGuid, stateName);
                    AnimationClip clip = assets.animationClip(clipGuid);
                    if (clip != null) {
                        animationState.bindClip(clip, clipGuid, anim.animationGuid);
                        animationState.setPreviewStateName(stateName);
                    }
                } else if (anim.clipGuid != null && !anim.clipGuid.isBlank()) {
                    AnimationClip clip = assets.animationClip(anim.clipGuid);
                    if (clip != null) {
                        animationState.bindClip(clip, anim.clipGuid, "");
                    }
                }
            }
        }
    }

    private void drawToolbar(StudioContext context, AnimationClip clip, java.util.function.Consumer<Runnable> withUndo) {
        float comboWidth = 200f;
        List<StudioAsset> animations = assets.allAnimations();
        if (!animations.isEmpty()) {
            ImGui.alignTextToFramePadding();
            ImGui.text("Animation");
            ImGui.sameLine(0f, 6f);
            int animIndex = indexOfGuid(animations, animationState.pinnedAnimationGuid());
            String[] animLabels = animations.stream().map(StudioAsset::friendlyDisplayName).toArray(String[]::new);
            ImInt idx = new ImInt(Math.max(0, animIndex));
            ImGui.setNextItemWidth(comboWidth);
            if (ImGui.combo("##anim", idx, animLabels)) {
                animationState.pinAnimation(animations.get(idx.get()).guid());
                syncFromEditor(context);
            }
        }
        drawStateSelector(context);
        drawStateActionsMenu(context, clip, withUndo);
        ImGui.sameLine(0f, 12f);
        if (ImGui.button("Save")) {
            try {
                saveAll(clip);
                lastSaveMessage = "Saved";
            } catch (IOException ex) {
                lastSaveMessage = "Save failed";
            }
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Save the animation set and all state clips.");
        }
    }

    private void drawStateSelector(StudioContext context) {
        String animGuid = animationState.pinnedAnimationGuid();
        if (animGuid.isBlank()) {
            return;
        }
        AnimationSetDefinition set = assets.animationSet(animGuid);
        if (set == null) {
            return;
        }
        ImGui.sameLine(0f, 16f);
        ImGui.alignTextToFramePadding();
        ImGui.text("State");
        ImGui.sameLine(0f, 6f);
        if (set.states.isEmpty()) {
            ImGui.textDisabled("(none)");
            return;
        }
        String[] names = set.states.stream().map(AnimationStateDefinition::name).toArray(String[]::new);
        int current = 0;
        String preview = animationState.previewStateName();
        if (preview.isBlank()) {
            preview = set.defaultState;
        }
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(preview)) {
                current = i;
                break;
            }
        }
        ImInt idx = new ImInt(current);
        ImGui.setNextItemWidth(160f);
        if (ImGui.combo("##state", idx, names)) {
            selectPreviewState(animGuid, set, names[idx.get()]);
        }
    }

    private void drawStateActionsMenu(
            StudioContext context,
            AnimationClip clip,
            java.util.function.Consumer<Runnable> withUndo
    ) {
        String animGuid = animationState.pinnedAnimationGuid();
        ImGui.sameLine(0f, 12f);
        if (ImGui.button("Actions")) {
            ImGui.openPopup("anim_panel_actions");
        }
        if (!ImGui.beginPopup("anim_panel_actions")) {
            return;
        }
        if (!animGuid.isBlank()) {
            AnimationSetDefinition set = assets.animationSet(animGuid);
            if (ImGui.menuItem("Create State...")) {
                pendingCreateState = true;
                ImGui.closeCurrentPopup();
            }
            if (set != null && ImGui.menuItem("Set Default State")) {
                try {
                    String stateName = currentPreviewStateName(set);
                    if (!stateName.isBlank()) {
                        AnimationSetActions.setDefaultState(assets, animGuid, stateName);
                        assets.refresh();
                    }
                } catch (IOException ex) {
                    lastSaveMessage = ex.getMessage();
                }
            }
            ImGui.separator();
        }
        if (ImGui.menuItem("Generate From Spritesheet")) {
            StudioAsset texture = findTextureForGenerate(context.editScene());
            if (texture != null) {
                withUndo.accept(() -> AnimationClipActions.generateFromSpritesheet(assets, clip, texture.guid()));
            }
        }
        ImGui.endPopup();
    }

    private void selectPreviewState(String animGuid, AnimationSetDefinition set, String stateName) {
        animationState.setPreviewStateName(stateName);
        String clipGuid = set.clipGuidForState(stateName);
        AnimationClip clip = assets.animationClip(clipGuid);
        if (clip != null) {
            animationState.bindClip(clip, clipGuid, animGuid);
        }
    }

    private String currentPreviewStateName(AnimationSetDefinition set) {
        String name = animationState.previewStateName();
        if (name == null || name.isBlank()) {
            return set.defaultState == null ? "" : set.defaultState;
        }
        return name;
    }

    private void drawPreviewTargetDropdown(StudioContext context) {
        ImGui.spacing();
        List<String> labels = new ArrayList<>();
        labels.add("(None)");
        List<EntityId> entities = new ArrayList<>();
        entities.add(EntityId.none());
        var names = context.editScene().world().store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId entity = names.entityAt(i);
            if (context.editScene().world().getComponent(entity, SpriteRendererComponent.class) == null) {
                continue;
            }
            labels.add(names.componentAt(i).name());
            entities.add(entity);
        }
        int current = 0;
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).equals(animationState.previewTargetEntity())) {
                current = i;
                break;
            }
        }
        ImGui.alignTextToFramePadding();
        ImGui.text("Preview target");
        ImInt idx = new ImInt(current);
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.combo("##previewTarget", idx, labels.toArray(String[]::new))) {
            animationState.setPreviewTargetEntity(entities.get(idx.get()));
        }
    }

    private static int indexOfGuid(List<StudioAsset> assets, String guid) {
        for (int i = 0; i < assets.size(); i++) {
            if (assets.get(i).guid().equals(guid)) {
                return i;
            }
        }
        return 0;
    }

    private StudioAsset findTextureForGenerate(Scene scene) {
        EntityId entity = animationState.previewTargetEntity();
        if (!entity.isNone() && scene != null) {
            SpriteRendererComponent sprite = scene.world().getComponent(entity, SpriteRendererComponent.class);
            if (sprite != null && sprite.spriteGuid != null && !sprite.spriteGuid.isBlank()) {
                var definition = assets.sprite(sprite.spriteGuid);
                if (definition != null) {
                    return assets.get(definition.textureGuid());
                }
            }
        }
        return null;
    }

    private void saveClip(AnimationClip clip) throws IOException {
        String guid = animationState.pinnedClipGuid();
        if (guid.isBlank()) {
            return;
        }
        StudioAsset asset = assets.get(guid);
        if (asset != null) {
            assets.saveAnimationClip(asset.path(), clip);
        }
    }

    private void saveAll(AnimationClip clip) throws IOException {
        saveClip(clip);
        String animGuid = animationState.pinnedAnimationGuid();
        if (!animGuid.isBlank()) {
            StudioAsset parent = assets.get(animGuid);
            AnimationSetDefinition set = assets.animationSet(animGuid);
            if (parent != null && set != null) {
                assets.saveAnimationSet(parent.path(), set);
            }
            for (StudioAsset child : assets.clipChildren(animGuid)) {
                AnimationClip childClip = assets.animationClip(child.guid());
                if (childClip != null) {
                    assets.saveAnimationClip(child.path(), childClip);
                }
            }
        }
    }
}
