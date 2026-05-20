package org.llw.studio.ui;

import org.llw.math.geometry.RectF;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;

/** Flattened UI widget ready for rendering and hit testing. */
public final class UiDrawItem {
    public final EntityId entity;
    public final UiWidgetKind kind;
    public final int canvasSortingOrder;
    public final int treeOrder;
    public final RectF rect;
    public final UILabelComponent label;
    public final UIButtonComponent button;
    public final UIToggleComponent toggle;
    public final UITextFieldComponent textField;

    public UiDrawItem(
            EntityId entity,
            UiWidgetKind kind,
            int canvasSortingOrder,
            int treeOrder,
            RectF rect,
            UILabelComponent label,
            UIButtonComponent button,
            UIToggleComponent toggle,
            UITextFieldComponent textField
    ) {
        this.entity = entity;
        this.kind = kind;
        this.canvasSortingOrder = canvasSortingOrder;
        this.treeOrder = treeOrder;
        this.rect = rect;
        this.label = label;
        this.button = button;
        this.toggle = toggle;
        this.textField = textField;
    }

    public boolean interactable() {
        return switch (kind) {
            case BUTTON -> button != null && button.interactable;
            case TOGGLE -> toggle != null && toggle.interactable;
            case TEXT_FIELD -> textField != null && textField.interactable;
            case LABEL -> false;
        };
    }
}
