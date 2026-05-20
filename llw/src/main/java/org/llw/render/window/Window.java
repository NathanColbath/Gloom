package org.llw.render.window;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.llw.math.vector.Vector2f;
import org.llw.render.core.IntSize;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

/**
 * GLFW-backed application window with an OpenGL 3.3 core profile context.
 */
public final class Window {
    private static final Logger log = Log.get(Loggers.WINDOW);

    private final long handle;
    private final WindowSettings settings;
    private final Deque<WindowEvent> events = new ArrayDeque<>();
    private final Set<Key> keysDown = new HashSet<>();
    private final Set<MouseButton> buttonsDown = new HashSet<>();
    private final Vector2f mousePosition = new Vector2f();
    private final StringBuilder textEntered = new StringBuilder();
    private final List<Path> droppedPaths = new ArrayList<>();
    private float scrollAccumX;
    private float scrollAccumY;
    private KeyModifiers activeMods = KeyModifiers.NONE;
    private boolean shouldClose;

    public Window(WindowSettings settings) {
        this.settings = settings;
        GLFWErrorCallback.create((error, description) ->
                log.error("GLFW error {}: {}", Integer.toString(error), description)
        ).set();
        if (!GLFW.glfwInit()) {
            log.error("Unable to initialize GLFW");
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, settings.resizable() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        handle = GLFW.glfwCreateWindow(settings.width(), settings.height(), settings.title(), MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            log.error("Failed to create GLFW window title={} size={}x{}", settings.title(), settings.width(), settings.height());
            throw new IllegalStateException("Failed to create GLFW window");
        }

        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (videoMode != null) {
            GLFW.glfwSetWindowPos(
                    handle,
                    (videoMode.width() - settings.width()) / 2,
                    (videoMode.height() - settings.height()) / 2
            );
        }

        registerCallbacks();
        GLFW.glfwShowWindow(handle);
        log.info("Window created title={} size={}x{} vsync={} resizable={}", settings.title(), settings.width(), settings.height(), settings.vsync(), settings.resizable());
        log.debug("GLFW window handle=0x{}", Long.toHexString(handle));
    }

    public long handle() {
        return handle;
    }

    public WindowSettings settings() {
        return settings;
    }

    public IntSize size() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(handle, width, height);
        return new IntSize(width[0], height[0]);
    }

    public boolean isOpen() {
        return !shouldClose && !GLFW.glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        shouldClose = true;
        GLFW.glfwSetWindowShouldClose(handle, true);
    }

    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(handle);
    }

    public Optional<WindowEvent> pollEvent() {
        return Optional.ofNullable(events.poll());
    }

    public boolean isKeyDown(Key key) {
        return keysDown.contains(key);
    }

    public boolean isMouseButtonDown(MouseButton button) {
        return buttonsDown.contains(button);
    }

    /**
     * Returns modifier keys currently held according to the most recent GLFW input callback.
     */
    public KeyModifiers activeModifiers() {
        return activeMods;
    }

    public Vector2f mousePosition() {
        return mousePosition.copy();
    }

    /**
     * Shows or hides the system cursor over this window.
     */
    public void setCursorVisible(boolean visible) {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, visible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN);
    }

    /**
     * Locks or unlocks the cursor for relative mouse movement (FPS-style look).
     */
    public void setCursorLocked(boolean locked) {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, locked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
    }

    /**
     * Returns a snapshot of all keys currently held down.
     */
    public Set<Key> pressedKeys() {
        return Set.copyOf(keysDown);
    }

    /**
     * Returns a snapshot of all mouse buttons currently held down.
     */
    public Set<MouseButton> pressedButtons() {
        return Set.copyOf(buttonsDown);
    }

    /**
     * Returns accumulated scroll offset since the last call and clears the accumulator.
     */
    public Vector2f takeScrollOffset() {
        Vector2f offset = new Vector2f(scrollAccumX, scrollAccumY);
        scrollAccumX = 0f;
        scrollAccumY = 0f;
        return offset;
    }

    /**
     * Returns text entered since the last call and clears the buffer.
     */
    public String takeEnteredText() {
        if (textEntered.isEmpty()) {
            return "";
        }
        String text = textEntered.toString();
        textEntered.setLength(0);
        return text;
    }

    /**
     * Returns filesystem paths from the most recent OS file drop on this window and clears the queue.
     * Drops are delivered by GLFW while the window has focus; consumers should call this once per frame.
     */
    public List<Path> takeDroppedPaths() {
        if (droppedPaths.isEmpty()) {
            return List.of();
        }
        List<Path> paths = List.copyOf(droppedPaths);
        droppedPaths.clear();
        return paths;
    }

    public void destroy() {
        log.info("Destroying window");
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private void registerCallbacks() {
        GLFW.glfwSetWindowCloseCallback(handle, (window) -> {
            shouldClose = true;
            events.add(new WindowEvent.Closed());
        });

        GLFW.glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            log.debug("Framebuffer resized to {}x{}", width, height);
            events.add(new WindowEvent.Resized(width, height));
        });

        GLFW.glfwSetWindowFocusCallback(handle, (window, focused) -> {
            if (focused) {
                events.add(new WindowEvent.FocusGained());
            } else {
                events.add(new WindowEvent.FocusLost());
            }
        });

        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            Key mapped = mapKey(key);
            KeyModifiers keyMods = KeyModifiers.fromGlfw(mods);
            activeMods = keyMods;
            if (action == GLFW.GLFW_PRESS) {
                keysDown.add(mapped);
                events.add(new WindowEvent.KeyPressed(mapped, keyMods, false));
                traceInput("KeyPressed {}", mapped);
            } else if (action == GLFW.GLFW_REPEAT) {
                keysDown.add(mapped);
                events.add(new WindowEvent.KeyPressed(mapped, keyMods, true));
                traceInput("KeyRepeated {}", mapped);
            } else if (action == GLFW.GLFW_RELEASE) {
                keysDown.remove(mapped);
                events.add(new WindowEvent.KeyReleased(mapped, keyMods));
                traceInput("KeyReleased {}", mapped);
            }
        });

        GLFW.glfwSetCharCallback(handle, (window, codepoint) -> {
            appendCodepoint(codepoint);
            events.add(new WindowEvent.TextEntered(codepoint));
            traceInput("TextEntered U+{}", Integer.toHexString(codepoint));
        });

        GLFW.glfwSetCursorPosCallback(handle, (window, x, y) -> {
            mousePosition.set((float) x, (float) y);
            events.add(new WindowEvent.MouseMoved(mousePosition.copy()));
            traceInput("MouseMoved {},{}", x, y);
        });

        GLFW.glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> {
            MouseButton mapped = mapMouseButton(button);
            KeyModifiers keyMods = KeyModifiers.fromGlfw(mods);
            activeMods = keyMods;
            Vector2f position = mousePosition.copy();
            if (action == GLFW.GLFW_PRESS) {
                buttonsDown.add(mapped);
                events.add(new WindowEvent.MouseButtonPressed(mapped, position, keyMods));
                traceInput("MouseButtonPressed {}", mapped);
            } else if (action == GLFW.GLFW_RELEASE) {
                buttonsDown.remove(mapped);
                events.add(new WindowEvent.MouseButtonReleased(mapped, position, keyMods));
                traceInput("MouseButtonReleased {}", mapped);
            }
        });

        GLFW.glfwSetScrollCallback(handle, (window, xOffset, yOffset) -> {
            scrollAccumX += (float) xOffset;
            scrollAccumY += (float) yOffset;
            events.add(new WindowEvent.MouseScrolled((float) xOffset, (float) yOffset));
            traceInput("MouseScrolled {},{}", xOffset, yOffset);
        });

        GLFW.glfwSetDropCallback(handle, (window, count, names) -> {
            for (int i = 0; i < count; i++) {
                long pathPointer = MemoryUtil.memGetAddress(names + ((long) i << 3));
                if (pathPointer == MemoryUtil.NULL) {
                    continue;
                }
                droppedPaths.add(Path.of(MemoryUtil.memUTF8(pathPointer)));
            }
            traceInput("FilesDropped count={}", count);
        });
    }

    private void appendCodepoint(int codepoint) {
        if (Character.isBmpCodePoint(codepoint)) {
            textEntered.append((char) codepoint);
        } else {
            textEntered.appendCodePoint(codepoint);
        }
    }

    private static void traceInput(String pattern, Object... args) {
        if (log.isTraceEnabled()) {
            log.trace(pattern, args);
        }
    }

    private static Key mapKey(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> Key.SPACE;
            case GLFW.GLFW_KEY_APOSTROPHE -> Key.APOSTROPHE;
            case GLFW.GLFW_KEY_COMMA -> Key.COMMA;
            case GLFW.GLFW_KEY_MINUS -> Key.MINUS;
            case GLFW.GLFW_KEY_PERIOD -> Key.PERIOD;
            case GLFW.GLFW_KEY_SLASH -> Key.SLASH;
            case GLFW.GLFW_KEY_0 -> Key.NUM_0;
            case GLFW.GLFW_KEY_1 -> Key.NUM_1;
            case GLFW.GLFW_KEY_2 -> Key.NUM_2;
            case GLFW.GLFW_KEY_3 -> Key.NUM_3;
            case GLFW.GLFW_KEY_4 -> Key.NUM_4;
            case GLFW.GLFW_KEY_5 -> Key.NUM_5;
            case GLFW.GLFW_KEY_6 -> Key.NUM_6;
            case GLFW.GLFW_KEY_7 -> Key.NUM_7;
            case GLFW.GLFW_KEY_8 -> Key.NUM_8;
            case GLFW.GLFW_KEY_9 -> Key.NUM_9;
            case GLFW.GLFW_KEY_SEMICOLON -> Key.SEMICOLON;
            case GLFW.GLFW_KEY_EQUAL -> Key.EQUAL;
            case GLFW.GLFW_KEY_A -> Key.A;
            case GLFW.GLFW_KEY_B -> Key.B;
            case GLFW.GLFW_KEY_C -> Key.C;
            case GLFW.GLFW_KEY_D -> Key.D;
            case GLFW.GLFW_KEY_E -> Key.E;
            case GLFW.GLFW_KEY_F -> Key.F;
            case GLFW.GLFW_KEY_G -> Key.G;
            case GLFW.GLFW_KEY_H -> Key.H;
            case GLFW.GLFW_KEY_I -> Key.I;
            case GLFW.GLFW_KEY_J -> Key.J;
            case GLFW.GLFW_KEY_K -> Key.K;
            case GLFW.GLFW_KEY_L -> Key.L;
            case GLFW.GLFW_KEY_M -> Key.M;
            case GLFW.GLFW_KEY_N -> Key.N;
            case GLFW.GLFW_KEY_O -> Key.O;
            case GLFW.GLFW_KEY_P -> Key.P;
            case GLFW.GLFW_KEY_Q -> Key.Q;
            case GLFW.GLFW_KEY_R -> Key.R;
            case GLFW.GLFW_KEY_S -> Key.S;
            case GLFW.GLFW_KEY_T -> Key.T;
            case GLFW.GLFW_KEY_U -> Key.U;
            case GLFW.GLFW_KEY_V -> Key.V;
            case GLFW.GLFW_KEY_W -> Key.W;
            case GLFW.GLFW_KEY_X -> Key.X;
            case GLFW.GLFW_KEY_Y -> Key.Y;
            case GLFW.GLFW_KEY_Z -> Key.Z;
            case GLFW.GLFW_KEY_LEFT_BRACKET -> Key.LEFT_BRACKET;
            case GLFW.GLFW_KEY_BACKSLASH -> Key.BACKSLASH;
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> Key.RIGHT_BRACKET;
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> Key.GRAVE_ACCENT;
            case GLFW.GLFW_KEY_ESCAPE -> Key.ESCAPE;
            case GLFW.GLFW_KEY_ENTER -> Key.ENTER;
            case GLFW.GLFW_KEY_TAB -> Key.TAB;
            case GLFW.GLFW_KEY_BACKSPACE -> Key.BACKSPACE;
            case GLFW.GLFW_KEY_INSERT -> Key.INSERT;
            case GLFW.GLFW_KEY_DELETE -> Key.DELETE;
            case GLFW.GLFW_KEY_RIGHT -> Key.RIGHT;
            case GLFW.GLFW_KEY_LEFT -> Key.LEFT;
            case GLFW.GLFW_KEY_DOWN -> Key.DOWN;
            case GLFW.GLFW_KEY_UP -> Key.UP;
            case GLFW.GLFW_KEY_PAGE_UP -> Key.PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN -> Key.PAGE_DOWN;
            case GLFW.GLFW_KEY_HOME -> Key.HOME;
            case GLFW.GLFW_KEY_END -> Key.END;
            case GLFW.GLFW_KEY_LEFT_SHIFT -> Key.LEFT_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL -> Key.LEFT_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT -> Key.LEFT_ALT;
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> Key.RIGHT_SHIFT;
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> Key.RIGHT_CONTROL;
            case GLFW.GLFW_KEY_RIGHT_ALT -> Key.RIGHT_ALT;
            case GLFW.GLFW_KEY_F1 -> Key.F1;
            case GLFW.GLFW_KEY_F2 -> Key.F2;
            case GLFW.GLFW_KEY_F3 -> Key.F3;
            case GLFW.GLFW_KEY_F4 -> Key.F4;
            case GLFW.GLFW_KEY_F5 -> Key.F5;
            case GLFW.GLFW_KEY_F6 -> Key.F6;
            case GLFW.GLFW_KEY_F7 -> Key.F7;
            case GLFW.GLFW_KEY_F8 -> Key.F8;
            case GLFW.GLFW_KEY_F9 -> Key.F9;
            case GLFW.GLFW_KEY_F10 -> Key.F10;
            case GLFW.GLFW_KEY_F11 -> Key.F11;
            case GLFW.GLFW_KEY_F12 -> Key.F12;
            case GLFW.GLFW_KEY_KP_0 -> Key.KP_0;
            case GLFW.GLFW_KEY_KP_1 -> Key.KP_1;
            case GLFW.GLFW_KEY_KP_2 -> Key.KP_2;
            case GLFW.GLFW_KEY_KP_3 -> Key.KP_3;
            case GLFW.GLFW_KEY_KP_4 -> Key.KP_4;
            case GLFW.GLFW_KEY_KP_5 -> Key.KP_5;
            case GLFW.GLFW_KEY_KP_6 -> Key.KP_6;
            case GLFW.GLFW_KEY_KP_7 -> Key.KP_7;
            case GLFW.GLFW_KEY_KP_8 -> Key.KP_8;
            case GLFW.GLFW_KEY_KP_9 -> Key.KP_9;
            case GLFW.GLFW_KEY_KP_DECIMAL -> Key.KP_DECIMAL;
            case GLFW.GLFW_KEY_KP_DIVIDE -> Key.KP_DIVIDE;
            case GLFW.GLFW_KEY_KP_MULTIPLY -> Key.KP_MULTIPLY;
            case GLFW.GLFW_KEY_KP_SUBTRACT -> Key.KP_SUBTRACT;
            case GLFW.GLFW_KEY_KP_ADD -> Key.KP_ADD;
            case GLFW.GLFW_KEY_KP_ENTER -> Key.KP_ENTER;
            case GLFW.GLFW_KEY_KP_EQUAL -> Key.KP_EQUAL;
            default -> Key.UNKNOWN;
        };
    }

    private static MouseButton mapMouseButton(int button) {
        return switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> MouseButton.LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.RIGHT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MouseButton.MIDDLE;
            case GLFW.GLFW_MOUSE_BUTTON_4 -> MouseButton.BUTTON_4;
            case GLFW.GLFW_MOUSE_BUTTON_5 -> MouseButton.BUTTON_5;
            case GLFW.GLFW_MOUSE_BUTTON_6 -> MouseButton.BUTTON_6;
            case GLFW.GLFW_MOUSE_BUTTON_7 -> MouseButton.BUTTON_7;
            case GLFW.GLFW_MOUSE_BUTTON_8 -> MouseButton.BUTTON_8;
            default -> MouseButton.MIDDLE;
        };
    }
}
