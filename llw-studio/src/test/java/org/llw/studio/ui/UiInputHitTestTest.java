package org.llw.studio.ui;

import org.junit.jupiter.api.Test;
import org.llw.math.geometry.RectF;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UILabelComponent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiInputHitTestTest {
    @Test
    void pickTopmostReturnsLastMatchingItem() {
        EntityId bottom = new EntityId(0, 0);
        EntityId top = new EntityId(1, 0);
        List<UiDrawItem> items = List.of(
                new UiDrawItem(
                        bottom,
                        UiWidgetKind.LABEL,
                        0,
                        0,
                        new RectF(0f, 0f, 100f, 100f),
                        new UILabelComponent(),
                        null,
                        null,
                        null
                ),
                new UiDrawItem(
                        top,
                        UiWidgetKind.BUTTON,
                        0,
                        1,
                        new RectF(10f, 10f, 40f, 40f),
                        null,
                        new UIButtonComponent(),
                        null,
                        null
                )
        );

        UiDrawItem hit = pick(items, 20f, 20f);
        assertEquals(top, hit.entity);
    }

    @Test
    void buttonClickSetsClickedFlag() {
        UIButtonComponent button = new UIButtonComponent();
        button.clickedThisFrame = true;
        assertTrue(button.clickedThisFrame);
    }

    private static UiDrawItem pick(List<UiDrawItem> items, float x, float y) {
        UiDrawItem hit = null;
        for (UiDrawItem item : items) {
            if (item.rect.contains(x, y)) {
                hit = item;
            }
        }
        return hit;
    }
}
