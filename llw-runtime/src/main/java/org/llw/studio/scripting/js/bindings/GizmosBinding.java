package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.render.core.Color;
import org.llw.studio.scripting.js.ScriptGizmoDrawBuffer;

/**
 * Script-facing {@code Gizmos} API for edit-mode scene overlays.
 */
public final class GizmosBinding {
    private ScriptGizmoDrawBuffer buffer;
    private float lineWorld;
    private Color current = Color.WHITE;

    /**
     * @param buffer    draw buffer for the current frame
     * @param lineWorld wire thickness in world units
     */
    public GizmosBinding(ScriptGizmoDrawBuffer buffer, float lineWorld) {
        this.buffer = buffer;
        this.lineWorld = lineWorld;
    }

    /** @param buffer draw buffer for the current scene-view frame */
    public void setBuffer(ScriptGizmoDrawBuffer buffer) {
        this.buffer = buffer;
    }

    /** @param lineWorld wire thickness in world units */
    public void setLineWorld(float lineWorld) {
        this.lineWorld = lineWorld;
    }

    /**
     * @param r red 0–1
     * @param g green 0–1
     * @param b blue 0–1
     * @param a alpha 0–1
     */
    @HostAccess.Export
    public void setColor(double r, double g, double b, double a) {
        current = new Color(
                clamp255(r),
                clamp255(g),
                clamp255(b),
                clamp255(a)
        );
    }

    @HostAccess.Export
    public void drawLine(double x1, double y1, double x2, double y2) {
        buffer.addLine((float) x1, (float) y1, (float) x2, (float) y2, current);
    }

    @HostAccess.Export
    public void drawWireCircle(double centerX, double centerY, double radius) {
        buffer.addWireCircle((float) centerX, (float) centerY, (float) radius, current, lineWorld);
    }

    @HostAccess.Export
    public void drawWireRect(double centerX, double centerY, double width, double height) {
        buffer.addWireRect((float) centerX, (float) centerY, (float) width, (float) height, current, lineWorld);
    }

    private static int clamp255(double value) {
        return Math.max(0, Math.min(255, Math.round((float) value * 255f)));
    }
}
