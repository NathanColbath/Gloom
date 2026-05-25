package org.llw.studio.ecs.components;

import org.llw.studio.ui.UiCanvasRenderMode;

/**
 * Root for a UI tree in the Game view (Y-down). Screen-space mode uses viewport pixels;
 * world-space mode follows the canvas entity world transform.
 */
public final class UICanvasComponent implements Cloneable {
    /** Draw order among UI canvases (higher draws on top). */
    public int sortingOrder;
    /** When {@code false}, the canvas and its descendants are not drawn or hit-tested. */
    public boolean enabled = true;
    /** How widget positions are interpreted at runtime. */
    public UiCanvasRenderMode renderMode = UiCanvasRenderMode.SCREEN_SPACE;
    /** Reference resolution width for the UI Editor (pixels). */
    public int referenceWidth = 1920;
    /** Reference resolution height for the UI Editor (pixels). */
    public int referenceHeight = 1080;

    public UICanvasComponent copy() {
        UICanvasComponent copy = new UICanvasComponent();
        copy.sortingOrder = sortingOrder;
        copy.enabled = enabled;
        copy.renderMode = renderMode;
        copy.referenceWidth = referenceWidth;
        copy.referenceHeight = referenceHeight;
        return copy;
    }

    @Override
    public UICanvasComponent clone() {
        return copy();
    }
}
