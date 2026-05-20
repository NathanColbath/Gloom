package org.llw.studio.shadergraph.editor;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.widgets.fields.AssetReferenceField;
import org.llw.studio.shadergraph.model.ShaderGraphNode;
import org.llw.studio.shadergraph.model.ShaderNodeType;

/**
 * Inline property editor for the selected shader graph node.
 */
public final class ShaderGraphNodeInspector {
    private ShaderGraphNodeInspector() {
    }

    public static void render(ShaderGraphEditorState state, AssetDatabase assets) {
        ImGui.text("Inspector");
        ShaderGraphNode node = state.document().nodeById(state.selectedNodeId());
        if (node == null) {
            ImGui.textDisabled("Select a node");
            return;
        }
        ImGui.text(node.type.name());
        ImGui.separator();
        switch (node.type) {
            case Color -> {
                float[] rgba = {
                        node.param("r", 1f),
                        node.param("g", 1f),
                        node.param("b", 1f),
                        node.param("a", 1f)
                };
                if (ImGui.colorEdit4("RGBA", rgba)) {
                    node.params.put("r", rgba[0]);
                    node.params.put("g", rgba[1]);
                    node.params.put("b", rgba[2]);
                    node.params.put("a", rgba[3]);
                    state.touch();
                }
            }
            case FloatConstant -> {
                float[] value = {node.param("value", 0f)};
                if (ImGui.dragFloat("Value", value, 0.01f, -100f, 100f)) {
                    node.params.put("value", value[0]);
                    state.touch();
                }
            }
            default -> ImGui.textDisabled("No parameters");
        }
        ImGui.separator();
        String nextGuid = AssetReferenceField.draw(
                "Preview Texture",
                state.document().previewTextureGuid,
                assets
        );
        if (!nextGuid.equals(state.document().previewTextureGuid)) {
            state.document().previewTextureGuid = nextGuid;
            state.touch();
        }
    }
}
