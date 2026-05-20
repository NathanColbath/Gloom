package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.scripting.js.PlayCameraBridge;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: global play camera exposed to JavaScript as {@code Camera}.
 */
public final class CameraBinding {
    private final ScriptContext context;
    private final ScriptHostApi hostApi;

    /**
     * @param context play-mode script context
     * @param hostApi host API for entity wrapping
     */
    public CameraBinding(ScriptContext context, ScriptHostApi hostApi) {
        this.context = context;
        this.hostApi = hostApi;
    }

    /**
     * @return {@code true} when play camera state has been synchronized
     */
    @HostAccess.Export
    public boolean isActive() {
        return PlayCameraBridge.isActive();
    }

    /**
     * @return wrapped main camera entity, or {@code null}
     */
    @HostAccess.Export
    public Object getMain() {
        if (!PlayCameraBridge.isActive() || PlayCameraBridge.mainCamera().isNone()) {
            return null;
        }
        return hostApi.wrapEntity(hostApi.createEntityBinding(context, PlayCameraBridge.mainCamera()));
    }

    /**
     * Play-mode 2D point returned from coordinate conversion helpers.
     */
    public static final class Vector2Result {
        /** X coordinate. */
        public final double x;
        /** Y coordinate. */
        public final double y;

        /**
         * @param x X coordinate
         * @param y Y coordinate
         */
        public Vector2Result(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return X coordinate
         */
        @HostAccess.Export
        public double getX() {
            return x;
        }

        /**
         * @return Y coordinate
         */
        @HostAccess.Export
        public double getY() {
            return y;
        }
    }
}
