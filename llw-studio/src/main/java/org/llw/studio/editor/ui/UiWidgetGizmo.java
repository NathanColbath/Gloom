package org.llw.studio.editor.ui;

import org.llw.math.geometry.RectF;
import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Sprite;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.render.UiDrawPass;
import org.llw.studio.ui.UiDrawItem;
import org.llw.studio.ui.UiSprites;

import java.util.List;

/** Selection outlines and resize handles in the UI Editor. */
public final class UiWidgetGizmo {
    private static final float HANDLE = 8f;
    private static final int GIZMO_LAYER = UiDrawPass.UI_LAYER_BASE + 500_000; // Above authored widgets so handles stay visible.

    private UiWidgetGizmo() {
    }

    /**
     * @param target   UI editor offscreen target
     * @param items    layout items in reference space
     * @param selected selected widget, or none
     */
    public static void draw(OffscreenTarget target, List<UiDrawItem> items, EntityId selected) {
        if (target == null || items == null) {
            return;
        }
        for (UiDrawItem item : items) {
            if (!item.entity.equals(selected)) {
                continue;
            }
            drawSelection(target, item.rect);
            return;
        }
    }

    private static void drawSelection(OffscreenTarget target, RectF rect) {
        DrawState state = DrawState.DEFAULT.withLayer(GIZMO_LAYER);
        float t = 2f;
        Sprite top = UiSprites.solidRect(rect.left, rect.top, rect.width, t, 0.4f, 0.75f, 1f, 1f);
        Sprite bottom = UiSprites.solidRect(rect.left, rect.bottom() - t, rect.width, t, 0.4f, 0.75f, 1f, 1f);
        Sprite left = UiSprites.solidRect(rect.left, rect.top, t, rect.height, 0.4f, 0.75f, 1f, 1f);
        Sprite right = UiSprites.solidRect(rect.right() - t, rect.top, t, rect.height, 0.4f, 0.75f, 1f, 1f);
        target.draw(top, state);
        target.draw(bottom, state);
        target.draw(left, state);
        target.draw(right, state);

        DrawState handleState = DrawState.DEFAULT.withLayer(GIZMO_LAYER + 1);
        Color handleColor = new Color(255, 255, 255, 220);
        target.draw(UiSprites.solidRect(rect.right() - HANDLE, rect.top + rect.height * 0.5f - HANDLE * 0.5f, HANDLE, HANDLE,
                handleColor.r / 255f, handleColor.g / 255f, handleColor.b / 255f, handleColor.a / 255f), handleState);
        target.draw(UiSprites.solidRect(rect.left + rect.width * 0.5f - HANDLE * 0.5f, rect.bottom() - HANDLE, HANDLE, HANDLE,
                handleColor.r / 255f, handleColor.g / 255f, handleColor.b / 255f, handleColor.a / 255f), handleState);
        target.draw(UiSprites.solidRect(rect.right() - HANDLE, rect.bottom() - HANDLE, HANDLE, HANDLE,
                handleColor.r / 255f, handleColor.g / 255f, handleColor.b / 255f, handleColor.a / 255f), handleState);
    }
}
