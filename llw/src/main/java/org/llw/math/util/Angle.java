package org.llw.math.util;

/**
 * Angle conversion helpers. All trigonometry in the math library uses radians unless stated.
 */
public final class Angle {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    private Angle() {}

    /**
     * Converts degrees to radians.
     *
     * @param degrees angle in degrees
     * @return radians
     */
    public static float toRadians(float degrees) {
        return degrees * DEG_TO_RAD;
    }

    /**
     * Converts radians to degrees.
     *
     * @param radians angle in radians
     * @return degrees
     */
    public static float toDegrees(float radians) {
        return radians * RAD_TO_DEG;
    }

    /**
     * Wraps {@code radians} to the range [-π, π].
     *
     * @param radians input angle
     * @return wrapped angle
     */
    public static float wrapRadians(float radians) {
        float twoPi = (float) (2.0 * Math.PI);
        radians %= twoPi;
        if (radians > Math.PI) {
            radians -= twoPi;
        } else if (radians < -Math.PI) {
            radians += twoPi;
        }
        return radians;
    }
}
