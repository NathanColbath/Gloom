package org.llw.studio.render;

import org.llw.math.vector.Vector2f;
import org.llw.render.core.Color;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Sprite;
import org.llw.render.renderables.Text;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.ui.UiDrawItem;
import org.llw.studio.ui.UiFontCache;
import org.llw.studio.ui.UiLayout;
import org.llw.studio.ui.UiLayoutContext;
import org.llw.studio.ui.UiSprites;
import org.llw.studio.ui.UiTextMetrics;
import org.llw.studio.ui.UiWidgetKind;

import java.util.List;

/**
 * Renders screen-space UI on top of the world in viewport pixel coordinates.
 */
public final class UiDrawPass {
    public static final int UI_LAYER_BASE = 100_000;
    private static final float TEXT_PADDING = 6f;
    private static final float CARET_WIDTH = 2f;

    private UiDrawPass() {
    }

    /**
     * @param scene     scene containing UI widgets
     * @param target    offscreen game view target
     * @param fonts     shared UI font cache; may be {@code null} to skip text
     * @param viewWidth game view width in pixels
     * @param viewHeight game view height in pixels
     */
    public static void draw(Scene scene, OffscreenTarget target, UiFontCache fonts, int viewWidth, int viewHeight) {
        draw(scene, target, fonts, UiLayoutContext.forViewport(target.getCamera(), viewWidth, viewHeight));
    }

    /**
     * @param scene scene containing UI widgets
     * @param target offscreen target
     * @param fonts shared UI font cache
     * @param ctx layout and projection context
     */
    public static void draw(Scene scene, OffscreenTarget target, UiFontCache fonts, UiLayoutContext ctx) {
        if (scene == null || target == null || ctx == null) {
            return;
        }
        int viewWidth = ctx.viewportWidth;
        int viewHeight = ctx.viewportHeight;
        if (viewWidth < 1 || viewHeight < 1) {
            return;
        }
        Camera2d camera = target.getCamera();
        Vector2f previousCenter = camera.getCenter();
        Vector2f previousSize = camera.getSize();
        camera.setCenter(viewWidth * 0.5f, viewHeight * 0.5f);
        camera.setSize(viewWidth, viewHeight);

        List<UiDrawItem> items = UiLayout.collect(scene, ctx);
        try {
            drawItems(target, fonts, items);
        } finally {
            camera.setCenter(previousCenter);
            camera.setSize(previousSize);
        }
    }

    /**
     * Draws a single canvas in reference resolution (UI Editor).
     */
    public static void drawCanvas(
            Scene scene,
            OffscreenTarget target,
            UiFontCache fonts,
            EntityId canvasEntity,
            UICanvasComponent canvas
    ) {
        if (scene == null || target == null || canvas == null || canvasEntity == null || canvasEntity.isNone()) {
            return;
        }
        int refW = Math.max(1, canvas.referenceWidth);
        int refH = Math.max(1, canvas.referenceHeight);
        draw(scene, target, fonts, UiLayoutContext.forAuthoring(canvasEntity, refW, refH));
    }

    private static void drawItems(OffscreenTarget target, UiFontCache fonts, List<UiDrawItem> items) {
        for (UiDrawItem item : items) {
            int layer = UI_LAYER_BASE + item.canvasSortingOrder * 1_000 + item.treeOrder;
            DrawState state = DrawState.DEFAULT.withLayer(layer);
            switch (item.kind) {
                case LABEL -> drawLabel(target, fonts, item.rect, item.label, state);
                case BUTTON -> drawButton(target, fonts, item.rect, item.button, state);
                case TOGGLE -> drawToggle(target, fonts, item.rect, item.toggle, state);
                case TEXT_FIELD -> drawTextField(target, fonts, item.rect, item.textField, state);
            }
        }
    }

    private static void drawLabel(OffscreenTarget target, UiFontCache fonts, org.llw.math.geometry.RectF rect, UILabelComponent label, DrawState state) {
        if (label == null) {
            return;
        }
        drawText(target, fonts, rect, label.text, label.fontSize, label.r, label.g, label.b, label.a, label.alignment, state);
    }

    private static void drawButton(OffscreenTarget target, UiFontCache fonts, org.llw.math.geometry.RectF rect, UIButtonComponent button, DrawState state) {
        if (button == null) {
            return;
        }
        float r = button.r;
        float g = button.g;
        float b = button.b;
        float a = button.a;
        if (button.pressed) {
            r = button.pressedR;
            g = button.pressedG;
            b = button.pressedB;
            a = button.pressedA;
        } else if (button.hovered) {
            r = button.hoverR;
            g = button.hoverG;
            b = button.hoverB;
            a = button.hoverA;
        }
        Sprite background = UiSprites.solidRect(rect.left, rect.top, rect.width, rect.height, r, g, b, a);
        target.draw(background, state);
        drawText(target, fonts, rect, button.label, button.fontSize, button.textR, button.textG, button.textB, button.textA, 1, state);
    }

    private static void drawToggle(OffscreenTarget target, UiFontCache fonts, org.llw.math.geometry.RectF rect, UIToggleComponent toggle, DrawState state) {
        if (toggle == null) {
            return;
        }
        float box = Math.min(toggle.boxSize, Math.min(rect.width, rect.height));
        float boxY = rect.top + (rect.height - box) * 0.5f;
        float boxR = toggle.isOn ? toggle.onR : toggle.r;
        float boxG = toggle.isOn ? toggle.onG : toggle.g;
        float boxB = toggle.isOn ? toggle.onB : toggle.b;
        float boxA = toggle.isOn ? toggle.onA : toggle.a;
        Sprite boxSprite = UiSprites.solidRect(rect.left, boxY, box, box, boxR, boxG, boxB, boxA);
        target.draw(boxSprite, state);

        org.llw.math.geometry.RectF labelRect = new org.llw.math.geometry.RectF(
                rect.left + box + TEXT_PADDING,
                rect.top,
                Math.max(1f, rect.width - box - TEXT_PADDING),
                rect.height
        );
        drawText(target, fonts, labelRect, toggle.label, toggle.fontSize, toggle.textR, toggle.textG, toggle.textB, toggle.textA, 0, state);
    }

    private static void drawTextField(OffscreenTarget target, UiFontCache fonts, org.llw.math.geometry.RectF rect, UITextFieldComponent field, DrawState state) {
        if (field == null) {
            return;
        }
        float borderAlpha = field.focused ? field.borderA : field.borderA * 0.6f;
        Sprite border = UiSprites.solidRect(
                rect.left, rect.top, rect.width, rect.height,
                field.borderR, field.borderG, field.borderB, borderAlpha);
        target.draw(border, state);
        Sprite background = UiSprites.solidRect(
                rect.left + 1f, rect.top + 1f, Math.max(1f, rect.width - 2f), Math.max(1f, rect.height - 2f),
                field.r, field.g, field.b, field.a);
        target.draw(background, state.withLayer(state.layer() + 1));

        String display = field.value == null || field.value.isEmpty() ? field.placeholder : field.value;
        boolean placeholder = field.value == null || field.value.isEmpty();
        float tr = placeholder ? field.placeholderR : field.textR;
        float tg = placeholder ? field.placeholderG : field.textG;
        float tb = placeholder ? field.placeholderB : field.textB;
        float ta = placeholder ? field.placeholderA : field.textA;
        org.llw.math.geometry.RectF inner = new org.llw.math.geometry.RectF(
                rect.left + TEXT_PADDING,
                rect.top,
                Math.max(1f, rect.width - TEXT_PADDING * 2f),
                rect.height
        );
        drawText(target, fonts, inner, display, field.fontSize, tr, tg, tb, ta, 0, state);

        if (field.focused && !placeholder && fonts != null) {
            Font font = fonts.font(field.fontSize);
            if (font != null) {
                float textWidth = UiTextMetrics.measureWidth(font, field.value);
                float caretX = inner.left + textWidth + 1f;
                float caretHeight = Math.min(inner.height - 4f, font.lineHeight());
                float caretY = inner.top + (inner.height - caretHeight) * 0.5f;
                Sprite caret = UiSprites.solidRect(caretX, caretY, CARET_WIDTH, caretHeight, field.textR, field.textG, field.textB, field.textA);
                target.draw(caret, state.withLayer(state.layer() + 2));
            }
        }
    }

    private static void drawText(
            OffscreenTarget target,
            UiFontCache fonts,
            org.llw.math.geometry.RectF rect,
            String content,
            int fontSize,
            float r,
            float g,
            float b,
            float a,
            int alignment,
            DrawState state
    ) {
        if (fonts == null || content == null || content.isEmpty()) {
            return;
        }
        Font font = fonts.font(fontSize);
        if (font == null) {
            return;
        }
        float textWidth = UiTextMetrics.measureWidth(font, content);
        float textX = rect.left + TEXT_PADDING;
        if (alignment == 1) {
            textX = rect.left + (rect.width - textWidth) * 0.5f;
        } else if (alignment == 2) {
            textX = rect.left + rect.width - textWidth - TEXT_PADDING;
        }
        float textY = rect.top + (rect.height - font.lineHeight()) * 0.5f;
        Text text = new Text(font);
        text.setContent(content);
        text.setPosition(textX, textY);
        text.setFillColor(new Color(
                Math.round(r * 255f),
                Math.round(g * 255f),
                Math.round(b * 255f),
                Math.round(a * 255f)
        ));
        target.draw(text, state);
    }
}
