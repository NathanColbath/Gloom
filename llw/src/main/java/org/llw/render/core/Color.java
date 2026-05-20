package org.llw.render.core;

/**
 * Immutable RGBA color with 8-bit channel values in the range [0, 255].
 *
 * <p>Constructors clamp out-of-range components. Normalized accessors divide by 255 for
 * use in shaders and floating-point color math.
 */
public final class Color {
    /** Opaque black ({@code 0, 0, 0}). */
    public static final Color BLACK = new Color(0, 0, 0);
    /** Opaque white ({@code 255, 255, 255}). */
    public static final Color WHITE = new Color(255, 255, 255);
    /** Opaque red ({@code 255, 0, 0}). */
    public static final Color RED = new Color(255, 0, 0);
    /** Opaque green ({@code 0, 255, 0}). */
    public static final Color GREEN = new Color(0, 255, 0);
    /** Opaque blue ({@code 0, 0, 255}). */
    public static final Color BLUE = new Color(0, 0, 255);
    /** Fully transparent black ({@code 0, 0, 0, 0}). */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /** Red channel in [0, 255]. */
    public final int r;
    /** Green channel in [0, 255]. */
    public final int g;
    /** Blue channel in [0, 255]. */
    public final int b;
    /** Alpha channel in [0, 255]. */
    public final int a;

    /**
     * Creates an opaque color from RGB components (alpha defaults to 255).
     *
     * @param r red channel [0, 255], clamped if out of range
     * @param g green channel [0, 255], clamped if out of range
     * @param b blue channel [0, 255], clamped if out of range
     */
    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Creates a color from RGBA components.
     *
     * @param r red channel [0, 255], clamped if out of range
     * @param g green channel [0, 255], clamped if out of range
     * @param b blue channel [0, 255], clamped if out of range
     * @param a alpha channel [0, 255], clamped if out of range
     */
    public Color(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    /**
     * Returns a new color with the same RGB and the given alpha.
     *
     * @param alpha alpha channel [0, 255], clamped if out of range
     * @return new color with updated alpha
     */
    public Color withAlpha(int alpha) {
        return new Color(r, g, b, alpha);
    }

    /**
     * Returns the red channel as a float in [0, 1].
     *
     * @return {@code r / 255f}
     */
    public float rNorm() {
        return r / 255f;
    }

    /**
     * Returns the green channel as a float in [0, 1].
     *
     * @return {@code g / 255f}
     */
    public float gNorm() {
        return g / 255f;
    }

    /**
     * Returns the blue channel as a float in [0, 1].
     *
     * @return {@code b / 255f}
     */
    public float bNorm() {
        return b / 255f;
    }

    /**
     * Returns the alpha channel as a float in [0, 1].
     *
     * @return {@code a / 255f}
     */
    public float aNorm() {
        return a / 255f;
    }

    /**
     * Packs this color into a 32-bit ARGB integer (alpha in the high byte).
     *
     * @return {@code (a << 24) | (b << 16) | (g << 8) | r}
     */
    public int toRgbaBits() {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Returns a string of the form {@code Color(r, g, b, a)}.
     *
     * @return human-readable representation of this color
     */
    @Override
    public String toString() {
        return "Color(" + r + ", " + g + ", " + b + ", " + a + ")";
    }
}
