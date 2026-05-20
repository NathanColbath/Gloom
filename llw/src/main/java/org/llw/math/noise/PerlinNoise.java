package org.llw.math.noise;

/**
 * Seeded 2D Perlin gradient noise in the range approximately [-1, 1].
 */
public final class PerlinNoise {
    private final int[] perm = new int[512];

    /**
     * Creates noise with the given seed.
     *
     * @param seed deterministic seed
     */
    public PerlinNoise(int seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        int state = seed;
        for (int i = 255; i > 0; i--) {
            state = state * 1103515245 + 12345;
            int j = (state >>> 16) % (i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    /**
     * Samples 1D noise at {@code x} (2D slice at y=0).
     *
     * @param x coordinate
     * @return noise value
     */
    public float noise1D(float x) {
        return noise2D(x, 0f);
    }

    /**
     * Samples 2D noise at {@code (x, y)}.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return noise value
     */
    public float noise2D(float x, float y) {
        int xi = fastFloor(x) & 255;
        int yi = fastFloor(y) & 255;
        float xf = x - fastFloor(x);
        float yf = y - fastFloor(y);
        float u = fade(xf);
        float v = fade(yf);
        int aa = perm[perm[xi] + yi];
        int ab = perm[perm[xi] + yi + 1];
        int ba = perm[perm[xi + 1] + yi];
        int bb = perm[perm[xi + 1] + yi + 1];
        float x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1f, yf), u);
        float x2 = lerp(grad(ab, xf, yf - 1f), grad(bb, xf - 1f, yf - 1f), u);
        return lerp(x1, x2, v);
    }

    private static int fastFloor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = h < 2 ? x : y;
        float v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
