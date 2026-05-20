package org.llw.util.log;

import org.llw.render.core.IntSize;

/**
 * Throttled per-frame rendering statistics for DEBUG output.
 */
public final class FrameDiagnostics {
    private static final Logger LOG = Log.get(Loggers.GL);

    private static float intervalSec = 1.0f;
    private static float accumulator;
    private static int drawItems;
    private static int batchQuads;

    private FrameDiagnostics() {
    }

    public static void configure(float intervalSeconds) {
        intervalSec = Math.max(0.25f, intervalSeconds);
    }

    public static void recordDrawItems(int count) {
        drawItems += count;
    }

    public static void recordBatchQuads(int quads) {
        batchQuads += quads;
    }

    public static void tick(float dt, IntSize windowSize) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        accumulator += dt;
        if (accumulator < intervalSec) {
            return;
        }
        float fps = accumulator > 0f ? (1f / dt) : 0f;
        LOG.debug(
                "frame fps~={} window={}x{} drawItems={} batchQuads={}",
                String.format("%.0f", fps),
                windowSize.width(),
                windowSize.height(),
                drawItems,
                batchQuads
        );
        accumulator = 0f;
        drawItems = 0;
        batchQuads = 0;
    }
}
