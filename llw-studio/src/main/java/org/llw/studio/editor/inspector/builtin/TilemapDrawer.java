package org.llw.studio.editor.inspector.builtin;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TilesetDefinition;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.editor.widgets.fields.AssetReferenceField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;

/** Inspector fields for {@link TilemapComponent}. */
public final class TilemapDrawer implements ComponentDrawer<TilemapComponent> {
    private final ImInt activeLayerIndex = new ImInt(0);
    private final ImInt layerSortOrder = new ImInt(0);

    @Override
    public void draw(TilemapComponent component, InspectorContext context) {
        component.ensureDefaultLayer();
        String previousTexture = component.tilesetTextureGuid;
        component.tilesetTextureGuid = drawTextureField("Tileset", component.tilesetTextureGuid, context.assets());
        if (!component.tilesetTextureGuid.equals(previousTexture)) {
            syncCellSizeFromTileset(component, context.assets()); // New tileset drives cell grid size for paint math.
        }
        component.cellWidth = FloatField.draw("Cell Width", component.cellWidth);
        component.cellHeight = FloatField.draw("Cell Height", component.cellHeight);

        ImGui.separator();
        ImGui.text("Layers");
        if (activeLayerIndex.get() >= component.layers.size()) {
            activeLayerIndex.set(Math.max(0, component.layers.size() - 1));
        }
        syncActiveLayerToSession(context.editorSession(), component, activeLayerIndex.get());

        for (int i = 0; i < component.layers.size(); i++) {
            TilemapLayer layer = component.layers.get(i);
            boolean selected = activeLayerIndex.get() == i;
            if (ImGui.radioButton(layer.name + "##layer" + i, selected)) {
                activeLayerIndex.set(i);
                syncActiveLayerToSession(context.editorSession(), component, i); // Inspector layer drives scene paint tool.
            }
            ImGui.sameLine();
            ImGui.textDisabled(layer.enabled ? "" : "(hidden)");
        }
        if (ImGui.button("Add Layer")) {
            TilemapLayer layer = new TilemapLayer();
            layer.name = "Layer " + component.layers.size();
            layer.sortingOrder = component.layers.size();
            component.layers.add(layer);
            activeLayerIndex.set(component.layers.size() - 1);
            context.markDirty();
        }
        ImGui.sameLine();
        if (component.layers.size() > 1 && ImGui.button("Remove Layer")) {
            component.layers.remove(activeLayerIndex.get());
            activeLayerIndex.set(Math.min(activeLayerIndex.get(), component.layers.size() - 1));
            context.markDirty();
        }

        if (activeLayerIndex.get() < component.layers.size()) {
            TilemapLayer layer = component.layers.get(activeLayerIndex.get());
            ImString layerName = new ImString(layer.name == null ? "" : layer.name, 64);
            if (ImGui.inputText("Name", layerName)) {
                layer.name = layerName.get();
            }
            PropertyRow.begin("Enabled");
            layer.enabled = ImGui.checkbox("##layerEnabled", layer.enabled);
            PropertyRow.end();
            layerSortOrder.set(layer.sortingOrder);
            if (ImGui.inputInt("Sorting Order", layerSortOrder)) {
                layer.sortingOrder = layerSortOrder.get();
            }
            if (ImGui.button("Clear Layer")) {
                layer.cells.clear();
                context.markDirty();
            }
        }
        context.markDirty();
    }

    private static String drawTextureField(String label, String guid, AssetDatabase assets) {
        PropertyRow.begin(label);
        String display = guid == null || guid.isBlank() ? "None" : assets.displayName(guid);
        ImGui.textUnformatted(display);
        ImGui.sameLine();
        if (ImGui.smallButton("Clear##" + label)) {
            guid = "";
        }
        if (ImGui.beginDragDropTarget()) {
            String payload = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (payload != null) {
                StudioAsset asset = assets.get(payload);
                if (asset != null && asset.type() == AssetType.TEXTURE) {
                    guid = payload;
                }
            }
            ImGui.endDragDropTarget();
        }
        PropertyRow.end();
        return guid == null ? "" : guid;
    }

    private static void syncCellSizeFromTileset(TilemapComponent component, AssetDatabase assets) {
        TilesetDefinition tileset = assets.tileset(component.tilesetTextureGuid);
        if (tileset != null) {
            component.cellWidth = tileset.cellWidth;
            component.cellHeight = tileset.cellHeight;
        }
    }

    private static void syncActiveLayerToSession(EditorSession session, TilemapComponent component, int layerIndex) {
        if (session != null && session.tilemapEdit() != null) {
            session.tilemapEdit().activeLayerIndex = layerIndex; // Shared with TilemapPaintController via EditorSession.
        }
    }
}
