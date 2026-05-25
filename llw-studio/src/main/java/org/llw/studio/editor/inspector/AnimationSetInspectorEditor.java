package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.widgets.PropertyRow;

import java.io.IOException;

/**
 * Inspector editor for parent {@link AssetType#ANIMATION} assets.
 */
public final class AnimationSetInspectorEditor {
    public void render(StudioAsset asset, AssetDatabase assets) {
        if (asset == null || asset.type() != AssetType.ANIMATION) {
            return;
        }
        AnimationSetDefinition set = assets.animationSet(asset.guid());
        if (set == null) {
            ImGui.textDisabled("Failed to load animation set.");
            return;
        }
        ImString defaultState = new ImString(set.defaultState == null ? "" : set.defaultState, 64);
        if (ImGui.inputText("Default State", defaultState)) {
            set.defaultState = defaultState.get();
        }
        ImGui.textDisabled("Each state has one clip with the same name. Use the Animation panel to add states.");
        for (int i = set.states.size() - 1; i >= 0; i--) {
            AnimationStateDefinition state = set.states.get(i);
            ImGui.pushID(i);
            ImGui.separator();
            ImGui.text("State: " + state.name());
            if (ImGui.button("Remove")) {
                set.states.remove(i);
            }
            ImGui.popID();
        }
        if (ImGui.button("Save Animation")) {
            try {
                assets.saveAnimationSet(asset.path(), set);
            } catch (IOException ex) {
                ImGui.textColored(EditorColors.DANGER[0], EditorColors.DANGER[1], EditorColors.DANGER[2],
                        EditorColors.DANGER[3], ex.getMessage());
            }
        }
        PropertyRow.readOnlyValue("States", String.valueOf(set.states.size()));
    }
}
