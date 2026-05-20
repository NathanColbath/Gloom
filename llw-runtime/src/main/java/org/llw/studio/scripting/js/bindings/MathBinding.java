package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.math.util.MathUtils;
import org.llw.math.vector.Vector2f;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: math helpers exposed as {@code Math} and {@code Vec2}.
 */
public final class MathBinding {
    private MathBinding() {
    }

    /** Factory for mutable 2D vectors exposed to JavaScript. */
    public static final class Vec2Factory {
        /**
         * @param x X component
         * @param y Y component
         * @return new vector value
         */
        @HostAccess.Export
        public Vector2Value create(double x, double y) {
            return new Vector2Value((float) x, (float) y);
        }

        /**
         * @return zero vector
         */
        @HostAccess.Export
        public Vector2Value create() {
            return new Vector2Value(0f, 0f);
        }

        /**
         * @return zero vector
         */
        @HostAccess.Export
        public Vector2Value zero() {
            return new Vector2Value(0f, 0f);
        }

        /**
         * @return vector {@code (1, 1)}
         */
        @HostAccess.Export
        public Vector2Value one() {
            return new Vector2Value(1f, 1f);
        }

        /**
         * @param a first vector
         * @param b second vector
         * @return dot product
         */
        @HostAccess.Export
        public double dot(Vector2Value a, Vector2Value b) {
            return new Vector2f(a.x, a.y).dot(new Vector2f(b.x, b.y));
        }

        /**
         * @param a first vector
         * @param b second vector
         * @return distance between {@code a} and {@code b}
         */
        @HostAccess.Export
        public double distance(Vector2Value a, Vector2Value b) {
            return new Vector2f(a.x, a.y).distance(new Vector2f(b.x, b.y));
        }

        /**
         * @param a start vector
         * @param b end vector
         * @param t interpolation factor
         * @return new interpolated vector
         */
        @HostAccess.Export
        public Vector2Value lerp(Vector2Value a, Vector2Value b, double t) {
            float ft = (float) t;
            return new Vector2Value(
                    MathUtils.lerp(a.x, b.x, ft),
                    MathUtils.lerp(a.y, b.y, ft)
            );
        }

        /**
         * @param radians angle in radians
         * @param length  vector length (defaults to {@code 1} when {@code <= 0})
         * @return unit-direction vector at {@code radians}
         */
        @HostAccess.Export
        public Vector2Value fromAngle(double radians, double length) {
            float len = length <= 0.0 ? 1f : (float) length;
            return new Vector2Value(
                    (float) (Math.cos(radians) * len),
                    (float) (Math.sin(radians) * len)
            );
        }

        /**
         * @param degrees angle in degrees (same units as {@link org.llw.studio.ecs.components.Transform2DComponent#rotation})
         * @param length  vector length (defaults to {@code 1} when {@code <= 0})
         * @return direction vector for gameplay forward (+X at {@code 0°})
         */
        @HostAccess.Export
        public Vector2Value fromAngleDegrees(double degrees, double length) {
            return fromAngle(Math.toRadians(degrees), length);
        }
    }

    /** Mutable 2D vector host object. */
    public static final class Vector2Value {
        public float x;
        public float y;

        Vector2Value(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @HostAccess.Export
        public double getX() {
            return x;
        }

        @HostAccess.Export
        public void setX(double value) {
            x = (float) value;
        }

        @HostAccess.Export
        public double getY() {
            return y;
        }

        @HostAccess.Export
        public void setY(double value) {
            y = (float) value;
        }

        @HostAccess.Export
        public double length() {
            return new Vector2f(x, y).length();
        }

        @HostAccess.Export
        public Vector2Value normalize() {
            float len = new Vector2f(x, y).length();
            if (len > 1e-6f) {
                x /= len;
                y /= len;
            }
            return this;
        }

        @HostAccess.Export
        public Vector2Value add(double ox, double oy) {
            x += (float) ox;
            y += (float) oy;
            return this;
        }

        @HostAccess.Export
        public Vector2Value sub(double ox, double oy) {
            x -= (float) ox;
            y -= (float) oy;
            return this;
        }

        @HostAccess.Export
        public Vector2Value mul(double scalar) {
            x *= (float) scalar;
            y *= (float) scalar;
            return this;
        }

        @HostAccess.Export
        public double distance(double ox, double oy) {
            return new Vector2f(x, y).distance(new Vector2f((float) ox, (float) oy));
        }

        @HostAccess.Export
        public Vector2Value lerp(double ox, double oy, double t) {
            x = MathUtils.lerp(x, (float) ox, (float) t);
            y = MathUtils.lerp(y, (float) oy, (float) t);
            return this;
        }

        @HostAccess.Export
        public Vector2Value clone() {
            return new Vector2Value(x, y);
        }

        /**
         * @param other other vector
         * @return dot product with {@code other}
         */
        @HostAccess.Export
        public double dot(Vector2Value other) {
            return new Vector2f(x, y).dot(new Vector2f(other.x, other.y));
        }

        /**
         * @param other other vector
         * @return distance to {@code other}
         */
        @HostAccess.Export
        public double distanceTo(Vector2Value other) {
            return new Vector2f(x, y).distance(new Vector2f(other.x, other.y));
        }

        @HostAccess.Export
        public boolean equals(Vector2Value other) {
            if (other == null) {
                return false;
            }
            return x == other.x && y == other.y;
        }

        @HostAccess.Export
        public String toString() {
            return "Vector2f(" + x + ", " + y + ")";
        }
    }

    /** Scalar math functions exposed as {@code Math}. */
    public static final class Mathf {
        /**
         * @param value input value
         * @param min   minimum
         * @param max   maximum
         * @return clamped value
         */
        @HostAccess.Export
        public double clamp(double value, double min, double max) {
            return MathUtils.clamp((float) value, (float) min, (float) max);
        }

        /**
         * @param a start value
         * @param b end value
         * @param t interpolation factor
         * @return linear interpolation
         */
        @HostAccess.Export
        public double lerp(double a, double b, double t) {
            return MathUtils.lerp((float) a, (float) b, (float) t);
        }

        /**
         * @param a     range start
         * @param b     range end
         * @param value value within the range
         * @return normalized position in {@code [0, 1]}
         */
        @HostAccess.Export
        public double inverseLerp(double a, double b, double value) {
            return MathUtils.inverseLerp((float) a, (float) b, (float) value);
        }

        /**
         * @param t input in {@code [0, 1]}
         * @return smoothstep of {@code t}
         */
        @HostAccess.Export
        public double smoothstep(double t) {
            return MathUtils.smoothstep((float) t);
        }

        /**
         * @param a first value
         * @param b second value
         * @return smaller value
         */
        @HostAccess.Export
        public double min(double a, double b) {
            return Math.min(a, b);
        }

        /**
         * @param a first value
         * @param b second value
         * @return larger value
         */
        @HostAccess.Export
        public double max(double a, double b) {
            return Math.max(a, b);
        }

        /**
         * @param value input
         * @return absolute value
         */
        @HostAccess.Export
        public double abs(double value) {
            return Math.abs(value);
        }

        /**
         * @param value input
         * @return value rounded to the nearest integer
         */
        @HostAccess.Export
        public double round(double value) {
            return Math.round(value);
        }

        /**
         * @param value input
         * @return largest integer less than or equal to {@code value}
         */
        @HostAccess.Export
        public double floor(double value) {
            return Math.floor(value);
        }

        /**
         * @param value input
         * @return smallest integer greater than or equal to {@code value}
         */
        @HostAccess.Export
        public double ceil(double value) {
            return Math.ceil(value);
        }

        /**
         * @param value angle in radians
         * @return sine
         */
        @HostAccess.Export
        public double sin(double value) {
            return Math.sin(value);
        }

        /**
         * @param value angle in radians
         * @return cosine
         */
        @HostAccess.Export
        public double cos(double value) {
            return Math.cos(value);
        }

        /**
         * @param degrees angle in degrees
         * @return cosine
         */
        @HostAccess.Export
        public double cosDeg(double degrees) {
            return Math.cos(Math.toRadians(degrees));
        }

        /**
         * @param degrees angle in degrees
         * @return sine
         */
        @HostAccess.Export
        public double sinDeg(double degrees) {
            return Math.sin(Math.toRadians(degrees));
        }

        /**
         * @param y ordinate
         * @param x abscissa
         * @return arc tangent of {@code y/x}
         */
        @HostAccess.Export
        public double atan2(double y, double x) {
            return Math.atan2(y, x);
        }

        /**
         * @param value non-negative input
         * @return square root
         */
        @HostAccess.Export
        public double sqrt(double value) {
            return Math.sqrt(value);
        }

        /**
         * @param degrees angle in degrees
         * @return radians
         */
        @HostAccess.Export
        public double deg2rad(double degrees) {
            return Math.toRadians(degrees);
        }

        /**
         * @param radians angle in radians
         * @return degrees
         */
        @HostAccess.Export
        public double rad2deg(double radians) {
            return Math.toDegrees(radians);
        }

        /**
         * @return pseudorandom value in {@code [0, 1)}
         */
        @HostAccess.Export
        public double random() {
            return Math.random();
        }

        /**
         * @param min inclusive minimum
         * @param max exclusive maximum
         * @return random value in {@code [min, max)}
         */
        @HostAccess.Export
        public double randomRange(double min, double max) {
            return min + Math.random() * (max - min);
        }
    }
}
