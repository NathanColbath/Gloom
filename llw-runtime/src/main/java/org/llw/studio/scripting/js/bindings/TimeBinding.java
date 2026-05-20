package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.scripting.js.PlayClock;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: frame timing exposed to JavaScript as {@code Time}.
 */
public final class TimeBinding {
    /**
     * @return elapsed seconds for the current frame
     */
    @HostAccess.Export
    public double getDeltaTime() {
        return PlayClock.deltaTime();
    }

    /**
     * @return accumulated play time in seconds
     */
    @HostAccess.Export
    public double getTime() {
        return PlayClock.time();
    }

    /**
     * @return frames since play mode started
     */
    @HostAccess.Export
    public int getFrameCount() {
        return PlayClock.frameCount();
    }
}
