package org.llw.studio.editor.inspector.builtin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import imgui.ImGui;
import imgui.type.ImString;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.widgets.ComponentFoldout;
import org.llw.studio.editor.widgets.fields.AssetReferenceField;
import org.llw.studio.editor.widgets.fields.BoolField;
import org.llw.studio.editor.widgets.fields.EntityReferenceField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptFieldSchema;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.studio.scripting.js.ScriptDiagnostics;
import org.llw.studio.scripting.js.ScriptFieldApplicator;
import org.llw.studio.systems.JsScriptSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Inspector for {@link ScriptComponent}: one foldout per script attachment with asset reference,
 * compile errors, and schema-driven fields.
 */
public final class ScriptDrawer implements ComponentDrawer<ScriptComponent> {
    private final ImString textBuffer = new ImString("", 256);

    @Override
    public void draw(ScriptComponent container, InspectorContext context) {
        AssetDatabase assets = context.assets();
        StudioContext studioContext = context.studioContext();
        boolean playing = studioContext != null && studioContext.isPlaying();
        var selected = context.selection() == null
                ? org.llw.studio.ecs.EntityId.none()
                : context.selection().selected();
        String entityKey = selected.isNone() ? "none" : selected.index() + ":" + selected.generation();

        List<ScriptAttachment> attachments = new ArrayList<>(container.attachments);
        for (ScriptAttachment attachment : attachments) {
            String title = titleFor(assets, attachment);
            String key = entityKey + ":script:" + attachment.slotId;
            ComponentFoldout.State state = ComponentFoldout.header(
                    key, title, EditorIcons.COMPONENT, true, !playing);
            if (state.removeClicked()) {
                container.removeAttachment(attachment.slotId);
                context.markDirty();
                continue;
            }
            if (state.open()) {
                ComponentFoldout.beginBody();
                drawAttachment(container, attachment, context, playing, selected);
                ComponentFoldout.endBody();
            }
        }

        if (!playing && ImGui.button("Add Script", -1f, 0f)) {
            container.addAttachment();
            context.markDirty();
        }
    }

    private static String titleFor(AssetDatabase assets, ScriptAttachment attachment) {
        if (!attachment.hasScriptReference()) {
            return "Script (empty)";
        }
        StudioAsset asset = assets.get(attachment.scriptGuid);
        if (asset != null && asset.displayName() != null && !asset.displayName().isBlank()) {
            return "Script: " + asset.displayName();
        }
        return "Script";
    }

    private void drawAttachment(
            ScriptComponent container,
            ScriptAttachment attachment,
            InspectorContext context,
            boolean playing,
            org.llw.studio.ecs.EntityId entity
    ) {
        AssetDatabase assets = context.assets();
        StudioContext studioContext = context.studioContext();

        boolean enabled = attachment.enabled;
        if (ImGui.checkbox("Enabled", enabled)) {
            if (!playing) {
                attachment.enabled = !enabled;
                context.markDirty();
            }
        }
        String guid = AssetReferenceField.draw("Script", attachment.scriptGuid, assets);
        if (!guid.equals(attachment.scriptGuid) && !playing) {
            if (guid.isBlank() || isScriptAsset(assets, guid)) {
                if (container.tryAssignGuid(attachment, guid)) {
                    context.markDirty();
                } else {
                    ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, EditorColors.LOG_ERROR[0], EditorColors.LOG_ERROR[1],
                            EditorColors.LOG_ERROR[2], EditorColors.LOG_ERROR[3]);
                    ImGui.textUnformatted("This script is already on the entity.");
                    ImGui.popStyleColor();
                }
            }
        }
        StudioAsset asset = assets.get(attachment.scriptGuid);
        if (attachment.hasScriptReference() && asset == null) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, EditorColors.LOG_ERROR[0], EditorColors.LOG_ERROR[1],
                    EditorColors.LOG_ERROR[2], EditorColors.LOG_ERROR[3]);
            ImGui.textUnformatted("Script asset is missing.");
            ImGui.popStyleColor();
        } else if (attachment.hasScriptReference()) {
            var diagnostic = ScriptDiagnostics.get(attachment.scriptGuid);
            if (diagnostic != null && diagnostic.error()) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, EditorColors.LOG_ERROR[0], EditorColors.LOG_ERROR[1],
                        EditorColors.LOG_ERROR[2], EditorColors.LOG_ERROR[3]);
                ImGui.textWrapped(diagnostic.message());
                ImGui.popStyleColor();
            }
        }

        if (!attachment.hasScriptReference() || studioContext == null) {
            return;
        }

        ScriptSchema schema = ScriptSchemaRegistry.get(studioContext.projectRoot(), attachment.scriptGuid);
        if (schema.fields.isEmpty()) {
            return;
        }

        ImGui.separator();
        JsScriptSystem scriptSystem = context.playScriptSystem();
        Scene activeScene = context.inspectionScene();
        EntityReferenceField.DropContext entityDrop = new EntityReferenceField.DropContext(
                studioContext,
                assets,
                context.selection(),
                context
        );
        if (activeScene == null) {
            return;
        }

        for (ScriptFieldSchema field : schema.fields) {
            drawField(attachment, context, playing, scriptSystem, activeScene, entity, field, entityDrop);
        }
    }

    private void drawField(
            ScriptAttachment attachment,
            InspectorContext context,
            boolean playing,
            JsScriptSystem scriptSystem,
            Scene activeScene,
            org.llw.studio.ecs.EntityId entity,
            ScriptFieldSchema field,
            EntityReferenceField.DropContext entityDrop
    ) {
        if (playing && scriptSystem != null && !entity.isNone()) {
            drawPlayField(attachment, context, scriptSystem, activeScene, entity, field, entityDrop);
            return;
        }
        drawEditField(attachment, context, activeScene, field, entityDrop);
    }

    private void drawPlayField(
            ScriptAttachment attachment,
            InspectorContext context,
            JsScriptSystem scriptSystem,
            Scene activeScene,
            org.llw.studio.ecs.EntityId entity,
            ScriptFieldSchema field,
            EntityReferenceField.DropContext entityDrop
    ) {
        int slotId = attachment.slotId;
        Value raw = scriptSystem.readFieldValue(entity, slotId, field.name, field);
        switch (effectivePlayFieldType(field.type, raw)) {
            case "number" -> {
                double value = scriptSystem.readNumberField(entity, slotId, field.name, field);
                float next = FloatField.draw(field.name, (float) value);
                if (next != value) {
                    scriptSystem.writeNumberField(entity, slotId, field.name, next);
                }
            }
            case "boolean" -> {
                boolean value = scriptSystem.readBooleanField(entity, slotId, field.name, field);
                boolean next = BoolField.draw(field.name, value);
                if (next != value) {
                    scriptSystem.writeBooleanField(entity, slotId, field.name, next);
                }
            }
            case "string" -> {
                String value = scriptSystem.readStringField(entity, slotId, field.name, field);
                textBuffer.set(value == null ? "" : value);
                ImGui.text(field.name);
                ImGui.sameLine();
                ImGui.setNextItemWidth(-1f);
                if (ImGui.inputText("##" + field.name + slotId, textBuffer)) {
                    scriptSystem.writeStringField(entity, slotId, field.name, textBuffer.get());
                }
            }
            case "entity" -> {
                EntityReferenceField.EntityFieldValue current = EntityReferenceField.EntityFieldValue.fromJson(
                        attachment.fields.get(field.name), context.assets());
                if (current.isNone()) {
                    current = EntityReferenceField.EntityFieldValue.fromJson(field.copyDefault(), context.assets());
                }
                EntityReferenceField.EntityFieldValue next = EntityReferenceField.draw(
                        field.name, activeScene, current, entityDrop);
                applyEntityFieldValuePlay(attachment, context, scriptSystem, entity, slotId, field, next, activeScene);
            }
            case "vector2" -> drawVector2FieldPlay(attachment, context, scriptSystem, entity, slotId, field);
            default -> drawEditField(attachment, context, activeScene, field, entityDrop);
        }
    }

    private void drawEditField(
            ScriptAttachment attachment,
            InspectorContext context,
            Scene activeScene,
            ScriptFieldSchema field,
            EntityReferenceField.DropContext entityDrop
    ) {
        JsonNode value = attachment.fields.getOrDefault(field.name, field.copyDefault());
        switch (field.type) {
            case "number" -> {
                float current = value == null || value.isNull() ? 0f : (float) value.asDouble();
                float next = FloatField.draw(field.name, current);
                if (next != current) {
                    attachment.setNumberField(field.name, next);
                    context.markDirty();
                }
            }
            case "boolean" -> {
                boolean current = value != null && !value.isNull() && value.asBoolean();
                boolean next = BoolField.draw(field.name, current);
                if (next != current) {
                    attachment.setBooleanField(field.name, next);
                    context.markDirty();
                }
            }
            case "string" -> {
                String current = value == null || value.isNull() ? "" : value.asText("");
                textBuffer.set(current);
                ImGui.text(field.name);
                ImGui.sameLine();
                ImGui.setNextItemWidth(-1f);
                if (ImGui.inputText("##" + field.name + attachment.slotId, textBuffer)) {
                    attachment.setTextField(field.name, textBuffer.get());
                    context.markDirty();
                }
            }
            case "entity" -> {
                EntityReferenceField.EntityFieldValue current = EntityReferenceField.EntityFieldValue.fromJson(
                        attachment.fields.get(field.name), context.assets());
                if (current.isNone() && (value == null || value.isNull())) {
                    current = EntityReferenceField.EntityFieldValue.fromJson(
                            field.copyDefault(), context.assets());
                }
                EntityReferenceField.EntityFieldValue next = EntityReferenceField.draw(
                        field.name, activeScene, current, entityDrop);
                applyEntityFieldValue(attachment, context, field.name, next);
            }
            case "vector2" -> drawVector2FieldEdit(attachment, context, field, value);
            default -> {
            }
        }
    }

    private static boolean isScriptAsset(AssetDatabase assets, String guid) {
        StudioAsset asset = assets.get(guid);
        return asset != null && asset.type() == AssetType.SCRIPT;
    }

    private static String effectivePlayFieldType(String schemaType, Value raw) {
        if (raw == null || raw.isNull()) {
            return schemaType;
        }
        if (raw.isHostObject() || raw.hasMember("sceneId")) {
            return "entity";
        }
        if (raw.fitsInDouble() && !raw.isString()) {
            return "number";
        }
        if (raw.isBoolean()) {
            return "boolean";
        }
        if (raw.isString()) {
            return "string";
        }
        return schemaType;
    }

    private static void applyEntityFieldValue(
            ScriptAttachment attachment,
            InspectorContext context,
            String fieldName,
            EntityReferenceField.EntityFieldValue next
    ) {
        if (next.isNone()) {
            if (attachment.fields.containsKey(fieldName)) {
                attachment.clearEntityField(fieldName);
                context.markDirty();
            }
            return;
        }
        if (next.hasPrefab()) {
            if (!next.prefabGuid().equals(attachment.entityFieldPrefabGuid(fieldName))) {
                attachment.setPrefabField(fieldName, next.prefabGuid());
                context.markDirty();
            }
            return;
        }
        if (next.sceneId() != attachment.entityFieldSceneId(fieldName)) {
            attachment.setEntityField(fieldName, next.sceneId());
            context.markDirty();
        }
    }

    private static void applyEntityFieldValuePlay(
            ScriptAttachment attachment,
            InspectorContext context,
            JsScriptSystem scriptSystem,
            org.llw.studio.ecs.EntityId entity,
            int slotId,
            ScriptFieldSchema field,
            EntityReferenceField.EntityFieldValue next,
            Scene activeScene
    ) {
        EntityReferenceField.EntityFieldValue current = EntityReferenceField.EntityFieldValue.fromJson(
                attachment.fields.get(field.name), context.assets());
        if (Objects.equals(current.prefabGuid(), next.prefabGuid())
                && current.sceneId() == next.sceneId()) {
            return;
        }
        applyEntityFieldValue(attachment, context, field.name, next);
        if (next.hasPrefab()) {
            scriptSystem.writePrefabField(entity, slotId, field.name, next.prefabGuid());
        } else if (next.isNone()) {
            scriptSystem.writeEntityField(entity, slotId, field.name, -1, activeScene);
        } else {
            scriptSystem.writeEntityField(entity, slotId, field.name, next.sceneId(), activeScene);
        }
    }

    private static float vector2Component(JsonNode value, String axis, JsonNode fallback) {
        if (value != null && value.isObject()) {
            return (float) value.path(axis).asDouble(0);
        }
        if (fallback != null && fallback.isObject()) {
            return (float) fallback.path(axis).asDouble(0);
        }
        return 0f;
    }

    private static float vector2FromInstance(Value raw, String axis) {
        if (raw == null || raw.isNull() || !raw.hasMember(axis)) {
            return 0f;
        }
        return (float) raw.getMember(axis).asDouble();
    }

    private static void drawVector2FieldEdit(
            ScriptAttachment attachment,
            InspectorContext context,
            ScriptFieldSchema field,
            JsonNode value
    ) {
        JsonNode defaults = field.copyDefault();
        float x = vector2Component(value, "x", defaults);
        float y = vector2Component(value, "y", defaults);
        float nextX = FloatField.draw(field.name + " X", x);
        float nextY = FloatField.draw(field.name + " Y", y);
        if (nextX != x || nextY != y) {
            attachment.setVector2Field(field.name, nextX, nextY);
            context.markDirty();
        }
    }

    private static void drawVector2FieldPlay(
            ScriptAttachment attachment,
            InspectorContext context,
            JsScriptSystem scriptSystem,
            org.llw.studio.ecs.EntityId entity,
            int slotId,
            ScriptFieldSchema field
    ) {
        Value raw = scriptSystem.readFieldValue(entity, slotId, field.name, field);
        float x = vector2FromInstance(raw, "x");
        float y = vector2FromInstance(raw, "y");
        if (raw == null || raw.isNull()) {
            JsonNode stored = attachment.fields.getOrDefault(field.name, field.copyDefault());
            x = vector2Component(stored, "x", field.copyDefault());
            y = vector2Component(stored, "y", field.copyDefault());
        }
        float nextX = FloatField.draw(field.name + " X", x);
        float nextY = FloatField.draw(field.name + " Y", y);
        if (nextX != x || nextY != y) {
            scriptSystem.writeVector2Field(entity, slotId, field.name, nextX, nextY);
            context.markDirty();
        }
    }
}
