package org.llw.studio.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import org.llw.math.geometry.RectF;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.commands.ComponentFieldEditCommand;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.scene.Scene;
import org.llw.studio.ui.UiDrawItem;
import org.llw.studio.ui.UiLayout;
import org.llw.studio.ui.UiLayoutContext;
import org.llw.studio.ui.UiWidgetKind;

import java.util.List;

/**
 * Edit-mode pointer handling for the UI Editor panel (select, move, resize widgets).
 */
public final class UiEditorInput {
    private static final float HANDLE_SIZE = 8f;
    private static final int HANDLE_RIGHT = 1;
    private static final int HANDLE_BOTTOM = 2;
    private static final int HANDLE_BOTTOM_RIGHT = 3;

    private final SelectionService selection;
    private final UndoStack undoStack;

    private EntityId dragEntity = EntityId.none();
    private Transform2DComponent dragTransformBefore;
    private UiWidgetKind dragKind = UiWidgetKind.LABEL;
    private UIButtonComponent buttonBefore;
    private UILabelComponent labelBefore;
    private UIToggleComponent toggleBefore;
    private UITextFieldComponent fieldBefore;
    private float dragStartMouseX;
    private float dragStartMouseY;
    private float dragStartX;
    private float dragStartY;
    private float dragStartWidth;
    private float dragStartHeight;
    private int activeHandle;
    private boolean dragging;

    public UiEditorInput(SelectionService selection, UndoStack undoStack) {
        this.selection = selection;
        this.undoStack = undoStack;
    }

    /**
     * Handles pointer input when the invisible hit region over the preview is hovered or active.
     *
     * @param scene        edit scene
     * @param canvasEntity active canvas
     * @param refWidth     reference layout width
     * @param refHeight    reference layout height
     * @param pointer      screen to layout coordinate mapping
     * @param imageMinX    preview top-left X
     * @param imageMinY    preview top-left Y
     */
    public void handle(
            Scene scene,
            EntityId canvasEntity,
            int refWidth,
            int refHeight,
            UiEditorPointer pointer,
            float imageMinX,
            float imageMinY
    ) {
        if (scene == null || canvasEntity == null || canvasEntity.isNone() || pointer == null) {
            return;
        }

        float mouseX = pointer.toLayoutX(ImGui.getMousePosX(), ImGui.getMousePosY(), imageMinX, imageMinY);
        float mouseY = pointer.toLayoutY(ImGui.getMousePosX(), ImGui.getMousePosY(), imageMinX, imageMinY);
        // Pointer coords are reference layout pixels, not stretched preview screen pixels.

        UiLayoutContext ctx = UiLayoutContext.forAuthoring(canvasEntity, refWidth, refHeight);
        List<UiDrawItem> items = UiLayout.collect(scene, ctx);
        UiDrawItem topmost = pickTopmost(items, mouseX, mouseY);

        boolean hovered = ImGui.isItemHovered();
        boolean active = ImGui.isItemActive();

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && hovered && !ImGui.getIO().getKeyCtrl()) {
            if (topmost != null) {
                int handle = hitHandle(topmost.rect, mouseX, mouseY);
                if (handle >= 0) {
                    selection.select(topmost.entity);
                    beginDrag(scene, topmost, mouseX, mouseY, handle);
                    dragging = true;
                }
            } else {
                selection.clear();
            }
        }

        if (dragging && active && ImGui.isMouseDown(ImGuiMouseButton.Left) && !dragEntity.isNone()) {
            updateDrag(scene, mouseX, mouseY);
        }

        if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            if (dragging) {
                endDrag(scene); // Push undo only on release so drag is one command, not per-frame steps.
            }
            dragging = false;
        }
    }

    private void beginDrag(Scene scene, UiDrawItem item, float mouseX, float mouseY, int handle) {
        dragEntity = item.entity;
        dragKind = item.kind;
        dragStartMouseX = mouseX;
        dragStartMouseY = mouseY;
        activeHandle = handle;
        Transform2DComponent transform = scene.world().getComponent(dragEntity, Transform2DComponent.class);
        if (transform != null) {
            dragTransformBefore = transform.copy();
            dragStartX = transform.x;
            dragStartY = transform.y;
        }
        buttonBefore = null;
        labelBefore = null;
        toggleBefore = null;
        fieldBefore = null;
        if (item.button != null) {
            buttonBefore = item.button.copy();
            dragStartWidth = buttonBefore.width;
            dragStartHeight = buttonBefore.height;
        } else if (item.label != null) {
            labelBefore = item.label.copy();
            dragStartWidth = labelBefore.width;
            dragStartHeight = labelBefore.height;
        } else if (item.toggle != null) {
            toggleBefore = item.toggle.copy();
            dragStartWidth = toggleBefore.width;
            dragStartHeight = toggleBefore.height;
        } else if (item.textField != null) {
            fieldBefore = item.textField.copy();
            dragStartWidth = fieldBefore.width;
            dragStartHeight = fieldBefore.height;
        }
    }

    private void updateDrag(Scene scene, float mouseX, float mouseY) {
        float dx = mouseX - dragStartMouseX;
        float dy = mouseY - dragStartMouseY;
        if (activeHandle == 0) {
            Transform2DComponent transform = scene.world().getComponent(dragEntity, Transform2DComponent.class);
            if (transform != null) {
                transform.x = dragStartX + dx;
                transform.y = dragStartY + dy;
            }
            return;
        }
        float newW = Math.max(8f, dragStartWidth + (activeHandle == HANDLE_RIGHT || activeHandle == HANDLE_BOTTOM_RIGHT ? dx : 0f));
        float newH = Math.max(8f, dragStartHeight + (activeHandle == HANDLE_BOTTOM || activeHandle == HANDLE_BOTTOM_RIGHT ? dy : 0f));
        applySize(scene, dragEntity, dragKind, newW, newH);
    }

    private void endDrag(Scene scene) {
        if (dragEntity.isNone() || !scene.world().isAlive(dragEntity)) {
            resetDrag();
            return;
        }
        Transform2DComponent afterTransform = scene.world().getComponent(dragEntity, Transform2DComponent.class);
        if (dragTransformBefore != null && afterTransform != null
                && (dragTransformBefore.x != afterTransform.x || dragTransformBefore.y != afterTransform.y)) {
            undoStack.execute(new ComponentFieldEditCommand<>(
                    scene,
                    dragEntity,
                    Transform2DComponent.class,
                    dragTransformBefore,
                    afterTransform.copy()
            ));
        }
        commitSizeUndo(scene);
        resetDrag();
    }

    private void commitSizeUndo(Scene scene) {
        switch (dragKind) {
            case BUTTON -> {
                if (buttonBefore == null) {
                    return;
                }
                UIButtonComponent after = scene.world().getComponent(dragEntity, UIButtonComponent.class);
                if (after != null && (after.width != buttonBefore.width || after.height != buttonBefore.height)) {
                    undoStack.execute(new ComponentFieldEditCommand<>(
                            scene, dragEntity, UIButtonComponent.class, buttonBefore, after.copy()));
                }
            }
            case LABEL -> {
                if (labelBefore == null) {
                    return;
                }
                UILabelComponent after = scene.world().getComponent(dragEntity, UILabelComponent.class);
                if (after != null && (after.width != labelBefore.width || after.height != labelBefore.height)) {
                    undoStack.execute(new ComponentFieldEditCommand<>(
                            scene, dragEntity, UILabelComponent.class, labelBefore, after.copy()));
                }
            }
            case TOGGLE -> {
                if (toggleBefore == null) {
                    return;
                }
                UIToggleComponent after = scene.world().getComponent(dragEntity, UIToggleComponent.class);
                if (after != null && (after.width != toggleBefore.width || after.height != toggleBefore.height)) {
                    undoStack.execute(new ComponentFieldEditCommand<>(
                            scene, dragEntity, UIToggleComponent.class, toggleBefore, after.copy()));
                }
            }
            case TEXT_FIELD -> {
                if (fieldBefore == null) {
                    return;
                }
                UITextFieldComponent after = scene.world().getComponent(dragEntity, UITextFieldComponent.class);
                if (after != null && (after.width != fieldBefore.width || after.height != fieldBefore.height)) {
                    undoStack.execute(new ComponentFieldEditCommand<>(
                            scene, dragEntity, UITextFieldComponent.class, fieldBefore, after.copy()));
                }
            }
            default -> {
            }
        }
    }

    private void resetDrag() {
        dragEntity = EntityId.none();
        dragTransformBefore = null;
        buttonBefore = null;
        labelBefore = null;
        toggleBefore = null;
        fieldBefore = null;
        activeHandle = 0;
        dragging = false;
    }

    private static int hitHandle(RectF rect, float x, float y) {
        if (x >= rect.right() - HANDLE_SIZE && x <= rect.right()
                && y >= rect.bottom() - HANDLE_SIZE && y <= rect.bottom()) {
            return HANDLE_BOTTOM_RIGHT;
        }
        if (x >= rect.right() - HANDLE_SIZE && x <= rect.right() && y >= rect.top && y <= rect.bottom()) {
            return HANDLE_RIGHT;
        }
        if (x >= rect.left && x <= rect.right() && y >= rect.bottom() - HANDLE_SIZE && y <= rect.bottom()) {
            return HANDLE_BOTTOM;
        }
        if (rect.contains(x, y)) {
            return 0;
        }
        return -1;
    }

    private static UiDrawItem pickTopmost(List<UiDrawItem> items, float x, float y) {
        UiDrawItem hit = null;
        for (UiDrawItem item : items) {
            if (item.rect.contains(x, y)) {
                hit = item; // UiLayout.collect order is back-to-front; last hit wins.
            }
        }
        return hit;
    }

    private static void applySize(Scene scene, EntityId entity, UiWidgetKind kind, float width, float height) {
        switch (kind) {
            case BUTTON -> {
                UIButtonComponent c = scene.world().getComponent(entity, UIButtonComponent.class);
                if (c != null) {
                    c.width = width;
                    c.height = height;
                }
            }
            case LABEL -> {
                UILabelComponent c = scene.world().getComponent(entity, UILabelComponent.class);
                if (c != null) {
                    c.width = width;
                    c.height = height;
                }
            }
            case TOGGLE -> {
                UIToggleComponent c = scene.world().getComponent(entity, UIToggleComponent.class);
                if (c != null) {
                    c.width = width;
                    c.height = height;
                }
            }
            case TEXT_FIELD -> {
                UITextFieldComponent c = scene.world().getComponent(entity, UITextFieldComponent.class);
                if (c != null) {
                    c.width = width;
                    c.height = height;
                }
            }
            default -> {
            }
        }
    }
}
