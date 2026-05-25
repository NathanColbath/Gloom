package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.editor.widgets.fields.ShaderGraphReferenceField;
import org.llw.studio.editor.widgets.fields.TextureReferenceField;
import org.llw.studio.materials.model.MaterialDocument;
import org.llw.studio.materials.model.MaterialProperty;
import org.llw.studio.materials.model.MaterialPropertyType;
import org.llw.studio.materials.model.MaterialShaderSource;

import java.io.IOException;

/**
 * Inspector editor for {@link AssetType#MATERIAL} assets.
 */
public final class MaterialInspectorEditor {
    private static final String[] SHADER_SOURCE_LABELS = {
            MaterialShaderSource.BUILTIN_LIT.name(),
            MaterialShaderSource.BUILTIN_UNLIT.name(),
            MaterialShaderSource.SHADER_GRAPH.name()
    };
    private static final String[] PROPERTY_TYPE_LABELS = {
            MaterialPropertyType.FLOAT.name(),
            MaterialPropertyType.COLOR.name(),
            MaterialPropertyType.TEXTURE.name()
    };

    private String loadedGuid = "";
    private MaterialDocument working;

    /**
     * @param asset   selected material asset
     * @param assets  asset database
     * @param session editor session (shader graph panel + material cache)
     */
    public void render(StudioAsset asset, AssetDatabase assets, EditorSession session) {
        if (asset == null || asset.type() != AssetType.MATERIAL) {
            return;
        }
        ensureLoaded(asset, assets);
        if (working == null) {
            ImGui.textDisabled("Failed to load material.");
            return;
        }

        ImGui.separator();
        ImGui.text("Shader");
        int sourceIndex = shaderSourceIndex(working.shaderSource);
        if (ImGui.beginCombo("Shader Source", SHADER_SOURCE_LABELS[sourceIndex])) {
            for (int i = 0; i < SHADER_SOURCE_LABELS.length; i++) {
                if (ImGui.selectable(SHADER_SOURCE_LABELS[i], i == sourceIndex)) {
                    working.shaderSource = SHADER_SOURCE_LABELS[i];
                }
            }
            ImGui.endCombo();
        }

        if (MaterialShaderSource.SHADER_GRAPH.name().equals(working.shaderSource)) {
            working.shaderGraphGuid = ShaderGraphReferenceField.draw(
                    "Shader Graph",
                    working.shaderGraphGuid,
                    assets,
                    session.shaderGraphPanel()
            );
        }

        ImGui.separator();
        ImGui.text("Maps");
        working.normalMapTextureGuid = TextureReferenceField.draw(
                "Normal Map",
                working.normalMapTextureGuid,
                assets
        );

        ImGui.separator();
        ImGui.text("Properties");
        for (int i = working.properties.size() - 1; i >= 0; i--) {
            MaterialProperty property = working.properties.get(i);
            ImGui.pushID(i);
            ImGui.separator();
            ImString nameBuffer = new ImString(property.name == null ? "" : property.name, 64);
            if (ImGui.inputText("Name", nameBuffer)) {
                property.name = nameBuffer.get();
            }
            int typeIndex = propertyTypeIndex(property.type);
            if (ImGui.beginCombo("Type", PROPERTY_TYPE_LABELS[typeIndex])) {
                for (int t = 0; t < PROPERTY_TYPE_LABELS.length; t++) {
                    if (ImGui.selectable(PROPERTY_TYPE_LABELS[t], t == typeIndex)) {
                        property.type = PROPERTY_TYPE_LABELS[t];
                    }
                }
                ImGui.endCombo();
            }
            MaterialPropertyType type = parsePropertyType(property.type);
            switch (type) {
                case FLOAT -> property.floatValue = FloatField.draw("Value", property.floatValue);
                case COLOR -> {
                    float[] rgba = ColorField.draw("Value", property.r, property.g, property.b, property.a);
                    property.r = rgba[0];
                    property.g = rgba[1];
                    property.b = rgba[2];
                    property.a = rgba[3];
                }
                case TEXTURE -> property.textureGuid = TextureReferenceField.draw(
                        "Texture",
                        property.textureGuid,
                        assets
                );
            }
            if (ImGui.button("Remove Property")) {
                working.properties.remove(i);
            }
            ImGui.popID();
        }
        if (ImGui.button("Add Property")) {
            working.properties.add(new MaterialProperty());
        }

        if (ImGui.button("Save Material")) {
            try {
                assets.saveMaterial(asset.path(), working);
                if (session.materialProgramCache() != null) {
                    session.materialProgramCache().invalidate(asset.guid()); // Viewport must recompile after shader/property save.
                }
            } catch (IOException ex) {
                ImGui.textColored(EditorColors.DANGER[0], EditorColors.DANGER[1], EditorColors.DANGER[2],
                        EditorColors.DANGER[3], ex.getMessage());
            }
        }
    }

    private void ensureLoaded(StudioAsset asset, AssetDatabase assets) {
        if (asset.guid().equals(loadedGuid) && working != null) {
            return;
        }
        loadedGuid = asset.guid();
        MaterialDocument loaded = assets.loadMaterial(asset.guid());
        // Working copy avoids disk write on every ImGui field tick.
        working = loaded == null ? null : loaded.copy();
    }

    private static int shaderSourceIndex(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        for (int i = 0; i < SHADER_SOURCE_LABELS.length; i++) {
            if (SHADER_SOURCE_LABELS[i].equalsIgnoreCase(raw)) {
                return i;
            }
        }
        return 0;
    }

    private static int propertyTypeIndex(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        for (int i = 0; i < PROPERTY_TYPE_LABELS.length; i++) {
            if (PROPERTY_TYPE_LABELS[i].equalsIgnoreCase(raw)) {
                return i;
            }
        }
        return 0;
    }

    private static MaterialPropertyType parsePropertyType(String raw) {
        if (raw == null || raw.isBlank()) {
            return MaterialPropertyType.FLOAT;
        }
        try {
            return MaterialPropertyType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return MaterialPropertyType.FLOAT;
        }
    }
}
