/**
 * Scalar math helpers shared across the Gloom math library.
 */
package org.llw.math.util;

/**
 * Common scalar operations: clamping, interpolation, and approximate equality.
 */
public final class MathUtils {
    /** Default epsilon for {@link #approxEqual(float, float, float)}. */
    public static final float EPSILON = 1e-5f;

    private MathUtils() {}

    /**
     * Clamps {@code value} to the inclusive range [{@code min}, {@code max}].
     *
     * @param value input
     * @param min lower bound
     * @param max upper bound
     * @return clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation between {@code a} and {@code b}.
     *
     * @param a value at t=0
     * @param b value at t=1
     * @param t blend factor
     * @return {@code a + (b - a) * t}
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Returns the t that yields {@code value} when lerping from {@code a} to {@code b}.
     *
     * @param a range start
     * @param b range end
     * @param value value within the range
     * @return interpolation factor
     */
    public static float inverseLerp(float a, float b, float value) {
        if (MathUtils.approxEqual(a, b, EPSILON)) {
            return 0f;
        }
        return (value - a) / (b - a);
    }

    /**
     * Remaps {@code value} from [{@code inMin}, {@code inMax}] to [{@code outMin}, {@code outMax}].
     *
     * @param value input
     * @param inMin input range minimum
     * @param inMax input range maximum
     * @param outMin output range minimum
     * @param outMax output range maximum
     * @return remapped value
     */
    public static float remap(float value, float inMin, float inMax, float outMin, float outMax) {
        float t = inverseLerp(inMin, inMax, value);
        return lerp(outMin, outMax, t);
    }

    /**
     * Hermite smoothstep on [0, 1] after clamping {@code t}.
     *
     * @param t input in [0, 1]
     * @return smoothed value
     */
    public static float smoothstep(float t) {
        t = clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    /**
     * Returns whether {@code a} and {@code b} differ by at most {@code epsilon}.
     *
     * @param a first value
     * @param b second value
     * @param epsilon tolerance
     * @return {@code true} when approximately equal
     */
    public static boolean approxEqual(float a, float b, float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }
}
