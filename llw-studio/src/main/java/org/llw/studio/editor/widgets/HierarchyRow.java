package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorDragDrop;
import org.llw.studio.editor.HierarchyEntityOrder;
import org.llw.studio.editor.prefab.PrefabEditorActions;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.scene.GameObject;

/**
 * One hierarchy tree row: selection, expand/collapse, reparent drag-drop, context menu, and active toggle.
 */
public final class HierarchyRow {
    private HierarchyRow() {
    }

    /**
     * Tells {@link HierarchyTree} whether to visit children and whether to call {@code ImGui.treePop()}.
     *
     * @param recurseChildren draw child nodes (e.g. filter matched a descendant)
     * @param treePopAfter    pop the tree node opened for this row
     */
    public record DrawResult(boolean recurseChildren, boolean treePopAfter) {
        public static DrawResult none() {
            return new DrawResult(false, false);
        }
    }

    public static DrawResult draw(
            StudioContext context,
            SelectionService selection,
            AssetDatabase assets,
            GameObject object,
            String filter
    ) {
        NameComponent name = object.getComponent(NameComponent.class);
        String label = name == null ? "GameObject" : name.name();
        boolean labelMatches = filter.isEmpty() || label.toLowerCase().contains(filter);
        if (!labelMatches) {
            // Keep branch open when a descendant matches so filtered tree stays connected.
            if (hasMatchingDescendant(object, filter)) {
                return new DrawResult(true, false);
            }
            return DrawResult.none();
        }

        boolean branch = !object.children().isEmpty();
        int flags = imgui.flag.ImGuiTreeNodeFlags.OpenOnArrow | imgui.flag.ImGuiTreeNodeFlags.SpanAvailWidth;
        if (!branch) {
            flags |= imgui.flag.ImGuiTreeNodeFlags.Leaf | imgui.flag.ImGuiTreeNodeFlags.NoTreePushOnOpen;
        }

        boolean selected = selection.isSelected(object.entity());
        if (selected) {
            flags |= imgui.flag.ImGuiTreeNodeFlags.Selected;
            EditorStyle.pushSelection();
        }

        ActiveComponent active = object.getComponent(ActiveComponent.class);
        if (active != null && !active.selfActive) {
            EditorStyle.pushMutedText();
        }

        String treeLabel = iconFor(object) + " " + label;
        boolean open = ImGui.treeNodeEx(treeLabel, flags);

        if (active != null && !active.selfActive) {
            EditorStyle.popMutedText();
        }
        if (selected) {
            EditorStyle.popSelection();
        }

        if (ImGui.isItemClicked() && !EditorDragDrop.shouldSuppressSelectionChange()) {
            boolean ctrl = ImGui.getIO().getKeyCtrl();
            boolean shift = ImGui.getIO().getKeyShift();
            boolean additive = ctrl && !shift;
            if (shift && !ctrl) {
                EntityId anchor = selection.rangeAnchor();
                if (anchor.isNone()) {
                    anchor = selection.selected();
                }
                if (!anchor.isNone()) {
                    selection.selectRange(
                            HierarchyEntityOrder.collect(context.activeScene()),
                            anchor,
                            object.entity()
                    );
                } else {
                    selection.select(object.entity());
                }
            } else if (EditorDragDrop.shouldDeferHierarchySelection(object.entity(), additive)) {
                EditorDragDrop.deferHierarchySelection(object.entity());
            } else {
                selection.toggleSelect(object.entity(), additive);
            }
            assets.clearSelection();
        }

        if (ImGui.beginPopupContextItem("hctx_" + object.entity().index())) {
            if (ImGui.menuItem("Create Child")) {
                GameObject child = context.editScene().createGameObject("GameObject");
                child.setParent(object, false);
                selection.select(child.entity());
            }
            if (ImGui.menuItem("Duplicate")) {
                GameObject dup = context.editScene().createGameObject(label);
                dup.transform().x = object.transform().x + 16f;
                dup.transform().y = object.transform().y + 16f;
                dup.setParent(object.parent(), false);
                selection.select(dup.entity());
            }
            if (ImGui.menuItem("Delete")) {
                context.editScene().world().destroyEntity(object.entity());
                selection.clear();
            }
            ImGui.endPopup();
        }

        if (ImGui.beginDragDropSource(ImGuiDragDropFlags.SourceAllowNullID)) {
            EditorDragDrop.markHierarchyDragFrame();
            ImGui.setDragDropPayload(SelectionService.PAYLOAD_ENTITY,
                    object.entity().index() + ":" + object.entity().generation());
            ImGui.text("Reparent " + label);
            ImGui.endDragDropSource();
        }
        if (ImGui.beginDragDropTarget()) {
            String payload = ImGui.acceptDragDropPayload(SelectionService.PAYLOAD_ENTITY, String.class);
            if (payload != null) {
                EntityId dragged = parseEntity(payload);
                GameObject draggedObject = context.editScene().find(dragged);
                if (draggedObject != null && !draggedObject.entity().equals(object.entity())) {
                    draggedObject.setParent(object, false);
                }
            }
            // Prefab asset drop instantiates under this object (edit mode only).
            String prefabGuid = ImGui.acceptDragDropPayload(AssetDatabase.PAYLOAD_ASSET_GUID, String.class);
            if (prefabGuid != null && !context.isPlaying()) {
                PrefabEditorActions.tryInstantiatePrefab(
                        context, assets, selection, prefabGuid, object, 0f, 0f, false, false);
            }
            ImGui.endDragDropTarget();
        }

        if (active != null) {
            // Active toggle pinned to right edge so it does not shift with tree indent.
            float checkboxX = ImGui.getWindowContentRegionMaxX() - 24f;
            ImGui.sameLine(checkboxX);
            boolean selfActive = active.selfActive;
            if (ImGui.checkbox("##active" + object.entity().index(), selfActive)) {
                active.selfActive = !selfActive;
            }
        }

        boolean needsPop = open && branch;
        return new DrawResult(needsPop, needsPop);
    }

    private static boolean hasMatchingDescendant(GameObject object, String filter) {
        for (GameObject child : object.children()) {
            NameComponent name = child.getComponent(NameComponent.class);
            String label = name == null ? "GameObject" : name.name();
            if (filter.isEmpty() || label.toLowerCase().contains(filter)) {
                return true;
            }
            if (hasMatchingDescendant(child, filter)) {
                return true;
            }
        }
        return false;
    }

    private static String iconFor(GameObject object) {
        if (object.getComponent(Camera2DComponent.class) != null) {
            return EditorIcons.CAMERA;
        }
        return EditorIcons.GAME_OBJECT;
    }

    private static EntityId parseEntity(String payload) {
        if (payload == null || !payload.contains(":")) {
            return EntityId.none();
        }
        String[] parts = payload.split(":");
        return new EntityId(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
