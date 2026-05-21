package org.llw.studio.curves;

import java.util.ArrayList;
import java.util.List;

/**
 * Color gradient over normalized lifetime {@code [0, 1]}.
 */
public final class Gradient {
    public static final class ColorKey {
        public float time;
        public float r = 1f;
        public float g = 1f;
        public float b = 1f;
        public float a = 1f;

        public ColorKey() {
        }

        public ColorKey(float time, float r, float g, float b, float a) {
            this.time = time;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    public final List<ColorKey> keys = new ArrayList<>();

    public Gradient copy() {
        Gradient copy = new Gradient();
        for (ColorKey key : keys) {
            copy.keys.add(new ColorKey(key.time, key.r, key.g, key.b, key.a));
        }
        return copy;
    }
}
