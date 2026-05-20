package org.llw.studio.ecs.components;

/**
 * Root for a screen-space UI tree in the Game view (positions are viewport pixels, Y-down).
 */
public final class UICanvasComponent implements Cloneable {
    /** Draw order among UI canvases (higher draws on top). */
    public int sortingOrder;
    /** When {@code false}, the canvas and its descendants are not drawn or hit-tested. */
    public boolean enabled = true;

    public UICanvasComponent copy() {
        UICanvasComponent copy = new UICanvasComponent();
        copy.sortingOrder = sortingOrder;
        copy.enabled = enabled;
        return copy;
    }

    @Override
    public UICanvasComponent clone() {
        return copy();
    }
}
