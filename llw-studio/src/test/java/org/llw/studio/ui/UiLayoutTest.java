package org.llw.studio.ui;

import org.junit.jupiter.api.Test;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiLayoutTest {
    @Test
    void collectAccumulatesScreenRectsFromHierarchy() {
        Scene scene = new Scene();
        GameObject canvasObject = scene.createGameObject("Canvas");
        canvasObject.addComponent(UICanvasComponent.class, new UICanvasComponent());
        canvasObject.transform().x = 10f;
        canvasObject.transform().y = 20f;

        GameObject labelObject = scene.createGameObject("Title");
        labelObject.setParent(canvasObject, false);
        labelObject.transform().x = 5f;
        labelObject.transform().y = 8f;
        UILabelComponent label = new UILabelComponent();
        label.width = 100f;
        label.height = 24f;
        labelObject.addComponent(UILabelComponent.class, label);

        GameObject buttonObject = scene.createGameObject("Play");
        buttonObject.setParent(canvasObject, false);
        buttonObject.transform().x = 5f;
        buttonObject.transform().y = 40f;
        UIButtonComponent button = new UIButtonComponent();
        button.width = 80f;
        button.height = 30f;
        buttonObject.addComponent(UIButtonComponent.class, button);

        List<UiDrawItem> items = UiLayout.collect(scene);
        assertEquals(2, items.size());
        UiDrawItem title = items.get(0);
        assertEquals(UiWidgetKind.LABEL, title.kind);
        assertEquals(15f, title.rect.left, 0.001f);
        assertEquals(28f, title.rect.top, 0.001f);
        assertEquals(100f, title.rect.width, 0.001f);

        UiDrawItem play = items.get(1);
        assertEquals(UiWidgetKind.BUTTON, play.kind);
        assertEquals(15f, play.rect.left, 0.001f);
        assertEquals(60f, play.rect.top, 0.001f);
        assertTrue(play.rect.contains(20f, 70f));
    }
}
