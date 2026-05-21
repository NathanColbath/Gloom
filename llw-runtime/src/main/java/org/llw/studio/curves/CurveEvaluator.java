package org.llw.studio.curves;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Evaluates {@link MinMaxCurve} and {@link Gradient} at normalized lifetime and spawn time.
 */
public final class CurveEvaluator {
    private CurveEvaluator() {
    }

    /**
     * @param curve      curve definition
     * @param normalized particle age / max life in {@code [0, 1]}
     * @param randomSeed per-particle seed for two-constant mode
     * @return evaluated scalar
     */
    public static float evaluate(MinMaxCurve curve, float normalized, int randomSeed) {
        if (curve == null) {
            return 1f;
        }
        return switch (curve.mode) {
            case CONSTANT -> curve.constant;
            case TWO_CONSTANTS -> lerp(curve.min, curve.max, hash01(randomSeed));
            case CURVE -> sampleKeyframes(curve.keyframes, normalized, curve.constant);
        };
    }

    /**
     * @param gradient   color keys
     * @param normalized particle age / max life
     * @param out        four floats {@code [r,g,b,a]} written here
     */
    public static void evaluateColor(Gradient gradient, float normalized, float[] out) {
        if (gradient == null || gradient.keys.isEmpty()) {
            out[0] = out[1] = out[2] = out[3] = 1f;
            return;
        }
        if (gradient.keys.size() == 1) {
            Gradient.ColorKey key = gradient.keys.get(0);
            out[0] = key.r;
            out[1] = key.g;
            out[2] = key.b;
            out[3] = key.a;
            return;
        }
        float t = clamp01(normalized);
        Gradient.ColorKey left = gradient.keys.get(0);
        Gradient.ColorKey right = gradient.keys.get(gradient.keys.size() - 1);
        for (int i = 0; i < gradient.keys.size() - 1; i++) {
            Gradient.ColorKey a = gradient.keys.get(i);
            Gradient.ColorKey b = gradient.keys.get(i + 1);
            if (t >= a.time && t <= b.time) {
                left = a;
                right = b;
                break;
            }
            if (t < a.time) {
                left = a;
                right = a;
                break;
            }
        }
        float span = Math.max(0.0001f, right.time - left.time);
        float u = (t - left.time) / span;
        out[0] = lerp(left.r, right.r, u);
        out[1] = lerp(left.g, right.g, u);
        out[2] = lerp(left.b, right.b, u);
        out[3] = lerp(left.a, right.a, u);
    }

    public static float randomTwoConstants(MinMaxCurve curve, int randomSeed) {
        if (curve == null || curve.mode != MinMaxCurve.Mode.TWO_CONSTANTS) {
            return curve == null ? 1f : curve.constant;
        }
        return lerp(curve.min, curve.max, hash01(randomSeed));
    }

    public static float randomTwoConstants(MinMaxCurve curve) {
        if (curve == null || curve.mode != MinMaxCurve.Mode.TWO_CONSTANTS) {
            return curve == null ? 1f : curve.constant;
        }
        return lerp(curve.min, curve.max, ThreadLocalRandom.current().nextFloat());
    }

    private static float sampleKeyframes(java.util.List<MinMaxCurve.Keyframe> keyframes, float normalized, float fallback) {
        if (keyframes == null || keyframes.isEmpty()) {
            return fallback;
        }
        if (keyframes.size() == 1) {
            return keyframes.get(0).value;
        }
        float t = clamp01(normalized);
        MinMaxCurve.Keyframe left = keyframes.get(0);
        MinMaxCurve.Keyframe right = keyframes.get(keyframes.size() - 1);
        for (int i = 0; i < keyframes.size() - 1; i++) {
            MinMaxCurve.Keyframe a = keyframes.get(i);
            MinMaxCurve.Keyframe b = keyframes.get(i + 1);
            if (t >= a.time && t <= b.time) {
                left = a;
                right = b;
                break;
            }
        }
        float span = Math.max(0.0001f, right.time - left.time);
        float u = (t - left.time) / span;
        return lerp(left.value, right.value, u);
    }

    private static float hash01(int seed) {
        int h = seed * 0x9E3779B9;
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return (h & 0xFFFF) / 65535f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}
