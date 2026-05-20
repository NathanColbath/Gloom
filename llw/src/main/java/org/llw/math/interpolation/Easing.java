package org.llw.math.interpolation;

import org.llw.math.util.MathUtils;

/**
 * Common easing functions mapping {@code t} in [0, 1] to [0, 1].
 */
public enum Easing {
    /** {@code t}. */
    LINEAR {
        @Override
        public float apply(float t) {
            return t;
        }
    },
    /** {@code t²}. */
    EASE_IN_QUAD {
        @Override
        public float apply(float t) {
            return t * t;
        }
    },
    /** {@code 1 - (1-t)²}. */
    EASE_OUT_QUAD {
        @Override
        public float apply(float t) {
            float inv = 1f - t;
            return 1f - inv * inv;
        }
    },
    /** Piecewise quad in/out. */
    EASE_IN_OUT_QUAD {
        @Override
        public float apply(float t) {
            if (t < 0.5f) {
                return 2f * t * t;
            }
            float inv = -2f * t + 2f;
            return 1f - inv * inv * 0.5f;
        }
    },
    /** {@code t³}. */
    EASE_IN_CUBIC {
        @Override
        public float apply(float t) {
            return t * t * t;
        }
    },
    /** {@code 1 - (1-t)³}. */
    EASE_OUT_CUBIC {
        @Override
        public float apply(float t) {
            float inv = 1f - t;
            return 1f - inv * inv * inv;
        }
    },
    /** Piecewise cubic in/out. */
    EASE_IN_OUT_CUBIC {
        @Override
        public float apply(float t) {
            if (t < 0.5f) {
                return 4f * t * t * t;
            }
            float inv = -2f * t + 2f;
            return 1f - inv * inv * inv * 0.5f;
        }
    };

    /**
     * Evaluates the easing curve at {@code t} (clamped to [0, 1]).
     *
     * @param t input parameter
     * @return eased value in [0, 1]
     */
    public float evaluate(float t) {
        return apply(MathUtils.clamp(t, 0f, 1f));
    }

    abstract float apply(float t);
}
