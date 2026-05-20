package org.llw.studio.ecs.components;

/**
 * Checkbox-style toggle with a caption.
 */
public final class UIToggleComponent implements Cloneable {
    public String label = "Toggle";
    public boolean isOn;
    public float width = 160f;
    public float height = 28f;
    public float boxSize = 18f;
    public int fontSize = 16;
    public float r = 0.2f;
    public float g = 0.2f;
    public float b = 0.2f;
    public float a = 1f;
    public float onR = 0.3f;
    public float onG = 0.7f;
    public float onB = 0.35f;
    public float onA = 1f;
    public float textR = 1f;
    public float textG = 1f;
    public float textB = 1f;
    public float textA = 1f;
    public boolean interactable = true;

    /** Runtime: pointer is over the widget. */
    public transient boolean hovered;

    public UIToggleComponent copy() {
        UIToggleComponent copy = new UIToggleComponent();
        copy.label = label;
        copy.isOn = isOn;
        copy.width = width;
        copy.height = height;
        copy.boxSize = boxSize;
        copy.fontSize = fontSize;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.onR = onR;
        copy.onG = onG;
        copy.onB = onB;
        copy.onA = onA;
        copy.textR = textR;
        copy.textG = textG;
        copy.textB = textB;
        copy.textA = textA;
        copy.interactable = interactable;
        return copy;
    }

    @Override
    public UIToggleComponent clone() {
        return copy();
    }
}
