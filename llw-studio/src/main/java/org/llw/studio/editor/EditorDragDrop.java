package org.llw.studio.editor;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import org.llw.studio.ecs.EntityId;

/**
 * Shared drag-and-drop state for editor UI that must not change selection or the inspector mid-drag.
 *
 * <p>Hierarchy drags keep the inspector on the entity selected before the gesture so entity-reference
 * fields on another object remain visible for drop targets.
 */
public final class EditorDragDrop {
    private static final float SELECTION_DRAG_THRESHOLD = 5f;

    private static boolean hierarchyDragThisFrame;
    private static boolean hierarchyDragLastFrame;
    private static int suppressHierarchyClickFrames;

    /** Selection when the left button went down this gesture (before hierarchy click handling). */
    private static EntityId selectionAtPointerDown = EntityId.none();
    private static EntityId pendingHierarchySelect = EntityId.none();
    private static boolean dragOccurredThisGesture;

    private EditorDragDrop() {
    }

    /**
     * Call once per editor frame before panels render.
     *
     * @param currentSelection {@link SelectionService#selected()} at frame start
     */
    public static void beginFrame(EntityId currentSelection) {
        if (isEntityPayloadActive()) {
            hierarchyDragThisFrame = true;
            dragOccurredThisGesture = true;
        }
        if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            selectionAtPointerDown = currentSelection;
            pendingHierarchySelect = EntityId.none();
            dragOccurredThisGesture = false;
        }
        if (ImGui.isMouseDragging(ImGuiMouseButton.Left, SELECTION_DRAG_THRESHOLD)) {
            dragOccurredThisGesture = true;
        }
        if (hierarchyDragLastFrame && !hierarchyDragThisFrame) {
            suppressHierarchyClickFrames = 2;
        }
        hierarchyDragLastFrame = hierarchyDragThisFrame;
        hierarchyDragThisFrame = false;
    }

    /**
     * Call once per editor frame after all panels have rendered.
     *
     * @param selection entity selection to apply deferred hierarchy picks
     */
    public static void endFrame(SelectionService selection) {
        if (suppressHierarchyClickFrames > 0) {
            suppressHierarchyClickFrames--;
        }
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            if (!pendingHierarchySelect.isNone() && !dragOccurredThisGesture) {
                selection.select(pendingHierarchySelect);
            }
            pendingHierarchySelect = EntityId.none();
            selectionAtPointerDown = EntityId.none();
            dragOccurredThisGesture = false;
        }
    }

    /** Marks that the hierarchy is the active entity drag source this frame. */
    public static void markHierarchyDragFrame() {
        hierarchyDragThisFrame = true;
        dragOccurredThisGesture = true;
    }

    /**
     * @param clicked  hierarchy row entity under the pointer
     * @param additive Ctrl-style multi-select
     * @return true when selection should wait until mouse release (avoids switching inspector before drag)
     */
    public static boolean shouldDeferHierarchySelection(EntityId clicked, boolean additive) {
        if (additive) {
            return false;
        }
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            return false;
        }
        if (selectionAtPointerDown.isNone()) {
            return false;
        }
        return !clicked.equals(selectionAtPointerDown);
    }

    /** Queues a hierarchy pick to apply on mouse release when the gesture was a click, not a drag. */
    public static void deferHierarchySelection(EntityId entity) {
        if (entity != null && !entity.isNone()) {
            pendingHierarchySelect = entity;
        }
    }

    /**
     * @param currentSelection live {@link SelectionService#selected()}
     * @return entity the inspector should display
     */
    public static EntityId inspectorEntity(EntityId currentSelection) {
        if (isHierarchyDragGesture()) {
            if (!selectionAtPointerDown.isNone()) {
                return selectionAtPointerDown;
            }
        }
        return currentSelection;
    }

    /** @return true while an ImGui drag-drop payload is active */
    public static boolean isActive() {
        return ImGui.getDragDropPayload() != null;
    }

    private static boolean isEntityPayloadActive() {
        return ImGui.getDragDropPayload(SelectionService.PAYLOAD_ENTITY) != null;
    }

    private static boolean isHierarchyDragGesture() {
        return isActive()
                || ImGui.isMouseDragging(ImGuiMouseButton.Left, SELECTION_DRAG_THRESHOLD)
                || hierarchyDragThisFrame
                || hierarchyDragLastFrame
                || suppressHierarchyClickFrames > 0
                || (!selectionAtPointerDown.isNone() && ImGui.isMouseDown(ImGuiMouseButton.Left)
                && dragOccurredThisGesture);
    }

    /**
     * @return true when hierarchy/project clicks should not alter the current selection or inspector
     */
    public static boolean shouldSuppressSelectionChange() {
        if (isActive()) {
            return true;
        }
        if (ImGui.isMouseDragging(ImGuiMouseButton.Left, SELECTION_DRAG_THRESHOLD)) {
            return true;
        }
        if (hierarchyDragThisFrame || hierarchyDragLastFrame) {
            return true;
        }
        if (suppressHierarchyClickFrames > 0) {
            return true;
        }
        if (!pendingHierarchySelect.isNone() && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            return true;
        }
        if (isHierarchyDragGesture() && !selectionAtPointerDown.isNone()) {
            return true;
        }
        return false;
    }
}
