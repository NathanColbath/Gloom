package org.llw.studio.particles.runtime;

/**
 * Lightweight 2D value noise for particle turbulence.
 */
public final class ParticleNoise {
    private ParticleNoise() {
    }

    public static float sample(float x, float y) {
        int ix = fastFloor(x);
        int iy = fastFloor(y);
        float fx = x - ix;
        float fy = y - iy;
        float a = hash(ix, iy);
        float b = hash(ix + 1, iy);
        float c = hash(ix, iy + 1);
        float d = hash(ix + 1, iy + 1);
        float ux = smooth(fx);
        float uy = smooth(fy);
        return lerp(lerp(a, b, ux), lerp(c, d, ux), uy) * 2f - 1f;
    }

    private static int fastFloor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private static float hash(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 65535f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
