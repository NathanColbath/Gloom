package org.llw.math.interpolation;

import org.llw.math.util.MathUtils;

/**
 * Scalar interpolation utilities.
 */
public final class Interpolator {
    private Interpolator() {}

    /**
     * Linear interpolation between {@code a} and {@code b}.
     *
     * @param a start
     * @param b end
     * @param t factor in [0, 1]
     * @return interpolated value
     */
    public static float linear(float a, float b, float t) {
        return MathUtils.lerp(a, b, t);
    }

    /**
     * Smoothstep interpolation between {@code a} and {@code b}.
     *
     * @param a start
     * @param b end
     * @param t factor in [0, 1]
     * @return smoothed interpolated value
     */
    public static float smooth(float a, float b, float t) {
        return MathUtils.lerp(a, b, MathUtils.smoothstep(t));
    }
}
