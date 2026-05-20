package org.llw.studio.ecs.components;

/**
 * Clickable screen-space button with a centered caption.
 */
public final class UIButtonComponent implements Cloneable {
    public String label = "Button";
    public float width = 120f;
    public float height = 36f;
    public int fontSize = 16;
    public float r = 0.25f;
    public float g = 0.45f;
    public float b = 0.85f;
    public float a = 1f;
    public float hoverR = 0.35f;
    public float hoverG = 0.55f;
    public float hoverB = 0.95f;
    public float hoverA = 1f;
    public float pressedR = 0.15f;
    public float pressedG = 0.35f;
    public float pressedB = 0.75f;
    public float pressedA = 1f;
    public float textR = 1f;
    public float textG = 1f;
    public float textB = 1f;
    public float textA = 1f;
    public boolean interactable = true;

    /** Runtime: pointer is over the widget. */
    public transient boolean hovered;
    /** Runtime: primary button held while over the widget. */
    public transient boolean pressed;
    /** Runtime: released inside after press; cleared each frame after input. */
    public transient boolean clickedThisFrame;

    public UIButtonComponent copy() {
        UIButtonComponent copy = new UIButtonComponent();
        copy.label = label;
        copy.width = width;
        copy.height = height;
        copy.fontSize = fontSize;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.hoverR = hoverR;
        copy.hoverG = hoverG;
        copy.hoverB = hoverB;
        copy.hoverA = hoverA;
        copy.pressedR = pressedR;
        copy.pressedG = pressedG;
        copy.pressedB = pressedB;
        copy.pressedA = pressedA;
        copy.textR = textR;
        copy.textG = textG;
        copy.textB = textB;
        copy.textA = textA;
        copy.interactable = interactable;
        return copy;
    }

    @Override
    public UIButtonComponent clone() {
        return copy();
    }
}
