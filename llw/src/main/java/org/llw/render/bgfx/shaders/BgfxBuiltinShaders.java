package org.llw.render.bgfx.shaders;

import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.bgfx.BGFX;

/**
 * Loads built-in bgfx shader programs used for fullscreen presentation and simple quads.
 */
public final class BgfxBuiltinShaders {
    private static final Logger log = Log.get(Loggers.GL);

    public static final int PROGRAM_SPRITE = 0;
    private static boolean loaded;

    private BgfxBuiltinShaders() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        log.debug("BgfxBuiltinShaders: using runtime shader handle {}", PROGRAM_SPRITE);
    }

    public static int spriteProgram() {
        return PROGRAM_SPRITE;
    }

    public static void dispose() {
        loaded = false;
    }
}
