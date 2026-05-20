package org.llw.studio.editor.widgets.fields;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.prefab.PrefabEditorActions;
import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.js.ScriptFieldApplicator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Scene entity or prefab reference for script fields: combo picker plus hierarchy/project drag-drop.
 */
public final class EntityReferenceField {

    /** Drag-drop and dirty-marking context for {@link #draw}. */
    public record DropContext(
            StudioContext studioContext,
            AssetDatabase assets,
            SelectionService selection,
            InspectorContext inspectorContext
    ) {
    }

    /**
     * Serialized entity reference: scene object id, prefab GUID, or none.
     */
    public record EntityFieldValue(int sceneId, String prefabGuid) {

        public static EntityFieldValue none() {
            return new EntityFieldValue(-1, "");
        }

        /** Parses stored JSON or script defaults into a field value. */
        public static EntityFieldValue fromJson(JsonNode value, AssetDatabase assets) {
            if (value == null || value instanceof NullNode || value.isNull()) {
                return none();
            }
            String prefab = ScriptFieldApplicator.entityFieldPrefabGuid(value);
            if (!prefab.isBlank()) {
                return new EntityFieldValue(-1, prefab);
            }
            return new EntityFieldValue(ScriptFieldApplicator.entitySceneId(value), "");
        }

        public boolean hasPrefab() {
            return prefabGuid != null && !prefabGuid.isBlank();
        }

        public boolean isNone() {
            return !hasPrefab() && sceneId < 0;
        }
    }

    private EntityReferenceField() {
    }

    public static EntityFieldValue draw(String label, Scene scene, EntityFieldValue value, DropContext dropContext) {
        AssetDatabase assets = dropContext == null ? null : dropContext.assets();
        PropertyRow.begin(label);
        List<SceneOption> sceneOptions = collectSceneOptions(scene);
        List<PrefabOption> prefabOptions = collectPrefabOptions(assets);
        EntityFieldValue result = value == null ? EntityFieldValue.none() : value;
        String preview = previewFor(sceneOptions, result, assets);
        if (ImGui.beginCombo("##entity_ref_" + label, preview)) {
            if (ImGui.selectable("None", result.isNone())) {
                result = EntityFieldValue.none();
            }
            if (!sceneOptions.isEmpty()) {
                ImGui.separator();
                EditorStyle.pushMutedText();
                ImGui.textUnformatted("Scene Objects");
                EditorStyle.popMutedText();
                for (SceneOption option : sceneOptions) {
                    if (ImGui.selectable(option.label, !result.hasPrefab() && option.sceneId == result.sceneId())) {
                        result = new EntityFieldValue(option.sceneId, "");
                    }
                }
            }
            if (!prefabOptions.isEmpty()) {
                ImGui.separator();
                EditorStyle.pushMutedText();
                ImGui.textUnformatted("Prefabs");
                EditorStyle.popMutedText();
                for (PrefabOption option : prefabOptions) {
                    boolean selected = result.hasPrefab() && Objects.equals(result.prefabGuid(), option.guid);
                    if (ImGui.selectable(option.label, selected)) {
                        result = new EntityFieldValue(-1, option.guid);
                    }
                }
            }
            ImGui.endCombo();
        }
        if (dropContext != null) {
            float dropX = ImGui.getWindowPosX() + EditorColors.INSPECTOR_LABEL_WIDTH;
            float dropY = ImGui.getItemRectMinY();
            float dropW = ImGui.getWindowContentRegionMaxX() - EditorColors.INSPECTOR_LABEL_WIDTH;
            float dropH = Math.max(ImGui.getItemRectSizeY(), ImGui.getFrameHeight());
            ImGui.setCursorScreenPos(dropX, dropY);
            ImGui.invisibleButton("##entity_drop_" + label, dropW, dropH);
            result = handleDrop(scene, result, dropContext);
        }
        PropertyRow.end();
        return result;
    }

    private static String previewFor(List<SceneOption> sceneOptions, EntityFieldValue value, AssetDatabase assets) {
        if (value.hasPrefab() && assets != null) {
            String name = assets.displayName(value.prefabGuid());
            return "Prefab: " + (name == null || name.isBlank() ? value.prefabGuid() : name);
        }
        int index = indexForSceneId(sceneOptions, value.sceneId());
        return index >= 0 ? sceneOptions.get(index).label : "None";
    }

    private static EntityFieldValue handleDrop(Scene scene, EntityFieldValue result, DropContext dropContext) {
        if (!ImGui.beginDragDropTarget()) {
            return result;
        }
        int flags = ImGuiDragDropFlags.AcceptNoDrawDefaultRect;
        boolean acceptedEntity = false;
        String entityPayload = ImGui.acceptDragDropPayload(SelectionService.PAYLOAD_ENTITY, flags, String.class);
        if (entityPayload != null && scene != null) {
            EntityId entity = PrefabEditorActions.parseEntityPayload(entityPayload);
            if (!entity.isNone()) {
                int droppedSceneId = SceneObjectIds.get(scene.world(), entity);
                if (droppedSceneId < 0) {
                    droppedSceneId = SceneObjectIds.allocate(scene);
                    SceneObjectIds.assign(scene, entity, droppedSceneId);
                }
                result = new EntityFieldValue(droppedSceneId, "");
                acceptedEntity = true;
                if (dropContext.inspectorContext() != null) {
                    dropContext.inspectorContext().markDirty();
                }
            }
        }
        if (!acceptedEntity) {
            String assetPayload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, flags, String.class);
            if (assetPayload != null && dropContext.assets() != null) {
                StudioAsset asset = dropContext.assets().get(assetPayload);
                if (asset != null && asset.type() == AssetType.PREFAB) {
                    result = new EntityFieldValue(-1, assetPayload);
                    if (dropContext.inspectorContext() != null) {
                        dropContext.inspectorContext().markDirty();
                    }
                }
            }
        }
        ImGui.endDragDropTarget();
        return result;
    }

    private static List<SceneOption> collectSceneOptions(Scene scene) {
        List<SceneOption> options = new ArrayList<>();
        if (scene == null) {
            return options;
        }
        var names = scene.world().store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId entity = names.entityAt(i);
            if (SceneObjectIds.isSceneRoot(scene.world(), entity)) {
                continue;
            }
            int id = SceneObjectIds.get(scene.world(), entity);
            if (id < 0) {
                continue;
            }
            NameComponent name = names.componentAt(i);
            options.add(new SceneOption(id, name == null ? "GameObject" : name.name() + " (" + id + ")"));
        }
        options.sort(Comparator.comparing(option -> option.label, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private static List<PrefabOption> collectPrefabOptions(AssetDatabase assets) {
        List<PrefabOption> options = new ArrayList<>();
        if (assets == null) {
            return options;
        }
        for (StudioAsset asset : assets.allAssets()) {
            if (asset.type() != AssetType.PREFAB) {
                continue;
            }
            String name = asset.displayName();
            String label = (name == null || name.isBlank() ? asset.guid() : name);
            options.add(new PrefabOption(asset.guid(), label));
        }
        options.sort(Comparator.comparing(option -> option.label, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    private static int indexForSceneId(List<SceneOption> options, int sceneId) {
        if (sceneId < 0) {
            return -1;
        }
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).sceneId == sceneId) {
                return i;
            }
        }
        return -1;
    }

    private record SceneOption(int sceneId, String label) {
    }

    private record PrefabOption(String guid, String label) {
    }
}
