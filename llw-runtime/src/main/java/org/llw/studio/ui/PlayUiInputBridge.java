package org.llw.studio.ui;

import org.llw.studio.ecs.EntityId;

/**
 * Per-frame play-mode UI input state shared between the editor loop and {@link org.llw.studio.systems.UiInputSystem}.
 */
public final class PlayUiInputBridge {
    private static boolean gameViewFocused;
    private static boolean wantCaptureKeyboard;
    private static String enteredText = "";
    private static EntityId focusedTextField = EntityId.none();
    private static int viewportWidth = 1;
    private static int viewportHeight = 1;

    private PlayUiInputBridge() {
    }

    public static void reset() {
        gameViewFocused = false;
        wantCaptureKeyboard = false;
        enteredText = "";
        focusedTextField = EntityId.none();
        viewportWidth = 1;
        viewportHeight = 1;
    }

    public static void setGameViewFocused(boolean focused) {
        gameViewFocused = focused;
    }

    public static boolean isGameViewFocused() {
        return gameViewFocused;
    }

    public static void setWantCaptureKeyboard(boolean capture) {
        wantCaptureKeyboard = capture;
    }

    public static boolean wantCaptureKeyboard() {
        return wantCaptureKeyboard;
    }

    public static void setEnteredText(String text) {
        enteredText = text == null ? "" : text;
    }

    public static String enteredText() {
        return enteredText;
    }

    public static void setViewportSize(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
    }

    public static int viewportWidth() {
        return viewportWidth;
    }

    public static int viewportHeight() {
        return viewportHeight;
    }

    public static EntityId focusedTextField() {
        return focusedTextField;
    }

    public static void setFocusedTextField(EntityId entity) {
        focusedTextField = entity == null ? EntityId.none() : entity;
    }
}
