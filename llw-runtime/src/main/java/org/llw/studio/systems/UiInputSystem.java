package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scripting.js.PlayCameraBridge;
import org.llw.studio.scripting.js.PlayInputBridge;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.ui.UiDrawItem;
import org.llw.studio.ui.UiLayout;
import org.llw.studio.ui.UiWidgetKind;
import org.llw.studio.scene.Scene;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Routes pointer and keyboard input to screen-space UI widgets during play mode.
 */
public final class UiInputSystem implements EcsSystem {
    private static final int MOUSE_LEFT = GLFW.GLFW_MOUSE_BUTTON_1;

    private final Scene scene;
    private EntityId pressedButton = EntityId.none();

    public UiInputSystem(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void onUpdate(World world, float deltaTime) {
        clearTransientFlags(world);
        if (!PlayUiInputBridge.isGameViewFocused()) {
            syncTextFieldFocus(world);
            pressedButton = EntityId.none();
            return;
        }

        float mouseX = (float) PlayCameraBridge.getViewportMouseX();
        float mouseY = (float) PlayCameraBridge.getViewportMouseY();
        boolean insideView = mouseX >= 0f && mouseY >= 0f
                && mouseX <= PlayUiInputBridge.viewportWidth()
                && mouseY <= PlayUiInputBridge.viewportHeight();

        List<UiDrawItem> items = UiLayout.collect(scene);
        UiDrawItem topmost = insideView ? pickTopmost(items, mouseX, mouseY) : null;

        boolean mouseDown = PlayInputBridge.getMouseButton(MOUSE_LEFT);
        boolean mouseDownThisFrame = PlayInputBridge.getMouseButtonDown(MOUSE_LEFT);
        boolean mouseUpThisFrame = PlayInputBridge.getMouseButtonUp(MOUSE_LEFT);

        for (UiDrawItem item : items) {
            if (item.kind == UiWidgetKind.BUTTON && item.button != null) {
                item.button.hovered = insideView && item.rect.contains(mouseX, mouseY);
            }
            if (item.kind == UiWidgetKind.TOGGLE && item.toggle != null) {
                item.toggle.hovered = insideView && item.rect.contains(mouseX, mouseY);
            }
        }

        if (mouseDownThisFrame) {
            if (topmost != null && topmost.interactable()) {
                handlePress(world, topmost);
            } else {
                PlayUiInputBridge.setFocusedTextField(EntityId.none());
            }
        }

        if (mouseDown && !pressedButton.isNone() && world.isAlive(pressedButton)) {
            UIButtonComponent button = world.getComponent(pressedButton, UIButtonComponent.class);
            if (button != null) {
                button.pressed = true;
            }
        }

        if (mouseUpThisFrame) {
            if (!pressedButton.isNone() && world.isAlive(pressedButton)) {
                UIButtonComponent button = world.getComponent(pressedButton, UIButtonComponent.class);
                if (button != null) {
                    button.pressed = false;
                    if (topmost != null && topmost.entity.equals(pressedButton) && topmost.kind == UiWidgetKind.BUTTON) {
                        button.clickedThisFrame = true;
                    }
                }
            }
            pressedButton = EntityId.none();
        }

        handleKeyboard(world);
        syncTextFieldFocus(world);
    }

    private void clearTransientFlags(World world) {
        var buttons = world.store(UIButtonComponent.class);
        for (int i = 0; i < buttons.size(); i++) {
            UIButtonComponent button = buttons.componentAt(i);
            button.clickedThisFrame = false;
            button.hovered = false;
            button.pressed = false;
        }
        var toggles = world.store(UIToggleComponent.class);
        for (int i = 0; i < toggles.size(); i++) {
            toggles.componentAt(i).hovered = false;
        }
    }

    private void syncTextFieldFocus(World world) {
        EntityId focused = PlayUiInputBridge.focusedTextField();
        var fields = world.store(UITextFieldComponent.class);
        for (int i = 0; i < fields.size(); i++) {
            EntityId entity = fields.entityAt(i);
            UITextFieldComponent field = fields.componentAt(i);
            field.focused = entity.equals(focused);
        }
    }

    private void handlePress(World world, UiDrawItem item) {
        switch (item.kind) {
            case BUTTON -> {
                pressedButton = item.entity;
                UIButtonComponent button = item.button;
                if (button != null) {
                    button.pressed = true;
                }
            }
            case TOGGLE -> {
                UIToggleComponent toggle = item.toggle;
                if (toggle != null) {
                    toggle.isOn = !toggle.isOn;
                }
            }
            case TEXT_FIELD -> PlayUiInputBridge.setFocusedTextField(item.entity);
            default -> {
            }
        }
    }

    private void handleKeyboard(World world) {
        if (PlayUiInputBridge.wantCaptureKeyboard()) {
            return;
        }
        EntityId focused = PlayUiInputBridge.focusedTextField();
        if (focused.isNone() || !world.isAlive(focused)) {
            return;
        }
        UITextFieldComponent field = world.getComponent(focused, UITextFieldComponent.class);
        if (field == null || !field.interactable) {
            return;
        }
        if (PlayInputBridge.getKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
            PlayUiInputBridge.setFocusedTextField(EntityId.none());
            return;
        }
        if (PlayInputBridge.getKeyDown(GLFW.GLFW_KEY_BACKSPACE)) {
            if (field.value != null && !field.value.isEmpty()) {
                field.value = field.value.substring(0, field.value.length() - 1);
            }
            return;
        }
        if (PlayInputBridge.getKeyDown(GLFW.GLFW_KEY_ENTER)) {
            PlayUiInputBridge.setFocusedTextField(EntityId.none());
            return;
        }
        String entered = PlayUiInputBridge.enteredText();
        if (entered.isEmpty()) {
            return;
        }
        if (field.value == null) {
            field.value = "";
        }
        for (int i = 0; i < entered.length(); ) {
            int codePoint = entered.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint < 32) {
                continue;
            }
            if (field.value.length() >= field.maxLength) {
                break;
            }
            field.value += new String(Character.toChars(codePoint));
        }
    }

    private static UiDrawItem pickTopmost(List<UiDrawItem> items, float x, float y) {
        UiDrawItem hit = null;
        for (UiDrawItem item : items) {
            if (!item.rect.contains(x, y)) {
                continue;
            }
            hit = item;
        }
        return hit;
    }
}
