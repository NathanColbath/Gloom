package org.llw.studio.ecs.components;

/**
 * Editable single-line text field in screen space.
 */
public final class UITextFieldComponent implements Cloneable {
    public String value = "";
    public String placeholder = "Enter text...";
    public float width = 200f;
    public float height = 32f;
    public int fontSize = 16;
    public int maxLength = 128;
    public float r = 0.12f;
    public float g = 0.12f;
    public float b = 0.12f;
    public float a = 1f;
    public float borderR = 0.45f;
    public float borderG = 0.45f;
    public float borderB = 0.45f;
    public float borderA = 1f;
    public float textR = 1f;
    public float textG = 1f;
    public float textB = 1f;
    public float textA = 1f;
    public float placeholderR = 0.55f;
    public float placeholderG = 0.55f;
    public float placeholderB = 0.55f;
    public float placeholderA = 1f;
    public boolean interactable = true;

    /** Runtime: receives keyboard input this frame. */
    public transient boolean focused;

    public UITextFieldComponent copy() {
        UITextFieldComponent copy = new UITextFieldComponent();
        copy.value = value;
        copy.placeholder = placeholder;
        copy.width = width;
        copy.height = height;
        copy.fontSize = fontSize;
        copy.maxLength = maxLength;
        copy.r = r;
        copy.g = g;
        copy.b = b;
        copy.a = a;
        copy.borderR = borderR;
        copy.borderG = borderG;
        copy.borderB = borderB;
        copy.borderA = borderA;
        copy.textR = textR;
        copy.textG = textG;
        copy.textB = textB;
        copy.textA = textA;
        copy.placeholderR = placeholderR;
        copy.placeholderG = placeholderG;
        copy.placeholderB = placeholderB;
        copy.placeholderA = placeholderA;
        copy.interactable = interactable;
        return copy;
    }

    @Override
    public UITextFieldComponent clone() {
        return copy();
    }
}
