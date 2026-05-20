package org.llw.studio.ecs.components;

/**
 * Read-only screen-space text widget.
 */
public final class UILabelComponent implements Cloneable {
    public String text = "Label";
    public float width = 120f;
    public float height = 32f;
    public int fontSize = 16;
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;
    /** 0 = left, 1 = center, 2 = right */
    public int alignment;

    public UILabelComponent copy() {
        UILabelComponent copy = new UILabelComponent();
        copy.text = text;
        copy.width = width;
        copy.height = height;
        copy.fontSize = fontSize;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.alignment = alignment;
        return copy;
    }

    @Override
    public UILabelComponent clone() {
        return copy();
    }
}
