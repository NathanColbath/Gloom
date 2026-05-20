package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.scripting.js.PlayInputBridge;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: keyboard and mouse input exposed to JavaScript as {@code Input}.
 */
public final class InputBinding {
    /**
     * @param key GLFW key code ({@code Keys.VK_*}) or legacy string name
     * @return {@code true} when the key is held
     */
    @HostAccess.Export
    public boolean getKey(Object key) {
        return PlayInputBridge.getKey(key);
    }

    /**
     * @param key GLFW key code or legacy string name
     * @return {@code true} on the pressed frame
     */
    @HostAccess.Export
    public boolean getKeyDown(Object key) {
        return PlayInputBridge.getKeyDown(key);
    }

    /**
     * @param key GLFW key code or legacy string name
     * @return {@code true} on the released frame
     */
    @HostAccess.Export
    public boolean getKeyUp(Object key) {
        return PlayInputBridge.getKeyUp(key);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} when the button is held
     */
    @HostAccess.Export
    public boolean getMouseButton(int button) {
        return PlayInputBridge.getMouseButton(button);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} on the pressed frame
     */
    @HostAccess.Export
    public boolean getMouseButtonDown(int button) {
        return PlayInputBridge.getMouseButtonDown(button);
    }

    /**
     * @param button GLFW mouse button index
     * @return {@code true} on the released frame
     */
    @HostAccess.Export
    public boolean getMouseButtonUp(int button) {
        return PlayInputBridge.getMouseButtonUp(button);
    }

    /**
     * @return cursor X in screen coordinates
     */
    @HostAccess.Export
    public double getMouseX() {
        return PlayInputBridge.getMouseX();
    }

    /**
     * @return cursor Y in screen coordinates
     */
    @HostAccess.Export
    public double getMouseY() {
        return PlayInputBridge.getMouseY();
    }

    /**
     * @return horizontal scroll delta this frame
     */
    @HostAccess.Export
    public double getScrollX() {
        return PlayInputBridge.getScrollX();
    }

    /**
     * @return vertical scroll delta this frame
     */
    @HostAccess.Export
    public double getScrollY() {
        return PlayInputBridge.getScrollY();
    }
}
