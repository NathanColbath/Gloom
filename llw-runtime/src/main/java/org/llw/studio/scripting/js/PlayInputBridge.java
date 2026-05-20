package org.llw.studio.scripting.js;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GLFW keyboard and mouse state sampled each play-mode frame for script {@code Input} bindings.
 */
public final class PlayInputBridge {
    private static long windowHandle;
    private static boolean enabled;
    private static final Map<Integer, Boolean> PREVIOUS_KEYS = new HashMap<>();
    private static final Set<Integer> ACTIVE_KEYS = new HashSet<>();
    private static final Map<Integer, Boolean> PREVIOUS_MOUSE = new HashMap<>();
    private static final Set<Integer> ACTIVE_MOUSE = new HashSet<>();
    private static double mouseX;
    private static double mouseY;
    private static double scrollX;
    private static double scrollY;

    private PlayInputBridge() {
    }

    /**
     * @param handle        native GLFW window handle
     * @param inputEnabled  when {@code false}, polling is skipped
     */
    public static void configure(long handle, boolean inputEnabled) {
        windowHandle = handle;
        enabled = inputEnabled;
    }

    /** Captures key, mouse, and cursor state for the current frame. */
    public static void beginFrame() {
        PREVIOUS_KEYS.clear();
        for (Integer key : ACTIVE_KEYS) {
            PREVIOUS_KEYS.put(key, true);
        }
        ACTIVE_KEYS.clear();
        PREVIOUS_MOUSE.clear();
        for (Integer button : ACTIVE_MOUSE) {
            PREVIOUS_MOUSE.put(button, true);
        }
        ACTIVE_MOUSE.clear();
        scrollX = 0d;
        scrollY = 0d;
        if (!enabled || windowHandle == 0L) {
            return;
        }
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(windowHandle, x, y);
        mouseX = x[0];
        mouseY = y[0];
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (GLFW.glfwGetKey(windowHandle, key) == GLFW.GLFW_PRESS) {
                ACTIVE_KEYS.add(key);
            }
        }
        for (int button = GLFW.GLFW_MOUSE_BUTTON_1; button <= GLFW.GLFW_MOUSE_BUTTON_LAST; button++) {
            if (GLFW.glfwGetMouseButton(windowHandle, button) == GLFW.GLFW_PRESS) {
                ACTIVE_MOUSE.add(button);
            }
        }
    }

    /**
     * @param dx horizontal scroll delta for this frame
     * @param dy vertical scroll delta for this frame
     */
    public static void addScroll(double dx, double dy) {
        scrollX += dx;
        scrollY += dy;
    }

    /**
     * @param key GLFW key code ({@code Keys.VK_*}) or legacy string name
     * @return {@code true} when the key is held this frame
     */
    public static boolean getKey(Object key) {
        Integer code = resolveKeyCode(key);
        return code != null && ACTIVE_KEYS.contains(code);
    }

    /**
     * @param key GLFW key code or legacy string name
     * @return {@code true} on the frame the key transitioned to pressed
     */
    public static boolean getKeyDown(Object key) {
        Integer code = resolveKeyCode(key);
        return code != null && ACTIVE_KEYS.contains(code) && !PREVIOUS_KEYS.getOrDefault(code, false);
    }

    /**
     * @param key GLFW key code or legacy string name
     * @return {@code true} on the frame the key transitioned to released
     */
    public static boolean getKeyUp(Object key) {
        Integer code = resolveKeyCode(key);
        return code != null && !ACTIVE_KEYS.contains(code) && PREVIOUS_KEYS.getOrDefault(code, false);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} when the button is held this frame
     */
    public static boolean getMouseButton(int button) {
        return ACTIVE_MOUSE.contains(button);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} on the frame the button transitioned to pressed
     */
    public static boolean getMouseButtonDown(int button) {
        return ACTIVE_MOUSE.contains(button) && !PREVIOUS_MOUSE.getOrDefault(button, false);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} on the frame the button transitioned to released
     */
    public static boolean getMouseButtonUp(int button) {
        return !ACTIVE_MOUSE.contains(button) && PREVIOUS_MOUSE.getOrDefault(button, false);
    }

    /**
     * @return cursor X in screen coordinates
     */
    public static double getMouseX() {
        return mouseX;
    }

    /**
     * @return cursor Y in screen coordinates
     */
    public static double getMouseY() {
        return mouseY;
    }

    /**
     * @return accumulated horizontal scroll for this frame
     */
    public static double getScrollX() {
        return scrollX;
    }

    /**
     * @return accumulated vertical scroll for this frame
     */
    public static double getScrollY() {
        return scrollY;
    }

    static Integer resolveKeyCode(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof Number number) {
            return number.intValue();
        }
        if (key instanceof String keyName) {
            return legacyKeyCode(keyName);
        }
        return null;
    }

  /** @deprecated use {@link org.llw.studio.scripting.setup.KeyCodes} / {@code Keys.VK_*} from TypeScript */
    private static Integer legacyKeyCode(String keyName) {
        return switch (keyName.toUpperCase()) {
            case "LEFT", "A" -> GLFW.GLFW_KEY_A;
            case "RIGHT", "D" -> GLFW.GLFW_KEY_D;
            case "UP", "W" -> GLFW.GLFW_KEY_W;
            case "DOWN", "S" -> GLFW.GLFW_KEY_S;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "CTRL", "CONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "LEFT_ARROW", "ARROWLEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT_ARROW", "ARROWRIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "UP_ARROW", "ARROWUP" -> GLFW.GLFW_KEY_UP;
            case "DOWN_ARROW", "ARROWDOWN" -> GLFW.GLFW_KEY_DOWN;
            default -> {
                if (keyName.length() == 1) {
                    yield (int) keyName.toUpperCase().charAt(0);
                }
                yield null;
            }
        };
    }
}
