package org.llw.studio.editor.prefab;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.components.ComponentCatalog;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.ComponentTypeInfo;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.widgets.ComponentFoldout;
import org.llw.studio.editor.widgets.InspectorObjectHeader;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.serialization.PrefabSerializer;

import java.nio.file.Path;

/**
 * In-inspector prefab JSON editor using a {@link PrefabScratchScene} and component drawers.
 */
public final class PrefabAssetEditor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ComponentCatalog catalog;
    private final InspectorContext inspectorContext;
    private final PrefabScratchScene scratch = new PrefabScratchScene();

    private String loadedGuid = "";
    private ObjectNode document;
    private boolean dirty;
    private final ImString nameBuffer = new ImString("", 128);
    private final ImString tagBuffer = new ImString("", 128);
    private EntityId lastRenderedEntity = EntityId.none();

    /**
     * @param catalog          component types and drawers
     * @param inspectorContext shared inspector context (scratch scene override)
     */
    public PrefabAssetEditor(ComponentCatalog catalog, InspectorContext inspectorContext) {
        this.catalog = catalog;
        this.inspectorContext = inspectorContext;
    }

    /** @return whether prefab JSON has unsaved scratch edits */
    public boolean isDirty() {
        return dirty;
    }

    /** Drops loaded prefab state and inspection scene override. */
    public void clear() {
        loadedGuid = "";
        document = null;
        dirty = false;
        lastRenderedEntity = EntityId.none();
        inspectorContext.setInspectionScene(null);
    }

    /**
     * Draws prefab object inspectors and save UI for the given asset.
     *
     * @param context studio context
     * @param assets  asset database
     * @param asset   prefab asset to edit
     */
    public void render(StudioContext context, AssetDatabase assets, StudioAsset asset) {
        if (asset == null || context == null || context.isPlaying()) {
            return;
        }
        ensureLoaded(asset);
        if (document == null) {
            ImGui.textUnformatted("Failed to load prefab.");
            return;
        }

        inspectorContext.setStudioContext(context);
        inspectorContext.setInspectionScene(scratch.scene());

        ImGui.separator();
        if (ImGui.button("Save Prefab", -1f, 0f)) {
            syncScratchToDocument();
            save(asset.path());
        }
        if (dirty) {
            ImGui.sameLine();
            ImGui.textUnformatted("(unsaved)");
        }

        ImGui.separator();
        boolean first = true;
        for (GameObject object : scratch.listPrefabObjects()) {
            if (!first) {
                ImGui.separator();
            }
            first = false;
            renderObjectInspector(asset, object);
        }

        if (inspectorContext.consumeDirty()) {
            syncScratchToDocument();
            dirty = true;
        }
    }

    private void renderObjectInspector(StudioAsset asset, GameObject object) {
        EntityId entity = object.entity();
        if (!entity.equals(lastRenderedEntity)) {
            lastRenderedEntity = entity;
            NameComponent identity = object.getComponent(NameComponent.class);
            nameBuffer.set(identity == null ? "GameObject" : identity.name());
            tagBuffer.set(identity == null ? "" : identity.tag());
        }

        String objectKey = asset.guid() + ":" + entity.index() + ":" + entity.generation();

        ActiveComponent active = object.getComponent(ActiveComponent.class);
        boolean activeValue = active != null && active.selfActive;
        String previousName = nameBuffer.get();
        String previousTag = tagBuffer.get();
        boolean previousActive = activeValue;
        activeValue = InspectorObjectHeader.render(nameBuffer, tagBuffer, activeValue, active != null);
        object.setName(nameBuffer.get());
        object.setTag(tagBuffer.get());
        if (active != null) {
            active.selfActive = activeValue;
        }
        if (!previousName.equals(nameBuffer.get())
                || !previousTag.equals(tagBuffer.get())
                || previousActive != activeValue) {
            inspectorContext.markDirty();
        }

        var transformInfo = catalog.get(Transform2DComponent.class);
        Transform2DComponent transform = object.transform();
        if (transformInfo != null) {
            ComponentFoldout.State state = ComponentFoldout.header(
                    "prefab:" + objectKey + ":transform",
                    "Transform",
                    EditorIcons.COMPONENT,
                    true,
                    false
            );
            if (state.open()) {
                ComponentFoldout.beginBody();
                drawDrawer(transformInfo, transform);
                ComponentFoldout.endBody();
            }
        }

        for (ComponentTypeInfo info : catalog.all()) {
            if (info.hiddenInInspector() || info.type() == Transform2DComponent.class) {
                continue;
            }
            if (!object.hasComponent(info.type())) {
                continue;
            }
            Object component = object.getComponent(info.type());
            if (component == null) {
                continue;
            }
            String label = info.menuName();
            if (component instanceof org.llw.studio.scripting.ScriptComponent scriptContainer) {
                for (var attachment : scriptContainer.attachments) {
                    if (attachment.hasScriptReference()) {
                        label = nameBuffer.get() + " / " + inspectorContext.assets().displayName(attachment.scriptGuid);
                        break;
                    }
                }
            }
            ComponentFoldout.State state = ComponentFoldout.header(
                    "prefab:" + objectKey + ":" + info.type().getSimpleName(),
                    label,
                    EditorIcons.COMPONENT,
                    true,
                    false
            );
            if (state.open()) {
                ComponentFoldout.beginBody();
                drawDrawer(info, component);
                ComponentFoldout.endBody();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void drawDrawer(ComponentTypeInfo info, Object component) {
        ComponentDrawer<Object> drawer = (ComponentDrawer<Object>) info.drawer();
        drawer.draw(component, inspectorContext);
    }

    private void ensureLoaded(StudioAsset asset) {
        if (asset.guid().equals(loadedGuid) && document != null) {
            return;
        }
        loadedGuid = asset.guid();
        dirty = false;
        lastRenderedEntity = EntityId.none();
        try {
            document = (ObjectNode) MAPPER.readTree(asset.path().toFile());
            scratch.loadFromDocument(document);
        } catch (Exception ex) {
            document = null;
            inspectorContext.setInspectionScene(null);
        }
    }

    private void syncScratchToDocument() {
        if (document == null) {
            return;
        }
        scratch.writeToDocument(document);
    }

    private void save(Path path) {
        try {
            syncScratchToDocument();
            PrefabSerializer.saveDocument(path, document);
            dirty = false;
        } catch (Exception ignored) {
        }
    }
}
