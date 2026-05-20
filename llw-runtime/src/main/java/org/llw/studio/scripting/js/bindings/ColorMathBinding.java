package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.math.util.MathUtils;

/**
 * Play-mode RGBA color values for script {@code new Color(r, g, b, a)}.
 */
public final class ColorMathBinding {
    private ColorMathBinding() {
    }

    /** Factory for mutable RGBA colors exposed to JavaScript. */
    public static final class ColorFactory {
        /**
         * @param r red in {@code [0, 1]}
         * @param g green in {@code [0, 1]}
         * @param b blue in {@code [0, 1]}
         * @param a alpha in {@code [0, 1]}
         * @return new color value
         */
        @HostAccess.Export
        public ColorValue create(double r, double g, double b, double a) {
            return new ColorValue((float) r, (float) g, (float) b, (float) a);
        }

        /**
         * @return opaque white
         */
        @HostAccess.Export
        public ColorValue create() {
            return new ColorValue(1f, 1f, 1f, 1f);
        }
    }

    /** Mutable RGBA color host object. */
    public static final class ColorValue {
        public float r;
        public float g;
        public float b;
        public float a;

        ColorValue(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @HostAccess.Export
        public double getR() {
            return r;
        }

        @HostAccess.Export
        public void setR(double value) {
            r = (float) value;
        }

        @HostAccess.Export
        public double getG() {
            return g;
        }

        @HostAccess.Export
        public void setG(double value) {
            g = (float) value;
        }

        @HostAccess.Export
        public double getB() {
            return b;
        }

        @HostAccess.Export
        public void setB(double value) {
            b = (float) value;
        }

        @HostAccess.Export
        public double getA() {
            return a;
        }

        @HostAccess.Export
        public void setA(double value) {
            a = (float) value;
        }

        @HostAccess.Export
        public ColorValue clone() {
            return new ColorValue(r, g, b, a);
        }

        @HostAccess.Export
        public ColorValue lerp(ColorValue other, double t) {
            float ft = (float) t;
            return new ColorValue(
                    MathUtils.lerp(r, other.r, ft),
                    MathUtils.lerp(g, other.g, ft),
                    MathUtils.lerp(b, other.b, ft),
                    MathUtils.lerp(a, other.a, ft)
            );
        }

        @HostAccess.Export
        public ColorValue multiply(double scalar) {
            float s = (float) scalar;
            r *= s;
            g *= s;
            b *= s;
            a *= s;
            return this;
        }
    }
}
