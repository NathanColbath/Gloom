package org.llw.studio.scripting.js;

import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Vertex;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.VertexGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects gizmo primitives from script {@code Gizmos.*} calls for a single scene-view frame.
 */
public final class ScriptGizmoDrawBuffer {
    public static final int MAX_PRIMITIVES = 500;

    private final List<LinePrimitive> lines = new ArrayList<>();
    private final List<CirclePrimitive> circles = new ArrayList<>();
    private final List<RectPrimitive> rects = new ArrayList<>();
    private boolean budgetExceeded;

    /** Clears primitives accumulated for the previous frame. */
    public void clear() {
        lines.clear();
        circles.clear();
        rects.clear();
        budgetExceeded = false;
    }

    /** @return whether the per-frame primitive budget was exceeded */
    public boolean budgetExceeded() {
        return budgetExceeded;
    }

    public void addLine(float x1, float y1, float x2, float y2, Color color) {
        if (!checkBudget()) {
            return;
        }
        lines.add(new LinePrimitive(x1, y1, x2, y2, color));
    }

    public void addWireCircle(float cx, float cy, float radius, Color color, float thickness) {
        if (!checkBudget()) {
            return;
        }
        circles.add(new CirclePrimitive(cx, cy, radius, color, thickness));
    }

    public void addWireRect(float cx, float cy, float width, float height, Color color, float lineWorld) {
        if (!checkBudget()) {
            return;
        }
        rects.add(new RectPrimitive(cx, cy, width, height, color, lineWorld));
    }

    /**
     * @param target    offscreen scene target
     * @param drawState layer and blend state
     * @param lineWorld default line thickness in world units
     */
    public void flush(OffscreenTarget target, DrawState drawState, float lineWorld) {
        for (LinePrimitive line : lines) {
            drawLineBar(target, drawState, line, lineWorld);
        }
        for (CirclePrimitive circle : circles) {
            Circle shape = new Circle();
            shape.setPosition(circle.cx, circle.cy);
            shape.setRadius(Math.max(1f, circle.radius));
            shape.setFilled(false);
            shape.setOutlined(true);
            shape.setOutlineColor(circle.color);
            shape.setOutlineThickness(Math.max(1f, circle.thickness));
            target.draw(shape, drawState);
        }
        for (RectPrimitive rect : rects) {
            float halfW = rect.width * 0.5f;
            float halfH = rect.height * 0.5f;
            float x = rect.cx - halfW;
            float y = rect.cy - halfH;
            float line = rect.lineWorld;
            drawRectBar(target, drawState, x, y, rect.width, line, rect.color);
            drawRectBar(target, drawState, x, y + rect.height - line, rect.width, line, rect.color);
            drawRectBar(target, drawState, x, y, line, rect.height, rect.color);
            drawRectBar(target, drawState, x + rect.width - line, y, line, rect.height, rect.color);
        }
    }

    private boolean checkBudget() {
        int count = lines.size() + circles.size() + rects.size();
        if (count >= MAX_PRIMITIVES) {
            budgetExceeded = true;
            return false;
        }
        return true;
    }

    private static void drawLineBar(OffscreenTarget target, DrawState state, LinePrimitive line, float lineWorld) {
        float dx = line.x2 - line.x1;
        float dy = line.y2 - line.y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) {
            return;
        }
        float nx = -dy / len * lineWorld * 0.5f;
        float ny = dx / len * lineWorld * 0.5f;
        drawRectBar(target, state, line.x1 + nx, line.y1 + ny, len, lineWorld, line.color);
    }

    private static void drawRectBar(
            OffscreenTarget target,
            DrawState state,
            float x,
            float y,
            float width,
            float height,
            Color color
    ) {
        Rectangle bar = new Rectangle();
        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setFillColor(color);
        target.draw(bar, state);
    }

    private record LinePrimitive(float x1, float y1, float x2, float y2, Color color) {
    }

    private record CirclePrimitive(float cx, float cy, float radius, Color color, float thickness) {
    }

    private record RectPrimitive(float cx, float cy, float width, float height, Color color, float lineWorld) {
    }
}
