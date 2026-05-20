package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.math.geometry.RectF;

/**
 * Play-mode axis-aligned rectangle for script {@code new Rect2(x, y, width, height)}.
 *
 * <p>{@code x} and {@code y} map to {@link RectF#left} and {@link RectF#top} in Y-down space.
 */
public final class Rect2Binding {
    private Rect2Binding() {
    }

    /** Factory for {@link Rect2Value} instances. */
    public static final class Rect2Factory {
        /**
         * @param x      left edge
         * @param y      top edge
         * @param width  horizontal extent
         * @param height vertical extent
         * @return new rectangle value
         */
        @HostAccess.Export
        public Rect2Value create(double x, double y, double width, double height) {
            return new Rect2Value((float) x, (float) y, (float) width, (float) height);
        }

        /**
         * @return zero rectangle at the origin
         */
        @HostAccess.Export
        public Rect2Value create() {
            return new Rect2Value(0f, 0f, 0f, 0f);
        }
    }

    /** Mutable rectangle host object. */
    public static final class Rect2Value {
        public float x;
        public float y;
        public float width;
        public float height;

        Rect2Value(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private RectF rect() {
            return new RectF(x, y, width, height);
        }

        @HostAccess.Export
        public double getRight() {
            return rect().right();
        }

        @HostAccess.Export
        public double getBottom() {
            return rect().bottom();
        }

        @HostAccess.Export
        public boolean contains(double px, double py) {
            return rect().contains((float) px, (float) py);
        }

        @HostAccess.Export
        public boolean intersects(Rect2Value other) {
            return rect().intersects(other.rect());
        }

        /**
         * @param other other rectangle
         * @return intersection rectangle, or {@code null} when disjoint
         */
        @HostAccess.Export
        public Rect2Value intersection(Rect2Value other) {
            RectF result = rect().intersection(other.rect());
            if (result == null) {
                return null;
            }
            return new Rect2Value(result.left, result.top, result.width, result.height);
        }

        @HostAccess.Export
        public Rect2Value clone() {
            return new Rect2Value(x, y, width, height);
        }
    }
}
