package org.llw.studio.curves;

import java.util.ArrayList;
import java.util.List;

/**
 * Scalar value that may be constant, a random range, or a curve over normalized lifetime {@code [0, 1]}.
 */
public final class MinMaxCurve {
    public enum Mode {
        CONSTANT,
        TWO_CONSTANTS,
        CURVE
    }

    public static final class Keyframe {
        public float time;
        public float value;

        public Keyframe() {
        }

        public Keyframe(float time, float value) {
            this.time = time;
            this.value = value;
        }
    }

    public Mode mode = Mode.CONSTANT;
    public float constant = 1f;
    public float min = 0f;
    public float max = 1f;
    public final List<Keyframe> keyframes = new ArrayList<>();

    public MinMaxCurve copy() {
        MinMaxCurve copy = new MinMaxCurve();
        copy.mode = mode;
        copy.constant = constant;
        copy.min = min;
        copy.max = max;
        for (Keyframe keyframe : keyframes) {
            copy.keyframes.add(new Keyframe(keyframe.time, keyframe.value));
        }
        return copy;
    }
}
