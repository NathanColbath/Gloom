package org.llw.math;

import org.llw.math.noise.PerlinNoise;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerlinNoiseTest {

    @Test
    void sameSeedSameOutput() {
        PerlinNoise a = new PerlinNoise(42);
        PerlinNoise b = new PerlinNoise(42);
        assertEquals(a.noise2D(1.5f, 2.5f), b.noise2D(1.5f, 2.5f), 1e-6f);
    }
}
